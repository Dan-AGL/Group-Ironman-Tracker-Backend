package com.dan.gimtrackerbackend.repository;

import com.dan.gimtrackerbackend.model.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for persisted Group Ironman teams.
 */
public interface GroupRepository extends JpaRepository<GroupEntity, Long>
{
    Optional<GroupEntity> findByInviteCode(String inviteCode);
}
