package com.app.expense_splitter.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class ExpenseShare {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "expense_share_seq")
    @SequenceGenerator(name = "expense_share_seq", sequenceName = "EXPENSE_SHARE_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "expense_id")
    private Expense expense;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // CHANGED: Use BigDecimal for precision
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    // Removed 'settled' boolean.
    // Logic Note: Individual expense shares are rarely "settled".
    // Users settle their *Net Balance*, not specific receipts.


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Expense getExpense() {
        return expense;
    }

    public void setExpense(Expense expense) {
        this.expense = expense;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}