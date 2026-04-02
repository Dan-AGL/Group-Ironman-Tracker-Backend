package com.dan.gimtrackerbackend.dto;

import com.dan.gimtrackerbackend.model.GroupMemberEntity;

import java.time.Instant;

/**
 * API response shape for one group member.
 */
public class GroupMemberResponse
{
    private Long id;
    private String playerName;
    private String role;
    private Instant joinedAt;

    public static GroupMemberResponse fromEntity(GroupMemberEntity entity)
    {
        GroupMemberResponse response = new GroupMemberResponse();
        response.id = entity.getId();
        response.playerName = entity.getPlayerName();
        response.role = entity.getRole();
        response.joinedAt = entity.getJoinedAt();
        return response;
    }

    public Long getId()
    {
        return id;
    }

    public String getPlayerName()
    {
        return playerName;
    }

    public String getRole()
    {
        return role;
    }

    public Instant getJoinedAt()
    {
        return joinedAt;
    }
}
