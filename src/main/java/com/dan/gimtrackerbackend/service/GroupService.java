package com.dan.gimtrackerbackend.service;

import com.dan.gimtrackerbackend.dto.AuthenticateMemberRequest;
import com.dan.gimtrackerbackend.dto.AuthenticatedGroupResponse;
import com.dan.gimtrackerbackend.dto.BootstrapSessionRequest;
import com.dan.gimtrackerbackend.dto.CreateGroupRequest;
import com.dan.gimtrackerbackend.dto.GetMemberAuthCodeRequest;
import com.dan.gimtrackerbackend.dto.JoinGroupRequest;
import com.dan.gimtrackerbackend.dto.LeaveGroupRequest;
import com.dan.gimtrackerbackend.dto.MemberAuthCodeResponse;
import com.dan.gimtrackerbackend.dto.RemoveGroupMemberRequest;
import com.dan.gimtrackerbackend.dto.ResetMemberAuthCodeRequest;
import com.dan.gimtrackerbackend.model.GroupEntity;
import com.dan.gimtrackerbackend.model.GroupMemberEntity;
import com.dan.gimtrackerbackend.repository.GroupMemberRepository;
import com.dan.gimtrackerbackend.repository.GroupRepository;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Handles creation and membership rules for shared groups.
 */
@Service
public class GroupService
{
    private static final int MAX_GROUP_MEMBERS = 5;
    private static final String INVITE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int INVITE_LENGTH = 8;
    private static final int AUTH_CODE_BLOCK_LENGTH = 4;
    private static final int AUTH_CODE_BLOCKS = 5;
    private static final int SESSION_TOKEN_BYTES = 24;

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final SecureRandom random = new SecureRandom();

