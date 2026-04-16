package com.dan.gimtrackerbackend.api;

import com.dan.gimtrackerbackend.dto.CreateGroupRequest;
import com.dan.gimtrackerbackend.dto.GroupMemberResponse;
import com.dan.gimtrackerbackend.dto.GroupResponse;
import com.dan.gimtrackerbackend.dto.JoinGroupRequest;
import com.dan.gimtrackerbackend.dto.LeaveGroupRequest;
import com.dan.gimtrackerbackend.dto.RemoveGroupMemberRequest;
import com.dan.gimtrackerbackend.model.GroupEntity;
import com.dan.gimtrackerbackend.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * API surface for creating and joining shared groups.
 */
@RestController
@RequestMapping("/api/groups")
public class GroupController
{
    private final GroupService groupService;

    public GroupController(GroupService groupService)
    {
        this.groupService = groupService;
    }

    /**
     * Creates a new group and returns the generated invite code.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupResponse createGroup(@Valid @RequestBody CreateGroupRequest request)
    {
        GroupEntity group = groupService.createGroup(request);
        return GroupResponse.fromEntity(group);
    }

    /**
     * Joins an existing group with an invite code.
     */
    @PostMapping("/join")
    public GroupResponse joinGroup(@Valid @RequestBody JoinGroupRequest request)
    {
        GroupEntity group = groupService.joinGroup(request);
        return GroupResponse.fromEntity(group);
    }

    /**
     * Removes one player from the group they previously joined.
     */
    @PostMapping("/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveGroup(@Valid @RequestBody LeaveGroupRequest request)
    {
        groupService.leaveGroup(request);
    }

    /**
     * Removes one non-owner member from the group on behalf of the current owner.
     */
    @PostMapping("/remove-member")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@Valid @RequestBody RemoveGroupMemberRequest request)
    {
        groupService.removeMember(request);
    }

    /**
     * Returns one group by invite code so the plugin can hydrate saved membership.
     */
    @GetMapping("/{inviteCode}")
    public GroupResponse getGroup(@PathVariable String inviteCode)
    {
        return GroupResponse.fromEntity(groupService.getGroupByInviteCode(inviteCode));
    }

    /**
     * Returns the players currently in the group.
     */
    @GetMapping("/{inviteCode}/members")
    public List<GroupMemberResponse> getMembers(@PathVariable String inviteCode)
    {
        return groupService.getMembers(inviteCode)
            .stream()
            .map(GroupMemberResponse::fromEntity)
            .toList();
    }
}
