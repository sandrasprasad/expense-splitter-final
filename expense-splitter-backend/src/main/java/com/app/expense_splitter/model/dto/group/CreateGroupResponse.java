package com.app.expense_splitter.model.dto.group;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class CreateGroupResponse {

    private Long id;
    private String name;
    private List<GroupMembers> members;

}
