package com.app.expense_splitter.model.dto.expense;

import com.app.expense_splitter.model.enums.SplitType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AddExpenseRequest {

    private Long groupId;
    private String title;
    private double amount;

    private Long paidBy;

    private SplitType splitType;

    private List<Long> participants;
    private List<Double> exactAmounts;

}
