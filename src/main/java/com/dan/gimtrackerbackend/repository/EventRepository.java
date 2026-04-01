package com.dan.gimtrackerbackend.repository;

import com.dan.gimtrackerbackend.model.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository for the events table.
 * Extending JpaRepository gives us standard CRUD operations without
 * writing SQL or implementation code by hand.
 */
public interface EventRepository extends JpaRepository<EventEntity, Long>
{
}
