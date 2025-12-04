package com.app.expense_splitter;

import com.app.expense_splitter.controller.GroupController;
import com.app.expense_splitter.model.dto.group.GroupDetailsResponse;
import com.app.expense_splitter.service.GroupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GroupController.class)
@AutoConfigureMockMvc(addFilters = false) // Bypass Security for Unit Test
public class GroupControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private GroupService groupService;
    // Mock other dependencies if Controller requires them (e.g., ExpenseService)
    @MockBean private com.app.expense_splitter.service.ExpenseService expenseService;

    @Test
    public void testSearchGroups_ReturnsList() throws Exception {
        // 1. Arrange
        GroupDetailsResponse g1 = new GroupDetailsResponse(); g1.setId(1L); g1.setName("Goa Trip");
        GroupDetailsResponse g2 = new GroupDetailsResponse(); g2.setId(2L); g2.setName("Goa Hotel");
        List<GroupDetailsResponse> mockResults = Arrays.asList(g1, g2);

        when(groupService.searchGroups("Goa")).thenReturn(mockResults);

        // 2. Act & Assert
        mockMvc.perform(get("/api/groups/search")
                        .param("name", "Goa")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Goa Trip"));
    }
}