package com.app.expense_splitter.controller;

import com.app.expense_splitter.model.dto.settlement.SettlementRequest;
import com.app.expense_splitter.model.dto.settlement.SettlementResponse;
import com.app.expense_splitter.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping
    public ResponseEntity<SettlementResponse> record(@RequestBody SettlementRequest request) {
        return ResponseEntity.ok(settlementService.recordSettlement(request));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<List<SettlementResponse>> list(@PathVariable Long groupId) {
        return ResponseEntity.ok(settlementService.getSettlementsForGroup(groupId));
    }
}