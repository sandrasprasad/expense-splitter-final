package com.app.expense_splitter.service;

import com.app.expense_splitter.model.dto.settlement.SettlementRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Min-Cash Flow debt simplification.
 *
 * Each member's net balance is a node in a settlement graph: positive means the member is owed
 * money, negative means the member owes money. Instead of settling every expense edge-by-edge,
 * the solver repeatedly pairs the largest creditor with the largest debtor and settles
 * min(|debt|, credit). That clears at least one member per transaction, so a group of n members
 * with non-zero balances needs at most n - 1 payments. Runs in O(n log n).
 */
public final class MinCashFlowSolver {

    private static final BigDecimal EPSILON = new BigDecimal("0.01");

    private MinCashFlowSolver() {
    }

    private static final class Node {
        final Long userId;
        BigDecimal amount;

        Node(Long userId, BigDecimal amount) {
            this.userId = userId;
            this.amount = amount;
        }
    }

    public static List<SettlementRequest> solve(Long groupId, Map<Long, BigDecimal> netBalances) {
        PriorityQueue<Node> creditors = new PriorityQueue<>(Comparator.comparing((Node n) -> n.amount).reversed());
        PriorityQueue<Node> debtors = new PriorityQueue<>(Comparator.comparing((Node n) -> n.amount.abs()).reversed());

        for (Map.Entry<Long, BigDecimal> entry : netBalances.entrySet()) {
            BigDecimal balance = entry.getValue();
            if (balance.compareTo(EPSILON.negate()) < 0) {
                debtors.add(new Node(entry.getKey(), balance));
            } else if (balance.compareTo(EPSILON) > 0) {
                creditors.add(new Node(entry.getKey(), balance));
            }
        }

        List<SettlementRequest> payments = new ArrayList<>();
        while (!debtors.isEmpty() && !creditors.isEmpty()) {
            Node debtor = debtors.poll();
            Node creditor = creditors.poll();

            BigDecimal amount = debtor.amount.abs().min(creditor.amount);

            SettlementRequest payment = new SettlementRequest();
            payment.setGroupId(groupId);
            payment.setFromUser(debtor.userId);
            payment.setToUser(creditor.userId);
            payment.setAmount(amount.doubleValue());
            payments.add(payment);

            debtor.amount = debtor.amount.add(amount);
            creditor.amount = creditor.amount.subtract(amount);

            if (debtor.amount.abs().compareTo(EPSILON) >= 0) {
                debtors.add(debtor);
            }
            if (creditor.amount.compareTo(EPSILON) >= 0) {
                creditors.add(creditor);
            }
        }
        return payments;
    }
}
