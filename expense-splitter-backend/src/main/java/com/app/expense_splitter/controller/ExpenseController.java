package com.app.expense_splitter.controller;

import com.app.expense_splitter.model.dto.expense.AddExpenseRequest;
import com.app.expense_splitter.model.dto.expense.AddExpenseResponse;
import com.app.expense_splitter.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    @Autowired
    private  ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<AddExpenseResponse> addExpense(@RequestBody AddExpenseRequest request) {
        return ResponseEntity.ok(expenseService.addExpense(request));
    }
    @GetMapping("/{id}")
    public ResponseEntity<AddExpenseResponse> getExpense(@PathVariable Long id) {
        // You'll need to add this method to ExpenseService interface & impl
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }

}
