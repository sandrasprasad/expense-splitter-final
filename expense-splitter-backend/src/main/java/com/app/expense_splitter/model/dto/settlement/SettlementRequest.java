package com.app.expense_splitter.model.dto.settlement;

import lombok.Data;

@Data
public class SettlementRequest {

    private Long groupId;
    private Long fromUser;
    private Long toUser;
    private double amount;

}
