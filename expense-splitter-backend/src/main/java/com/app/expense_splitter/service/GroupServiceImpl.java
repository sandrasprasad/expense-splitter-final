package com.app.expense_splitter.service;

import com.app.expense_splitter.model.dto.group.CreateGroupRequest;
import com.app.expense_splitter.model.dto.group.GroupDetailsResponse;
import com.app.expense_splitter.model.dto.group.GroupMembers;
import com.app.expense_splitter.model.entity.ExpenseGroup;
import com.app.expense_splitter.model.entity.User;
import com.app.expense_splitter.repository.AuditLogRepository;
import com.app.expense_splitter.repository.ExpenseRepository;
import com.app.expense_splitter.repository.GroupRepository;
import com.app.expense_splitter.repository.SettlementRepository;
import com.app.expense_splitter.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ExpenseService expenseService;
    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ExpenseRepository expenseRepository;
    @Autowired private SettlementRepository settlementRepository;


    @Override
    @Transactional
    public ExpenseGroup createGroup(CreateGroupRequest groupRequest, String creatorEmail) {
        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        ExpenseGroup expenseGroup = new ExpenseGroup();
        expenseGroup.setName(groupRequest.getName());
        expenseGroup.setCreatedBy(creator.getId());

        expenseGroup.setMembers(new HashSet<>());
        expenseGroup.getMembers().add(creator);

        return groupRepository.save(expenseGroup);
    }

    @Override
    @Transactional
    public ExpenseGroup addMember(Long groupId, String email) {
        ExpenseGroup expenseGroup = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User with email " + email + " not found"));

        expenseGroup.getMembers().add(user);
        return groupRepository.save(expenseGroup);
    }

    @Override
    @Transactional
    public void removeMember(Long groupId, Long memberId) {
        ExpenseGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        var balanceSheet = expenseService.calculateGroupBalance(groupId);
        var userBalance = balanceSheet.getBalances().stream()
                .filter(b -> b.getUserId().equals(memberId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User not found in balance sheet"));

        if (Math.abs(userBalance.getNetBalance()) > 0.1) {
            throw new IllegalStateException("Cannot remove member. They have an outstanding balance of " + userBalance.getNetBalance());
        }

        User userToRemove = userRepository.findById(memberId).orElseThrow();
        group.getMembers().remove(userToRemove);
        groupRepository.save(group);

        auditLogRepository.save(new com.app.expense_splitter.model.entity.AuditLog(
                "MEMBER_REMOVED", groupId, "System", "Removed member " + userToRemove.getName()
        ));
    }

    @Override
    @Transactional
    public void deleteGroup(Long groupId) {

        auditLogRepository.deleteAll(auditLogRepository.findByGroupIdOrderByTimestampDesc(groupId));
        settlementRepository.deleteAll(settlementRepository.findByGroupId(groupId));
        expenseRepository.deleteAll(expenseRepository.findByGroupId(groupId));

        groupRepository.deleteById(groupId);
    }

    @Override
    public String generateCsvExport(Long groupId) {
        ExpenseGroup group = groupRepository.findById(groupId).orElseThrow();
        List<com.app.expense_splitter.model.entity.Expense> expenses = expenseRepository.findByGroupId(groupId);

        StringBuilder csv = new StringBuilder();
        csv.append("Date,Description,Total Amount,Paid By,Split Type\n");

        for (var exp : expenses) {
            csv.append(String.format("%s,%s,%.2f,%s,%s\n",
                    LocalTime.now(),
                    exp.getTitle(),
                    exp.getAmount(),
                    exp.getPaidBy().getName(),
                    exp.getSplitType()
            ));
        }
        return csv.toString();
    }

    @Override
    public GroupDetailsResponse getGroupDetails(Long groupId) {
        ExpenseGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        GroupDetailsResponse response = new GroupDetailsResponse();
        response.setId(group.getId());
        response.setName(group.getName());

        List<GroupMembers> memberList = group.getMembers().stream()
                .map(user -> {
                    GroupMembers member = new GroupMembers();
                    member.setId(user.getId());
                    member.setName(user.getName());
                    member.setEmail(user.getEmail());
                    return member;
                })
                .toList();

        response.setMembers(memberList);
        return response;
    }
    @Override
    public List<GroupDetailsResponse> searchGroups(String name) {
        return groupRepository.findByNameContainingIgnoreCase(name).stream()
                .map(group -> {
                    GroupDetailsResponse resp = new GroupDetailsResponse();
                    resp.setId(group.getId());
                    resp.setName(group.getName());
                    return resp;
                }).toList();
    }
    @Override
    public List<GroupDetailsResponse> getUserGroups(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return groupRepository.findByMembers_Id(user.getId()).stream()
                .map(group -> {
                    GroupDetailsResponse resp = new GroupDetailsResponse();
                    resp.setId(group.getId());
                    resp.setName(group.getName());
                    return resp;
                }).toList();
    }
}