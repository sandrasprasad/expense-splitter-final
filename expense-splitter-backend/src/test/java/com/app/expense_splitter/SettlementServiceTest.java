package com.app.expense_splitter;

import com.app.expense_splitter.model.dto.settlement.SettlementRequest;
import com.app.expense_splitter.model.entity.*;
import com.app.expense_splitter.repository.*;
import com.app.expense_splitter.service.SettlementServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SettlementServiceTest {

    @Mock private SettlementRepository settlementRepository;
    @Mock private UserRepository userRepository;
    @Mock private GroupRepository groupRepository;
    @Mock private AuditLogRepository auditLogRepository; // <--- ADDED THIS

    @InjectMocks
    private SettlementServiceImpl settlementService;

    @Test
    public void recordSettlement_ShouldSaveLog() {
        // Arrange
        User u1 = new User(); u1.setId(1L); u1.setName("Alice"); u1.setEmail("a@a.com");
        User u2 = new User(); u2.setId(2L); u2.setName("Bob"); u2.setEmail("b@b.com");

        ExpenseGroup group = new ExpenseGroup();
        group.setId(10L);
        // Important: Add users to group to pass validation
        group.setMembers(new HashSet<>(Set.of(u1, u2)));

        when(groupRepository.findById(10L)).thenReturn(Optional.of(group));
        when(userRepository.findById(1L)).thenReturn(Optional.of(u1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(u2));

        when(settlementRepository.save(any(Settlement.class))).thenAnswer(i -> {
            Settlement s = i.getArgument(0);
            s.setId(99L);
            // Fix: ensure timestamp is set if your entity logic relies on @PrePersist which doesn't run in mocks
            s.setTimestamp(java.time.LocalDateTime.now());
            return s;
        });

        SettlementRequest req = new SettlementRequest();
        req.setGroupId(10L); req.setFromUser(1L); req.setToUser(2L); req.setAmount(50.0);

        // Act
        settlementService.recordSettlement(req);

        // Assert
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }
}