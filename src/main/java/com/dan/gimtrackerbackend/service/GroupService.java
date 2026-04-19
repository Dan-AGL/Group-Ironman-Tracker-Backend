package com.dan.gimtrackerbackend.service;

import com.dan.gimtrackerbackend.dto.AuthenticatedGroupResponse;
import com.dan.gimtrackerbackend.dto.CreateGroupRequest;
import com.dan.gimtrackerbackend.dto.JoinGroupRequest;
import com.dan.gimtrackerbackend.dto.LeaveGroupRequest;
import com.dan.gimtrackerbackend.dto.RemoveGroupMemberRequest;
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
        String role = resolveJoinRole(group, playerName);

        GroupMemberEntity existingMembership = groupMemberRepository.findByGroupAndPlayerNameIgnoreCase(group, playerName).orElse(null);
        if (existingMembership != null)
        {
            applyOwnerRole(group, existingMembership, role);
            existingMembership.setSessionToken(generateUniqueSessionToken());
            groupMemberRepository.save(existingMembership);
            return AuthenticatedGroupResponse.from(group, existingMembership);
        }

        long currentMemberCount = groupMemberRepository.countByGroup(group);
        if (currentMemberCount >= MAX_GROUP_MEMBERS)
        {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Group is already full");
        }

        GroupMemberEntity membership = new GroupMemberEntity();
        membership.setGroup(group);
        membership.setPlayerName(playerName);
        membership.setRole(role);
        if ("OWNER".equals(role))
        {
            demoteOtherOwners(group, null);
        }
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

    private String resolveJoinRole(GroupEntity group, String playerName)
    {
        return group.getCreatedBy().equalsIgnoreCase(playerName) ? "OWNER" : "MEMBER";
    }

    private void applyOwnerRole(GroupEntity group, GroupMemberEntity membership, String role)
    {
        if ("OWNER".equals(role))
        {
            demoteOtherOwners(group, membership);
        }
        membership.setRole(role);
    }

    private void demoteOtherOwners(GroupEntity group, GroupMemberEntity keepOwner)
    {
        groupMemberRepository.findByGroupOrderByJoinedAtAsc(group).stream()
            .filter(member -> "OWNER".equals(member.getRole()))
            .filter(member -> keepOwner == null || !member.getId().equals(keepOwner.getId()))
            .forEach(member ->
            {
                member.setRole("MEMBER");
                groupMemberRepository.save(member);
            });
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
