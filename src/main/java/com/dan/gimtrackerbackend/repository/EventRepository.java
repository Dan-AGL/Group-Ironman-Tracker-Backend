package com.dan.gimtrackerbackend.repository;

import com.dan.gimtrackerbackend.model.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data repository for the events table.
 * Extending JpaRepository gives us standard CRUD operations without
 * writing SQL or implementation code by hand.
 */
public interface EventRepository extends JpaRepository<EventEntity, Long>
{
    /**
     * Returns all events for one group, oldest event-time first.
     * Spring generates the SQL from the method name.
     */
    List<EventEntity> findByGroupCodeOrderByEventTimeAsc(String groupCode);
}