    public GroupService(GroupRepository groupRepository, GroupMemberRepository groupMemberRepository)
    {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    /**
     * Creates a group and automatically adds the creator as the first member.
     */
    public AuthenticatedGroupResponse createGroup(CreateGroupRequest request)
    {
        GroupEntity group = new GroupEntity();
        group.setName(request.getGroupName().trim());
        group.setCreatedBy(normalizePlayerName(request.getCreatorPlayerName()));
        group.setInviteCode(generateUniqueInviteCode());
        GroupEntity savedGroup = groupRepository.save(group);

        GroupMemberEntity creatorMembership = new GroupMemberEntity();
        creatorMembership.setGroup(savedGroup);
        creatorMembership.setPlayerName(normalizePlayerName(request.getCreatorPlayerName()));
        creatorMembership.setRole("OWNER");
        creatorMembership.setAuthCode(generateUniqueMemberAuthCode());
        creatorMembership.setSessionToken(generateUniqueSessionToken());
        groupMemberRepository.save(creatorMembership);

        return AuthenticatedGroupResponse.from(savedGroup, creatorMembership);
    }

    /**
     * Adds a player to an existing group by invite code.
     */
    public AuthenticatedGroupResponse joinGroup(JoinGroupRequest request)
    {
        GroupEntity group = getGroupByInviteCode(request.getInviteCode());
        String playerName = normalizePlayerName(request.getPlayerName());

        GroupMemberEntity existingMembership = groupMemberRepository.findByGroupAndPlayerNameIgnoreCase(group, playerName).orElse(null);
        if (existingMembership != null)
        {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Member already exists, authenticate with your auth code");
        }

        long currentMemberCount = groupMemberRepository.countByGroup(group);
        if (currentMemberCount >= MAX_GROUP_MEMBERS)
        {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Group is already full");
        }

        GroupMemberEntity membership = new GroupMemberEntity();
        membership.setGroup(group);
        membership.setPlayerName(playerName);
        membership.setRole("MEMBER");
        membership.setAuthCode(generateUniqueMemberAuthCode());
        membership.setSessionToken(generateUniqueSessionToken());
        groupMemberRepository.save(membership);
        return AuthenticatedGroupResponse.from(group, membership);
    }

    /**
     * Exchanges one member auth code for a fresh session token.
     */
    public AuthenticatedGroupResponse authenticateMember(AuthenticateMemberRequest request)
    {
        GroupEntity group = getGroupByInviteCode(request.getInviteCode());
        GroupMemberEntity membership = groupMemberRepository.findByGroupOrderByJoinedAtAsc(group)
            .stream()
            .filter(member -> request.getAuthCode().trim().equalsIgnoreCase(member.getAuthCode()))
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid auth code"));

        membership.setSessionToken(generateUniqueSessionToken());
        groupMemberRepository.save(membership);
        return AuthenticatedGroupResponse.from(group, membership);
    }

    /**
     * Temporary migration path for existing members saved before session tokens were introduced.
     */
    public AuthenticatedGroupResponse bootstrapSession(BootstrapSessionRequest request)
    {
        GroupEntity group = getGroupByInviteCode(request.getInviteCode());
        GroupMemberEntity membership = requireMembership(group, request.getPlayerName());
        membership.setSessionToken(generateUniqueSessionToken());
        groupMemberRepository.save(membership);
        return AuthenticatedGroupResponse.from(group, membership);
    }

    /**
     * Looks up a group from the invite code shown to players.
     */
    public GroupEntity getGroupByInviteCode(String inviteCode)
    {
        return groupRepository.findByInviteCode(normalizeInviteCode(inviteCode))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));
    }

    public GroupEntity getGroupByInviteCode(String sessionToken, String inviteCode)
    {
        return requireSessionMembership(sessionToken, inviteCode).getGroup();
    }

    /**
     * Returns the members belonging to one group in join order.
     */
    public List<GroupMemberEntity> getMembers(String sessionToken, String inviteCode)
    {
        GroupEntity group = requireSessionMembership(sessionToken, inviteCode).getGroup();
        return groupMemberRepository.findByGroupOrderByJoinedAtAsc(group);
    }

    /**
     * Removes one player from a group and deletes the group if it becomes empty.
     */
    public void leaveGroup(String sessionToken, LeaveGroupRequest request)
    {
        GroupMemberEntity membership = requireSessionMembership(sessionToken, request.getInviteCode());
        GroupEntity group = membership.getGroup();

        boolean ownerLeaving = "OWNER".equals(membership.getRole());
        groupMemberRepository.delete(membership);

        long remainingMembers = groupMemberRepository.countByGroup(group);
        if (remainingMembers == 0)
        {
            groupRepository.delete(group);
            return;
        }

        if (ownerLeaving)
        {
            groupMemberRepository.findFirstByGroupOrderByJoinedAtAsc(group)
                .ifPresent(nextOwner ->
                {
                    nextOwner.setRole("OWNER");
                    groupMemberRepository.save(nextOwner);
                });
        }
    }

    /**
     * Removes a non-owner member after verifying the caller is the current owner.
     */
    public void removeMember(String sessionToken, RemoveGroupMemberRequest request)
    {
        GroupMemberEntity ownerMembership = requireSessionMembership(sessionToken, request.getInviteCode());
        GroupEntity group = ownerMembership.getGroup();

        if (!"OWNER".equals(ownerMembership.getRole()))
        {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the group owner can remove members");
        }

        GroupMemberEntity targetMembership = groupMemberRepository.findByGroupAndPlayerNameIgnoreCase(
                group,
                normalizePlayerName(request.getTargetPlayerName())
            )
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group member not found"));

        if ("OWNER".equals(targetMembership.getRole()))
        {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The group owner cannot be removed");
        }

        groupMemberRepository.delete(targetMembership);
    }

    /**
     * Returns the current auth code for one existing member.
     */
    public MemberAuthCodeResponse getMemberAuthCode(String sessionToken, GetMemberAuthCodeRequest request)
    {
        GroupMemberEntity membership = requireSessionMembership(sessionToken, request.getInviteCode());
        return new MemberAuthCodeResponse(membership.getPlayerName(), membership.getAuthCode());
    }

    /**
     * Rotates one member auth code after verifying the caller is the current owner.
     */
    public MemberAuthCodeResponse resetMemberAuthCode(String sessionToken, ResetMemberAuthCodeRequest request)
    {
        GroupMemberEntity ownerMembership = requireSessionMembership(sessionToken, request.getInviteCode());
        GroupEntity group = ownerMembership.getGroup();
        if (!"OWNER".equals(ownerMembership.getRole()))
        {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the group owner can reset auth codes");
        }

        GroupMemberEntity targetMembership = requireMembership(group, request.getTargetPlayerName());
        targetMembership.setAuthCode(generateUniqueMemberAuthCode());
        groupMemberRepository.save(targetMembership);
        return new MemberAuthCodeResponse(targetMembership.getPlayerName(), targetMembership.getAuthCode());
    }

    private String generateUniqueInviteCode()
    {
        for (int attempt = 0; attempt < 20; attempt++)
        {
            String code = randomInviteCode();
            if (groupRepository.findByInviteCode(code).isEmpty())
            {
                return code;
            }
        }

        throw new IllegalStateException("Unable to generate a unique invite code");
    }

    private String generateUniqueMemberAuthCode()
    {
        for (int attempt = 0; attempt < 20; attempt++)
        {
            String code = randomMemberAuthCode();
            if (!groupMemberRepository.existsByAuthCode(code))
            {
                return code;
            }
        }

        throw new IllegalStateException("Unable to generate a unique member auth code");
    }

    private String generateUniqueSessionToken()
    {
        for (int attempt = 0; attempt < 20; attempt++)
        {
            byte[] randomBytes = new byte[SESSION_TOKEN_BYTES];
            random.nextBytes(randomBytes);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
            if (!groupMemberRepository.existsBySessionToken(token))
            {
                return token;
            }
        }

        throw new IllegalStateException("Unable to generate a unique session token");
    }

    private String randomInviteCode()
    {
        StringBuilder builder = new StringBuilder(INVITE_LENGTH);
        for (int index = 0; index < INVITE_LENGTH; index++)
        {
            builder.append(INVITE_ALPHABET.charAt(random.nextInt(INVITE_ALPHABET.length())));
        }
        return builder.toString();
    }

    private String randomMemberAuthCode()
    {
        StringBuilder builder = new StringBuilder((AUTH_CODE_BLOCK_LENGTH * AUTH_CODE_BLOCKS) + (AUTH_CODE_BLOCKS - 1));
        for (int block = 0; block < AUTH_CODE_BLOCKS; block++)
        {
            if (block > 0)
            {
                builder.append('-');
            }

            for (int index = 0; index < AUTH_CODE_BLOCK_LENGTH; index++)
            {
                builder.append(INVITE_ALPHABET.charAt(random.nextInt(INVITE_ALPHABET.length())));
            }
        }
        return builder.toString();
    }

    private GroupMemberEntity requireMembership(String inviteCode, String playerName)
    {
        GroupEntity group = getGroupByInviteCode(inviteCode);
        return requireMembership(group, playerName);
    }

    public GroupMemberEntity requireSessionMembership(String sessionToken, String inviteCode)
    {
        GroupMemberEntity membership = groupMemberRepository.findBySessionToken(normalizeSessionToken(sessionToken))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid session token"));

        if (!membership.getGroup().getInviteCode().equals(normalizeInviteCode(inviteCode)))
        {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Session token does not match this group");
        }

        return membership;
    }

    private GroupMemberEntity requireMembership(GroupEntity group, String playerName)
    {
        return groupMemberRepository.findByGroupAndPlayerNameIgnoreCase(
                group,
                normalizePlayerName(playerName)
            )
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group member not found"));
    }

    private String normalizeInviteCode(String inviteCode)
    {
        return inviteCode.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizePlayerName(String playerName)
    {
        return playerName.trim();
    }

    private String normalizeSessionToken(String sessionToken)
    {
        return sessionToken.trim();
    }
}
