package com.dan.gimtrackerbackend.dto;

/**
 * Response containing one member's recovery/auth code.
 */
public class MemberAuthCodeResponse
{
    private final String playerName;
    private final String authCode;

    public MemberAuthCodeResponse(String playerName, String authCode)
    {
        this.playerName = playerName;
        this.authCode = authCode;
    }

    public String getPlayerName()
    {
        return playerName;
    }

    public String getAuthCode()
    {
        return authCode;
    }
}
