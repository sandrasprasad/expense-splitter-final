package com.app.expense_splitter.model.dto.settlement;

import lombok.Data;

@Data
public class BalanceResponse {
    private Long userId;
    private String name;
    private double netBalance;

}
