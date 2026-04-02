package com.dan.gimtrackerbackend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body used when a player creates a new group.
 */
public class CreateGroupRequest
{
    @NotBlank
    private String groupName;

    @NotBlank
    private String creatorPlayerName;

    public String getGroupName()
    {
        return groupName;
    }

    public void setGroupName(String groupName)
    {
        this.groupName = groupName;
    }

    public String getCreatorPlayerName()
    {
        return creatorPlayerName;
    }

    public void setCreatorPlayerName(String creatorPlayerName)
    {
        this.creatorPlayerName = creatorPlayerName;
    }
}
