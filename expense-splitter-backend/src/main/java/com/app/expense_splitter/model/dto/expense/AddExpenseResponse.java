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

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public boolean isSettled() {
            return settled;
        }

        public void setSettled(boolean settled) {
            this.settled = settled;
        }
    }

    public Long getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(Long expenseId) {
        this.expenseId = expenseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getPaidBy() {
        return paidBy;
    }

    public void setPaidBy(Long paidBy) {
        this.paidBy = paidBy;
    }

    public String getSplitType() {
        return splitType;
    }

    public void setSplitType(String splitType) {
        this.splitType = splitType;
    }

    public List<ShareDto> getShares() {
        return shares;
    }

    public void setShares(List<ShareDto> shares) {
        this.shares = shares;
    }
}
