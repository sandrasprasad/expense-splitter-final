package com.app.expense_splitter.controller;

import com.app.expense_splitter.model.dto.group.AddMemberRequest;
import com.app.expense_splitter.model.dto.group.CreateGroupRequest;
import com.app.expense_splitter.model.dto.group.CreateGroupResponse;
import com.app.expense_splitter.model.dto.group.GroupDetailsResponse;
import com.app.expense_splitter.model.dto.group.GroupMembers;
import com.app.expense_splitter.model.dto.settlement.GroupBalanceResponse;
import com.app.expense_splitter.model.entity.ExpenseGroup;
import com.app.expense_splitter.service.ExpenseService;
import com.app.expense_splitter.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    @Autowired
    private GroupService groupService;

    @Autowired
    private ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<CreateGroupResponse> createGroup(
            @RequestBody @Valid CreateGroupRequest request,
            java.security.Principal principal
    ) {
        // SECURITY IMPROVEMENT: Get ID from the token, not the user input
        // Assuming principal.getName() returns the email
        ExpenseGroup created = groupService.createGroup(request, principal.getName());
        return ResponseEntity.ok(mapToGroupResponse(created));
    }

    @PostMapping("/{groupId}/add-member")
    public ResponseEntity<CreateGroupResponse> addMember(
            @PathVariable Long groupId,
            @RequestBody @Valid AddMemberRequest req
    ) {
        ExpenseGroup group = groupService.addMember(groupId, req.getEmail());
        return ResponseEntity.ok(mapToGroupResponse(group));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDetailsResponse> getGroupDetails(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.getGroupDetails(groupId));
    }

    @GetMapping("/{groupId}/balances")
    public ResponseEntity<GroupBalanceResponse> getGroupBalance(@PathVariable Long groupId) {
        return ResponseEntity.ok(expenseService.calculateGroupBalance(groupId));
    }

    // Extracted Mapper Method
    private CreateGroupResponse mapToGroupResponse(ExpenseGroup group) {
        CreateGroupResponse resp = new CreateGroupResponse();
        resp.setId(group.getId());
        resp.setName(group.getName());
        resp.setMembers(
                group.getMembers().stream().map(u -> {
                    GroupMembers dto = new GroupMembers();
                    dto.setId(u.getId());
                    dto.setName(u.getName());
                    dto.setEmail(u.getEmail());
                    return dto;
                }).toList()
        );
        return resp;
    }
    @GetMapping("/search")
    public ResponseEntity<List<GroupDetailsResponse>> searchGroups(@RequestParam String name) {
        return ResponseEntity.ok(groupService.searchGroups(name));
    }
    @GetMapping("/my-groups")
    public ResponseEntity<List<GroupDetailsResponse>> getUserGroups(java.security.Principal principal) {
        return ResponseEntity.ok(groupService.getUserGroups(principal.getName()));
    }
    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long groupId, @PathVariable Long memberId) {
        groupService.removeMember(groupId, memberId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long groupId) {
        groupService.deleteGroup(groupId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{groupId}/export")
    public ResponseEntity<String> exportGroup(@PathVariable Long groupId) {
        String csvData = groupService.generateCsvExport(groupId);

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"group_export.csv\"")
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "text/csv")
                .body(csvData);
    }
}