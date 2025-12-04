package com.app.expense_splitter.model.dto.settlement;

import lombok.Data;
import java.util.List;

@Data
public class GroupBalanceResponse {

    private Long groupId;
    private List<BalanceResponse> balances;
    private List<SettlementRequest> settlements;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public List<BalanceResponse> getBalances() {
        return balances;
    }

    public void setBalances(List<BalanceResponse> balances) {
        this.balances = balances;
    }

    public List<SettlementRequest> getSettlements() {
        return settlements;
    }

    public void setSettlements(List<SettlementRequest> settlements) {
        this.settlements = settlements;
    }
}
