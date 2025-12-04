package com.app.expense_splitter.model.dto.settlement;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class SettlementResponse {
    private Long id;
    private Long groupId;
    private Long fromUser;
    private Long toUser;
    private double amount;
    private LocalDateTime timestamp;

}

