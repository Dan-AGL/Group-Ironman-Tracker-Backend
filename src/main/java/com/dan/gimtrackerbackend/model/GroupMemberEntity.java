package com.dan.gimtrackerbackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

/**
 * Represents one player membership inside a shared Group Ironman team.
 */
@Entity
@Table(
    name = "group_members",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_group_member_group_player", columnNames = {"group_id", "player_name"})
    }
)
public class GroupMemberEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupEntity group;

    @Column(name = "player_name", nullable = false)
    private String playerName;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false, updatable = false)
    private Instant joinedAt;

    @PrePersist
    public void prePersist()
    {
        joinedAt = Instant.now();
    }

    public Long getId()
    {
        return id;
    }

    public GroupEntity getGroup()
    {
        return group;
    }

    public void setGroup(GroupEntity group)
    {
        this.group = group;
    }

    public String getPlayerName()
    {
        return playerName;
    }

    public void setPlayerName(String playerName)
    {
        this.playerName = playerName;
    }

    public String getRole()
    {
        return role;
    }

    public void setRole(String role)
    {
        this.role = role;
    }

    public Instant getJoinedAt()
    {
        return joinedAt;
    }
}
