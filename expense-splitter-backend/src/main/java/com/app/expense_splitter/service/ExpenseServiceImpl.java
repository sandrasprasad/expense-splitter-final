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

        for (Long uid : req.getParticipants()) {
            boolean isMember = group.getMembers().stream().anyMatch(u -> u.getId().equals(uid));
            if (!isMember) {
                throw new IllegalArgumentException("User " + uid + " is not a member of group " + group.getId());
            }
        }

        BigDecimal totalAmount = BigDecimal.valueOf(req.getAmount());

        Expense expense = new Expense();
        expense.setTitle(req.getTitle());
        expense.setAmount(totalAmount);
        expense.setGroup(group);
        expense.setPaidBy(payer);
        expense.setSplitType(req.getSplitType());

        List<ExpenseShare> shares = new ArrayList<>();

        if (req.getSplitType() == SplitType.EQUAL) {
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

        Map<Long, BigDecimal> netBalances = new HashMap<>();
        for (User u : group.getMembers()) {
            netBalances.put(u.getId(), BigDecimal.ZERO);
        }
        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        for (Expense exp : expenses) {
            Long payerId = exp.getPaidBy().getId();
            netBalances.put(payerId, netBalances.getOrDefault(payerId, BigDecimal.ZERO).add(exp.getAmount()));

            for (ExpenseShare share : exp.getShares()) {
                Long userId = share.getUser().getId();
                netBalances.put(userId, netBalances.getOrDefault(userId, BigDecimal.ZERO).subtract(share.getAmount()));
            }
        }

        List<Settlement> settlements = settlementRepository.findByGroupId(groupId);
        for (Settlement s : settlements) {
            Long payerId = s.getFromUser().getId();
            Long receiverId = s.getToUser().getId();

            BigDecimal amount = s.getAmount();
            netBalances.put(payerId, netBalances.getOrDefault(payerId, BigDecimal.ZERO).add(amount));
            netBalances.put(receiverId, netBalances.getOrDefault(receiverId, BigDecimal.ZERO).subtract(amount));
        }

        List<BalanceResponse> balanceList = group.getMembers().stream().map(user -> {
            BalanceResponse dto = new BalanceResponse();
            dto.setUserId(user.getId());
            dto.setName(user.getName());
            dto.setNetBalance(netBalances.get(user.getId()).doubleValue());
            return dto;
        }).collect(Collectors.toList());

        List<SettlementRequest> suggestedSettlements = computeSettlements(netBalances);

        GroupBalanceResponse response = new GroupBalanceResponse();
        response.setGroupId(groupId);
        response.setBalances(balanceList);
        response.setSettlements(suggestedSettlements);

        return response;
    }

    private List<SettlementRequest> computeSettlements(Map<Long, BigDecimal> netBalances) {
        List<SettlementRequest> result = new ArrayList<>();

        class BalanceNode {
            Long userId;
            BigDecimal amount;
            BalanceNode(Long id, BigDecimal amt) { this.userId = id; this.amount = amt; }
        }

        List<BalanceNode> debtors = new ArrayList<>();
        List<BalanceNode> creditors = new ArrayList<>();

        for (Map.Entry<Long, BigDecimal> entry : netBalances.entrySet()) {
            if (entry.getValue().compareTo(new BigDecimal("-0.01")) < 0) {
                debtors.add(new BalanceNode(entry.getKey(), entry.getValue()));
            } else if (entry.getValue().compareTo(new BigDecimal("0.01")) > 0) {
                creditors.add(new BalanceNode(entry.getKey(), entry.getValue()));
            }
        }

        int i = 0;
        int j = 0;

        while (i < debtors.size() && j < creditors.size()) {
            BalanceNode debtor = debtors.get(i);
            BalanceNode creditor = creditors.get(j);

            BigDecimal amount = debtor.amount.abs().min(creditor.amount);

            SettlementRequest req = new SettlementRequest();
            req.setFromUser(debtor.userId);
            req.setToUser(creditor.userId);
            req.setAmount(amount.doubleValue());
            result.add(req);

            debtor.amount = debtor.amount.add(amount);
            creditor.amount = creditor.amount.subtract(amount);

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
            return dto;
        }).collect(Collectors.toList());

        res.setShares(shareDtos);
        return res;
    }
    @Override
    public AddExpenseResponse getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));
        return mapToResponse(expense);
    }
}