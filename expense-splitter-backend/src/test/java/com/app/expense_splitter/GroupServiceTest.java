package com.app.expense_splitter;

import com.app.expense_splitter.model.dto.group.CreateGroupRequest;
import com.app.expense_splitter.model.entity.ExpenseGroup;
import com.app.expense_splitter.model.entity.User;
import com.app.expense_splitter.repository.GroupRepository;
import com.app.expense_splitter.repository.UserRepository;
import com.app.expense_splitter.service.GroupServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GroupServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private GroupRepository groupRepository;

    @InjectMocks
    private GroupServiceImpl groupService;

    @Test
    public void testCreateGroup_Success() {
        // Arrange
        String email = "john@example.com";
        CreateGroupRequest req = new CreateGroupRequest();
        req.setName("Goa Trip");

        User creator = new User();
        creator.setId(1L);
        creator.setEmail(email);

        // Mock User Fetch
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(creator));

        // Mock Group Save
        when(groupRepository.save(any(ExpenseGroup.class))).thenAnswer(invocation -> {
            ExpenseGroup g = invocation.getArgument(0);
            g.setId(100L);
            return g;
        });

        // Act
        ExpenseGroup result = groupService.createGroup(req, email);

        // Assert
        assertNotNull(result);
        assertEquals("Goa Trip", result.getName());
        assertEquals(1L, result.getCreatedBy());

        // Verify interactions
        verify(userRepository).findByEmail(email);
        verify(groupRepository).save(any(ExpenseGroup.class));
    }
}