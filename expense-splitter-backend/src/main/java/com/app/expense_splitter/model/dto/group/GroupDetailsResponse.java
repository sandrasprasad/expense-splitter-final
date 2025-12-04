package com.app.expense_splitter.model.dto.group;

import lombok.Data;
import java.util.List;

@Data
public class GroupDetailsResponse {
    private Long id;
    private String name;
    private List<GroupMembers> members;
    private List<String> balances;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<GroupMembers> getMembers() {
        return members;
    }

    public void setMembers(List<GroupMembers> members) {
        this.members = members;
    }

    public List<String> getBalances() {
        return balances;
    }

    public void setBalances(List<String> balances) {
        this.balances = balances;
    }
}
