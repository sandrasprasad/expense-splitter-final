package com.app.expense_splitter.service;

import com.app.expense_splitter.model.dto.group.CreateGroupRequest;
import com.app.expense_splitter.model.dto.group.GroupDetailsResponse;
import com.app.expense_splitter.model.entity.ExpenseGroup;

import java.util.List;

public interface GroupService {

    ExpenseGroup createGroup(CreateGroupRequest groupRequest, String creatorId);
    ExpenseGroup addMember(Long GroupId, String email);
    GroupDetailsResponse getGroupDetails(Long groupId);
    List<GroupDetailsResponse> searchGroups(String name);
    List<GroupDetailsResponse> getUserGroups(String email);
    void removeMember(Long groupId, Long memberId);
    void deleteGroup(Long groupId);
    String generateCsvExport(Long groupId);
}
