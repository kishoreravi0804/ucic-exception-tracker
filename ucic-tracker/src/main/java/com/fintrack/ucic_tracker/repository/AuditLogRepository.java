package com.fintrack.ucic_tracker.repository;

import com.fintrack.ucic_tracker.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByExceptionGroupIdOrderByCreatedAtAsc(Long groupId);
}
