package com.app.expense_splitter;

import com.app.expense_splitter.controller.ExpenseController;
import com.app.expense_splitter.model.dto.expense.AddExpenseRequest;
import com.app.expense_splitter.model.dto.expense.AddExpenseResponse;
import com.app.expense_splitter.service.ExpenseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = ExpenseController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ExpenseService expenseService;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void postAddExpense_returnsResponse() throws Exception {
        AddExpenseRequest req = new AddExpenseRequest();
        req.setGroupId(1L);
        req.setTitle("Lunch");
        req.setAmount(1200.0);
        req.setPaidBy(1L);
        req.setSplitType(com.app.expense_splitter.model.enums.SplitType.EQUAL);
        req.setParticipants(List.of(1L,2L,3L));

        AddExpenseResponse resp = new AddExpenseResponse();
        resp.setExpenseId(5L);
        resp.setTitle("Lunch");
        resp.setAmount(1200.0);

        when(expenseService.addExpense(any())).thenReturn(resp);

        mvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expenseId").value(5));
    }
}
