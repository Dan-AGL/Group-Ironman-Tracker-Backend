package com.dan.gimtrackerbackend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body used when the current owner resets one member's auth code.
 */
public class ResetMemberAuthCodeRequest
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
