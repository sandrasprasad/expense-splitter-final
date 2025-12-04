package com.app.expense_splitter.service;

import com.app.expense_splitter.model.dto.settlement.SettlementRequest;
import com.app.expense_splitter.model.dto.settlement.SettlementResponse;
import com.app.expense_splitter.model.entity.Settlement;

import java.util.List;

public interface SettlementService {
    SettlementResponse recordSettlement(SettlementRequest req);
    List<SettlementResponse> getSettlementsForGroup(Long groupId);
}
