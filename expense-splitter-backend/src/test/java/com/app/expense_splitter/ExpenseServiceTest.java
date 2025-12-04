package com.app.expense_splitter.service;

import com.app.expense_splitter.model.dto.expense.AddExpenseRequest;
import com.app.expense_splitter.model.dto.expense.AddExpenseResponse;
import com.app.expense_splitter.model.entity.*;
import com.app.expense_splitter.model.enums.SplitType;
import com.app.expense_splitter.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock private ExpenseRepository expenseRepository;
    @Mock private UserRepository userRepository;
    @Mock private GroupRepository groupRepository;
    @Mock private AuditLogRepository auditLogRepository; // <--- ADDED THIS

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    @Test
    void addExpense_EqualSplit_SavesExpenseAndAuditLog() {
        // 1. DATA SETUP
        Long groupId = 1L;
        Long payerId = 100L;

        User payer = new User(); payer.setId(payerId); payer.setName("Alice"); payer.setEmail("alice@test.com");
        User user2 = new User(); user2.setId(101L); user2.setName("Bob");

        ExpenseGroup group = new ExpenseGroup();
        group.setId(groupId);
        group.setMembers(Set.of(payer, user2));

        AddExpenseRequest req = new AddExpenseRequest();
        req.setGroupId(groupId);
        req.setPaidBy(payerId);
        req.setAmount(100.0); // This is double in DTO, converted inside service
        req.setTitle("Dinner");
        req.setSplitType(SplitType.EQUAL);
        req.setParticipants(List.of(payerId, 101L));

        // 2. MOCK BEHAVIOR
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(payerId)).thenReturn(Optional.of(payer));
        when(userRepository.findById(101L)).thenReturn(Optional.of(user2));

        // Mock save to return the object with an ID
        when(expenseRepository.save(any(Expense.class))).thenAnswer(i -> {
            Expense e = i.getArgument(0);
            e.setId(555L);
            return e;
        });

        // 3. EXECUTE
        AddExpenseResponse response = expenseService.addExpense(req);

        // 4. ASSERTIONS
        assertNotNull(response);
        assertEquals(555L, response.getExpenseId());

        // Verify Audit Log was saved
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }
}