package com.dan.gimtrackerbackend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body used when a player leaves a group.
 */
public class LeaveGroupRequest
{
    @NotBlank
    private String inviteCode;

    @NotBlank
    private String playerName;

    public String getInviteCode()
    {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode)
    {
        this.inviteCode = inviteCode;
    }

    public String getPlayerName()
    {
        return playerName;
    }

    public void setPlayerName(String playerName)
    {
        this.playerName = playerName;
    }
}
