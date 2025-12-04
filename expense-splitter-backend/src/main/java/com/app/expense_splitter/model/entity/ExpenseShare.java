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

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

}