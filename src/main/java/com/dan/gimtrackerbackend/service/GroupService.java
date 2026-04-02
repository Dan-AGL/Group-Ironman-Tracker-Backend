package com.dan.gimtrackerbackend.service;

import com.dan.gimtrackerbackend.dto.CreateGroupRequest;
import com.dan.gimtrackerbackend.dto.JoinGroupRequest;
import com.dan.gimtrackerbackend.model.GroupEntity;
import com.dan.gimtrackerbackend.model.GroupMemberEntity;
import com.dan.gimtrackerbackend.repository.GroupMemberRepository;
import com.dan.gimtrackerbackend.repository.GroupRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Handles creation and membership rules for shared groups.
 */
@Service
public class GroupService
{
    private static final int MAX_GROUP_MEMBERS = 5;
    private static final String INVITE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int INVITE_LENGTH = 8;

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final Random random = new Random();

    public GroupService(GroupRepository groupRepository, GroupMemberRepository groupMemberRepository)
    {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    /**
     * Creates a group and automatically adds the creator as the first member.
     */
    public GroupEntity createGroup(CreateGroupRequest request)
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
        groupMemberRepository.save(creatorMembership);

        return savedGroup;
    }

    /**
     * Adds a player to an existing group by invite code.
     */
    public GroupEntity joinGroup(JoinGroupRequest request)
    {
        GroupEntity group = getGroupByInviteCode(request.getInviteCode());
        String playerName = normalizePlayerName(request.getPlayerName());

        if (groupMemberRepository.findByGroupAndPlayerNameIgnoreCase(group, playerName).isPresent())
        {
            return group;
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
        groupMemberRepository.save(membership);
        return group;
    }

    /**
     * Looks up a group from the invite code shown to players.
     */
    public GroupEntity getGroupByInviteCode(String inviteCode)
    {
        return groupRepository.findByInviteCode(normalizeInviteCode(inviteCode))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));
    }

    /**
     * Returns the members belonging to one group in join order.
     */
    public List<GroupMemberEntity> getMembers(String inviteCode)
    {
        GroupEntity group = getGroupByInviteCode(inviteCode);
        return groupMemberRepository.findByGroupOrderByJoinedAtAsc(group);
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

    private String randomInviteCode()
    {
        StringBuilder builder = new StringBuilder(INVITE_LENGTH);
        for (int index = 0; index < INVITE_LENGTH; index++)
        {
            builder.append(INVITE_ALPHABET.charAt(random.nextInt(INVITE_ALPHABET.length())));
        }
        return builder.toString();
    }

    private String normalizeInviteCode(String inviteCode)
    {
        return inviteCode.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizePlayerName(String playerName)
    {
        return playerName.trim();
    }
}
