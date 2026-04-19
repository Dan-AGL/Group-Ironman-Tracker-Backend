package com.dan.gimtrackerbackend.api;

import com.dan.gimtrackerbackend.dto.AuthenticatedGroupResponse;
import com.dan.gimtrackerbackend.dto.CreateGroupRequest;
import com.dan.gimtrackerbackend.dto.GroupMemberResponse;
import com.dan.gimtrackerbackend.dto.GroupResponse;
import com.dan.gimtrackerbackend.dto.JoinGroupRequest;
import com.dan.gimtrackerbackend.dto.LeaveGroupRequest;
import com.dan.gimtrackerbackend.dto.RemoveGroupMemberRequest;
import com.dan.gimtrackerbackend.service.GroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
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
    public AuthenticatedGroupResponse createGroup(@Valid @RequestBody CreateGroupRequest request)
    {
        return groupService.createGroup(request);
    }

    /**
     * Joins an existing group with an invite code.
     */
    @PostMapping("/join")
    public AuthenticatedGroupResponse joinGroup(@Valid @RequestBody JoinGroupRequest request)
    {
        return groupService.joinGroup(request);
    }

    /**
     * Removes one player from the group they previously joined.
     */
    @PostMapping("/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveGroup(
        @RequestHeader("X-GIM-Session") String sessionToken,
        @Valid @RequestBody LeaveGroupRequest request
    )
    {
        groupService.leaveGroup(sessionToken, request);
    }

    /**
     * Removes one non-owner member from the group on behalf of the current owner.
     */
    @PostMapping("/remove-member")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(
        @RequestHeader("X-GIM-Session") String sessionToken,
        @Valid @RequestBody RemoveGroupMemberRequest request
    )
    {
        groupService.removeMember(sessionToken, request);
    }

    /**
     * Returns one group by invite code so the plugin can hydrate saved membership.
     */
    @GetMapping("/{inviteCode}")
    public GroupResponse getGroup(
        @RequestHeader("X-GIM-Session") String sessionToken,
        @PathVariable String inviteCode
    )
    {
        return GroupResponse.fromEntity(groupService.getGroupByInviteCode(sessionToken, inviteCode));
    }

    /**
     * Returns the players currently in the group.
     */
    @GetMapping("/{inviteCode}/members")
    public List<GroupMemberResponse> getMembers(
        @RequestHeader("X-GIM-Session") String sessionToken,
        @PathVariable String inviteCode
    )
    {
        return groupService.getMembers(sessionToken, inviteCode)
            .stream()
            .map(GroupMemberResponse::fromEntity)
            .toList();
    }
}
