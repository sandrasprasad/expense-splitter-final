package com.app.expense_splitter.model.dto.group;

import lombok.Data;
import java.util.List;

@Data
public class GroupDetailsResponse {
    private Long id;
    private String name;
    private List<GroupMembers> members;
    private List<String> balances;

}
