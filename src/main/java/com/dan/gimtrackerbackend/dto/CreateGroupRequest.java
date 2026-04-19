package com.dan.gimtrackerbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body used when a player creates a new group.
 */
public class CreateGroupRequest
{
    @NotBlank
    @Size(max = 25)
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
