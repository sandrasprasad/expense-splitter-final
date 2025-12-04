package com.app.expense_splitter.model.dto.settlement;

import lombok.Data;
import java.util.List;

@Data
public class GroupBalanceResponse {

    private Long groupId;
    private List<BalanceResponse> balances;
    private List<SettlementRequest> settlements;

}
