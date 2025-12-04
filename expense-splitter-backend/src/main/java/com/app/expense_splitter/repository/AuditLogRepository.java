package com.app.expense_splitter.repository;

import com.app.expense_splitter.model.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByGroupIdOrderByTimestampDesc(Long groupId);
}