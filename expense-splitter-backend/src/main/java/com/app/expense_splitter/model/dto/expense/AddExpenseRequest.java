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
    private List<Double> exactAmounts; // used only when splitType = EXACT

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
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

    public Long getPaidBy() {
        return paidBy;
    }

    public void setPaidBy(Long paidBy) {
        this.paidBy = paidBy;
    }

    public SplitType getSplitType() {
        return splitType;
    }

    public void setSplitType(SplitType splitType) {
        this.splitType = splitType;
    }

    public List<Long> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Long> participants) {
        this.participants = participants;
    }

    public List<Double> getExactAmounts() {
        return exactAmounts;
    }

    public void setExactAmounts(List<Double> exactAmounts) {
        this.exactAmounts = exactAmounts;
    }
}
