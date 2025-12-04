package com.app.expense_splitter.service;

import com.app.expense_splitter.model.dto.expense.AddExpenseRequest;
import com.app.expense_splitter.model.dto.expense.AddExpenseResponse;
import com.app.expense_splitter.model.dto.settlement.BalanceResponse;
import com.app.expense_splitter.model.dto.settlement.GroupBalanceResponse;
import com.app.expense_splitter.model.dto.settlement.SettlementRequest;
import com.app.expense_splitter.model.entity.*;
import com.app.expense_splitter.model.enums.SplitType;
import com.app.expense_splitter.repository.AuditLogRepository;
import com.app.expense_splitter.repository.ExpenseRepository;
import com.app.expense_splitter.repository.GroupRepository;
import com.app.expense_splitter.repository.SettlementRepository;
import com.app.expense_splitter.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final SettlementRepository settlementRepository;
    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public AddExpenseResponse addExpense(AddExpenseRequest req) {

        ExpenseGroup group = groupRepository.findById(req.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        User payer = userRepository.findById(req.getPaidBy())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Validate participants belong to group
        for (Long uid : req.getParticipants()) {
            boolean isMember = group.getMembers().stream().anyMatch(u -> u.getId().equals(uid));
            if (!isMember) {
                throw new IllegalArgumentException("User " + uid + " is not a member of group " + group.getId());
            }
        }

        // Convert double to BigDecimal for calculation
        BigDecimal totalAmount = BigDecimal.valueOf(req.getAmount());

        Expense expense = new Expense();
        expense.setTitle(req.getTitle());
        expense.setAmount(totalAmount);
        expense.setGroup(group);
        expense.setPaidBy(payer);
        expense.setSplitType(req.getSplitType());

        List<ExpenseShare> shares = new ArrayList<>();

        if (req.getSplitType() == SplitType.EQUAL) {
            // Penny Problem Logic
            BigDecimal numberOfParticipants = new BigDecimal(req.getParticipants().size());
            BigDecimal splitAmount = totalAmount.divide(numberOfParticipants, 2, RoundingMode.DOWN);
            BigDecimal totalCalculated = splitAmount.multiply(numberOfParticipants);
            BigDecimal remainder = totalAmount.subtract(totalCalculated);

            for (int i = 0; i < req.getParticipants().size(); i++) {
                Long uid = req.getParticipants().get(i);
                User user = userRepository.findById(uid).orElseThrow();

                ExpenseShare share = new ExpenseShare();
                share.setUser(user);
                share.setExpense(expense);

                if (i == 0) {
                    share.setAmount(splitAmount.add(remainder));
                } else {
                    share.setAmount(splitAmount);
                }
                shares.add(share);
            }

        } else if (req.getSplitType() == SplitType.EXACT) {
            BigDecimal sumCheck = BigDecimal.ZERO;
            for (int i = 0; i < req.getParticipants().size(); i++) {
                Long uid = req.getParticipants().get(i);
                BigDecimal amount = BigDecimal.valueOf(req.getExactAmounts().get(i));

                User user = userRepository.findById(uid).orElseThrow();

                ExpenseShare share = new ExpenseShare();
                share.setUser(user);
                share.setAmount(amount);
                share.setExpense(expense);
                shares.add(share);

                sumCheck = sumCheck.add(amount);
            }

            if (sumCheck.compareTo(totalAmount) != 0) {
                throw new IllegalArgumentException("The sum of exact splits (" + sumCheck + ") does not equal the total amount (" + totalAmount + ")");
            }
        }

        expense.setShares(shares);
        Expense saved = expenseRepository.save(expense);
        String formattedDetails = String.format("%s|||%.2f|||%s|||%d",
                req.getTitle(),
                req.getAmount(),
                payer.getName(),
                saved.getId());

        com.app.expense_splitter.model.entity.AuditLog logEntry = new com.app.expense_splitter.model.entity.AuditLog(
                "EXPENSE",
                group.getId(),
                payer.getEmail(),
                formattedDetails
        );

        auditLogRepository.save(logEntry);

        return mapToResponse(saved);
    }

    @Override
    public GroupBalanceResponse calculateGroupBalance(Long groupId) {
        ExpenseGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // 1. Initialize balances map (User ID -> Net Balance)
        Map<Long, BigDecimal> netBalances = new HashMap<>();
        for (User u : group.getMembers()) {
            netBalances.put(u.getId(), BigDecimal.ZERO);
        }

        // 2. Process Expenses
        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        for (Expense exp : expenses) {
            // Payer gets Credit (+)
            Long payerId = exp.getPaidBy().getId();
            netBalances.put(payerId, netBalances.getOrDefault(payerId, BigDecimal.ZERO).add(exp.getAmount()));

            // Participants get Debit (-)
            for (ExpenseShare share : exp.getShares()) {
                Long userId = share.getUser().getId();
                netBalances.put(userId, netBalances.getOrDefault(userId, BigDecimal.ZERO).subtract(share.getAmount()));
            }
        }

        // 3. Process Settlements (Existing payments)
        // If A paid B $50: A gets +50 (debt cleared/credit added), B gets -50 (credit used/debt paid)
        // 2. Process Settlements (Existing Payments)
        List<Settlement> settlements = settlementRepository.findByGroupId(groupId);
        for (Settlement s : settlements) {
            Long payerId = s.getFromUser().getId();
            Long receiverId = s.getToUser().getId();

            // FIX: s.getAmount() is already BigDecimal, so just assign it.
            BigDecimal amount = s.getAmount();

            // Logic:
            // Payer (Debtor) pays money -> Debt reduces (Balance increases towards 0)
            netBalances.put(payerId, netBalances.getOrDefault(payerId, BigDecimal.ZERO).add(amount));

            // Receiver (Creditor) gets money -> Credit reduces (Balance decreases towards 0)
            netBalances.put(receiverId, netBalances.getOrDefault(receiverId, BigDecimal.ZERO).subtract(amount));
        }

        // 4. Create Response List
        List<BalanceResponse> balanceList = group.getMembers().stream().map(user -> {
            BalanceResponse dto = new BalanceResponse();
            dto.setUserId(user.getId());
            dto.setName(user.getName());
            // Convert BigDecimal back to double for JSON response
            dto.setNetBalance(netBalances.get(user.getId()).doubleValue());
            return dto;
        }).collect(Collectors.toList());

        // 5. Calculate Suggested Settlements (Greedy Algorithm)
        List<SettlementRequest> suggestedSettlements = computeSettlements(netBalances);

        GroupBalanceResponse response = new GroupBalanceResponse();
        response.setGroupId(groupId);
        response.setBalances(balanceList);
        response.setSettlements(suggestedSettlements);

        return response;
    }

    private List<SettlementRequest> computeSettlements(Map<Long, BigDecimal> netBalances) {
        List<SettlementRequest> result = new ArrayList<>();

        // Helper class to sort balances
        class BalanceNode {
            Long userId;
            BigDecimal amount;
            BalanceNode(Long id, BigDecimal amt) { this.userId = id; this.amount = amt; }
        }

        List<BalanceNode> debtors = new ArrayList<>();
        List<BalanceNode> creditors = new ArrayList<>();

        // Separate users into Debtors (-) and Creditors (+)
        for (Map.Entry<Long, BigDecimal> entry : netBalances.entrySet()) {
            // We ignore zero balances or very small fractions
            if (entry.getValue().compareTo(new BigDecimal("-0.01")) < 0) {
                debtors.add(new BalanceNode(entry.getKey(), entry.getValue()));
            } else if (entry.getValue().compareTo(new BigDecimal("0.01")) > 0) {
                creditors.add(new BalanceNode(entry.getKey(), entry.getValue()));
            }
        }

        // Greedy matching
        int i = 0; // debtor index
        int j = 0; // creditor index

        while (i < debtors.size() && j < creditors.size()) {
            BalanceNode debtor = debtors.get(i);
            BalanceNode creditor = creditors.get(j);

            // Find minimum of |debtor| or creditor
            // Example: Debtor -50, Creditor 40. We settle 40.
            // Example: Debtor -30, Creditor 60. We settle 30.
            BigDecimal amount = debtor.amount.abs().min(creditor.amount);

            // Record settlement
            SettlementRequest req = new SettlementRequest();
            req.setFromUser(debtor.userId);
            req.setToUser(creditor.userId);
            req.setAmount(amount.doubleValue());
            result.add(req);

            // Update remaining amounts
            debtor.amount = debtor.amount.add(amount);      // -50 + 40 = -10
            creditor.amount = creditor.amount.subtract(amount); // 40 - 40 = 0

            // If settled, move to next person
            // Use compareTo to handle precision safety
            if (debtor.amount.abs().compareTo(new BigDecimal("0.01")) < 0) i++;
            if (creditor.amount.abs().compareTo(new BigDecimal("0.01")) < 0) j++;
        }

        return result;
    }

    private AddExpenseResponse mapToResponse(Expense expense) {
        AddExpenseResponse res = new AddExpenseResponse();
        res.setExpenseId(expense.getId());
        res.setTitle(expense.getTitle());
        res.setAmount(expense.getAmount().doubleValue());
        res.setGroupId(expense.getGroup().getId());
        res.setPaidBy(expense.getPaidBy().getId());
        res.setSplitType(expense.getSplitType().name());

        List<AddExpenseResponse.ShareDto> shareDtos = expense.getShares().stream().map(share -> {
            AddExpenseResponse.ShareDto dto = new AddExpenseResponse.ShareDto();
            dto.setUserId(share.getUser().getId());
            dto.setUserName(share.getUser().getName());
            dto.setAmount(share.getAmount().doubleValue());
            // dto.setSettled(share.getSettled()); // Removed settled logic
            return dto;
        }).collect(Collectors.toList());

        res.setShares(shareDtos);
        return res;
    }
    // 1. ADD THIS METHOD
    @Override
    public AddExpenseResponse getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));
        return mapToResponse(expense);
    }
}