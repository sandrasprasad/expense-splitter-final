package com.app.expense_splitter.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "AUDIT_LOGS")
@Getter @Setter @NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;       // e.g., "SETTLEMENT", "EXPENSE_ADDED"
    private Long groupId;        // Context
    private String performedBy;  // User Email or Name
    private String details;      // e.g., "Bob paid Alice 50.00"

    private LocalDateTime timestamp;

    public AuditLog(String action, Long groupId, String performedBy, String details) {
        this.action = action;
        this.groupId = groupId;
        this.performedBy = performedBy;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
}