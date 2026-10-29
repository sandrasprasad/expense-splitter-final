package com.app.expense_splitter;

import com.app.expense_splitter.model.dto.settlement.SettlementRequest;
import com.app.expense_splitter.service.MinCashFlowSolver;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinCashFlowSolverTest {

    private static Map<Long, BigDecimal> balances(double... values) {
        Map<Long, BigDecimal> map = new HashMap<>();
        for (int i = 0; i < values.length; i++) {
            map.put((long) (i + 1), BigDecimal.valueOf(values[i]));
        }
        return map;
    }

    /** Applying every payment must bring all balances back to zero. */
    private static void assertFullySettled(Map<Long, BigDecimal> start, List<SettlementRequest> payments) {
        Map<Long, BigDecimal> remaining = new HashMap<>(start);
        for (SettlementRequest p : payments) {
            remaining.merge(p.getFromUser(), BigDecimal.valueOf(p.getAmount()), BigDecimal::add);
            remaining.merge(p.getToUser(), BigDecimal.valueOf(p.getAmount()).negate(), BigDecimal::add);
        }
        remaining.values().forEach(v -> assertTrue(v.abs().compareTo(new BigDecimal("0.01")) < 0, "unsettled: " + v));
    }

    @Test
    void settledGroupNeedsNoPayments() {
        assertTrue(MinCashFlowSolver.solve(1L, balances(0, 0, 0)).isEmpty());
    }

    @Test
    void twoPeopleSettleWithOnePayment() {
        List<SettlementRequest> payments = MinCashFlowSolver.solve(1L, balances(40, -40));
        assertEquals(1, payments.size());
        assertEquals(2L, payments.get(0).getFromUser());
        assertEquals(1L, payments.get(0).getToUser());
        assertEquals(40.0, payments.get(0).getAmount());
    }

    @Test
    void chainOfDebtsCollapsesToMinimumPayments() {
        // A owes B 100 and B owes C 100 is the same as A paying C 100: one payment, not two.
        Map<Long, BigDecimal> net = balances(-100, 0, 100);
        List<SettlementRequest> payments = MinCashFlowSolver.solve(1L, net);
        assertEquals(1, payments.size());
        assertFullySettled(net, payments);
    }

    @Test
    void paymentsNeverExceedMembersMinusOne() {
        Map<Long, BigDecimal> net = balances(70, 30, -20, -50, -10, -20);
        List<SettlementRequest> payments = MinCashFlowSolver.solve(7L, net);
        assertTrue(payments.size() <= net.size() - 1);
        assertFullySettled(net, payments);
        payments.forEach(p -> assertEquals(7L, p.getGroupId()));
    }

    @Test
    void ignoresRoundingDust() {
        assertTrue(MinCashFlowSolver.solve(1L, balances(0.004, -0.004)).isEmpty());
    }
}
