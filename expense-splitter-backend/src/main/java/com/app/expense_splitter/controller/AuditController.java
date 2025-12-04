package com.app.expense_splitter.controller;

import com.app.expense_splitter.model.entity.AuditLog;
import com.app.expense_splitter.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<AuditLog>> getGroupHistory(@PathVariable Long groupId) {
        return ResponseEntity.ok(auditLogRepository.findByGroupIdOrderByTimestampDesc(groupId));
    }
}