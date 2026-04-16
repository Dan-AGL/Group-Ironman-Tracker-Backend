package com.dan.gimtrackerbackend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body used when a group owner removes one member from the team.
 */
public class RemoveGroupMemberRequest
{
    @NotBlank
    private String inviteCode;

    @NotBlank
    private String ownerPlayerName;

    @NotBlank
    private String targetPlayerName;

    public String getInviteCode()
    {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode)
    {
        this.inviteCode = inviteCode;
    }

    public String getOwnerPlayerName()
    {
        return ownerPlayerName;
    }

    public void setOwnerPlayerName(String ownerPlayerName)
    {
        this.ownerPlayerName = ownerPlayerName;
    }

    public String getTargetPlayerName()
    {
        return targetPlayerName;
    }

    public void setTargetPlayerName(String targetPlayerName)
    {
        this.targetPlayerName = targetPlayerName;
    }
}
