package com.dan.gimtrackerbackend.dto;

import com.dan.gimtrackerbackend.model.GroupEntity;

import java.time.Instant;

/**
 * API response shape for a group and its invite metadata.
 */
public class GroupResponse
{
    private Long id;
    private String name;
    private String inviteCode;
    private String createdBy;
    private Instant createdAt;

    public static GroupResponse fromEntity(GroupEntity entity)
    {
        GroupResponse response = new GroupResponse();
        response.id = entity.getId();
        response.name = entity.getName();
        response.inviteCode = entity.getInviteCode();
        response.createdBy = entity.getCreatedBy();
        response.createdAt = entity.getCreatedAt();
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
}
