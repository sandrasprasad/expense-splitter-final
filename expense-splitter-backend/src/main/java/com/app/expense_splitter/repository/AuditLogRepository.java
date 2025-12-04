package com.app.expense_splitter.repository;

import com.app.expense_splitter.model.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    // Allows you to fetch history for a specific group
    List<AuditLog> findByGroupIdOrderByTimestampDesc(Long groupId);
}