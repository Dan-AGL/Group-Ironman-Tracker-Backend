package com.dan.gimtrackerbackend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body used when one member exchanges their auth code for a session token.
 */
public class AuthenticateMemberRequest
{
    @NotBlank
    private String inviteCode;

    @NotBlank
    private String authCode;

    public String getInviteCode()
    {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode)
    {
        this.inviteCode = inviteCode;
    }

    public String getAuthCode()
    {
        return authCode;
    }

    public void setAuthCode(String authCode)
    {
        this.authCode = authCode;
    }
}
