package com.dan.gimtrackerbackend.repository;

import com.dan.gimtrackerbackend.model.GroupEntity;
import com.dan.gimtrackerbackend.model.GroupMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for group membership rows.
 */
public interface GroupMemberRepository extends JpaRepository<GroupMemberEntity, Long>
{
    List<GroupMemberEntity> findByGroupOrderByJoinedAtAsc(GroupEntity group);

    Optional<GroupMemberEntity> findByGroupAndPlayerNameIgnoreCase(GroupEntity group, String playerName);

    Optional<GroupMemberEntity> findFirstByGroupOrderByJoinedAtAsc(GroupEntity group);

    long countByGroup(GroupEntity group);
}
