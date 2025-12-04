package com.app.expense_splitter.model.dto.expense;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AddExpenseResponse {

    private Long expenseId;
    private String title;
    private double amount;

    private Long groupId;
    private Long paidBy;

    private String splitType;

    private List<ShareDto> shares;

    @Getter
    @Setter
    public static class ShareDto {
        private Long userId;
        private String userName;
        private double amount;
        private boolean settled;
    }
}
