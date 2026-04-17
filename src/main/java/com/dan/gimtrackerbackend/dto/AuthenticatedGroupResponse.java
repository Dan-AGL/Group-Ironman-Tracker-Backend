package com.dan.gimtrackerbackend.dto;

import com.dan.gimtrackerbackend.model.GroupEntity;
import com.dan.gimtrackerbackend.model.GroupMemberEntity;
import java.time.Instant;

/**
 * Group response variant that also carries the authenticated member session state.
 */
public class AuthenticatedGroupResponse
{
    private Long id;
    private String name;
    private String inviteCode;
    private String createdBy;
    private Instant createdAt;
    private String playerName;
    private String role;
    private String sessionToken;

    public static AuthenticatedGroupResponse from(GroupEntity group, GroupMemberEntity member)
    {
        AuthenticatedGroupResponse response = new AuthenticatedGroupResponse();
        response.id = group.getId();
        response.name = group.getName();
        response.inviteCode = group.getInviteCode();
        response.createdBy = group.getCreatedBy();
        response.createdAt = group.getCreatedAt();
        response.playerName = member.getPlayerName();
        response.role = member.getRole();
        response.sessionToken = member.getSessionToken();
        return response;
    }

    public Long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getInviteCode()
    {
        return inviteCode;
    }

    public String getCreatedBy()
    {
        return createdBy;
    }

    public Instant getCreatedAt()
    {
        return createdAt;
    }

    public String getPlayerName()
    {
        return playerName;
    }

    public String getRole()
    {
        return role;
    }

    public String getSessionToken()
    {
        return sessionToken;
    }
}
