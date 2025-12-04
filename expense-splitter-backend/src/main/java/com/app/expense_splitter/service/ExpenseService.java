package com.app.expense_splitter.service;

import com.app.expense_splitter.model.dto.expense.AddExpenseRequest;
import com.app.expense_splitter.model.dto.expense.AddExpenseResponse;
import com.app.expense_splitter.model.dto.settlement.GroupBalanceResponse;
import com.app.expense_splitter.model.entity.Expense;

public interface ExpenseService {

    AddExpenseResponse addExpense(AddExpenseRequest request);
    GroupBalanceResponse calculateGroupBalance(Long groupId);
    AddExpenseResponse getExpenseById(Long id);

    // Expense editExpense(Long id, EditExpenseRequest request);
    // void deleteExpense(Long id);
    // List<Expense> getGroupExpenses(Long groupId);
}
