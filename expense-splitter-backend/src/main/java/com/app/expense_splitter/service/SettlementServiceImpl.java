package com.app.expense_splitter.service;

import com.app.expense_splitter.model.dto.settlement.SettlementRequest;
import com.app.expense_splitter.model.dto.settlement.SettlementResponse;
import com.app.expense_splitter.model.entity.AuditLog;
import com.app.expense_splitter.model.entity.ExpenseGroup;
import com.app.expense_splitter.model.entity.Settlement;
import com.app.expense_splitter.model.entity.User;
import com.app.expense_splitter.repository.AuditLogRepository;
import com.app.expense_splitter.repository.GroupRepository;
import com.app.expense_splitter.repository.SettlementRepository;
import com.app.expense_splitter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Import for Audit Trail
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j // Enables 'log.info' for the Audit Trail requirement
public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public SettlementResponse recordSettlement(SettlementRequest req) {

        // 1. Fetch Entities
        ExpenseGroup group = groupRepository.findById(req.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        User fromUser = userRepository.findById(req.getFromUser())
                .orElseThrow(() -> new IllegalArgumentException("Payer (From) not found"));

        User toUser = userRepository.findById(req.getToUser())
                .orElseThrow(() -> new IllegalArgumentException("Receiver (To) not found"));

        // 2. Validate Membership (Defensive Programming)
        boolean isPayerInGroup = group.getMembers().stream().anyMatch(u -> u.getId().equals(fromUser.getId()));
        boolean isReceiverInGroup = group.getMembers().stream().anyMatch(u -> u.getId().equals(toUser.getId()));

        if (!isPayerInGroup || !isReceiverInGroup) {
            log.warn("Failed settlement attempt: Users not in group. GroupId: {}", group.getId());
            throw new IllegalArgumentException("Both users must be members of the group to settle up.");
        }

        // 3. Create Settlement
        Settlement settlement = new Settlement();
        settlement.setGroup(group);
        settlement.setFromUser(fromUser);
        settlement.setToUser(toUser);
        settlement.setAmount(BigDecimal.valueOf(req.getAmount())); // Convert double to BigDecimal

        // 4. Save
        Settlement saved = settlementRepository.save(settlement);

        // 5. AUDIT TRAIL LOGGING
        log.info("AUDIT: Settlement recorded. ID: {}, Group: {}, {} paid {} amount: {}",
                saved.getId(), group.getName(), fromUser.getEmail(), toUser.getEmail(), req.getAmount());

        String description = String.format("%s paid %s %.2f", fromUser.getName(), toUser.getName(), req.getAmount());
        AuditLog audit = new AuditLog(
                "SETTLEMENT",
                group.getId(),
                fromUser.getEmail(),
                description
        );
        auditLogRepository.save(audit);

        return mapToResponse(saved);
    }

    @Override
    public List<SettlementResponse> getSettlementsForGroup(Long groupId) {
        // Return DTOs, not Entities, to prevent JSON recursion issues
        return settlementRepository.findByGroupId(groupId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Helper to map Entity -> DTO
    private SettlementResponse mapToResponse(Settlement s) {
        SettlementResponse resp = new SettlementResponse();
        resp.setId(s.getId());
        resp.setGroupId(s.getGroup().getId());
        resp.setFromUser(s.getFromUser().getId());
        resp.setToUser(s.getToUser().getId());
        resp.setAmount(s.getAmount().doubleValue()); // Convert back for JSON
        resp.setTimestamp(s.getTimestamp());
        return resp;
    }
}