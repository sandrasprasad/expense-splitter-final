package com.app.expense_splitter.model.dto.settlement;

import lombok.Data;

@Data
public class BalanceResponse {
    private Long userId;
    private String name;
    private double netBalance;// +ve = gets money, -ve = owes money

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getNetBalance() {
        return netBalance;
    }

    public void setNetBalance(double netBalance) {
        this.netBalance = netBalance;
    }
}
