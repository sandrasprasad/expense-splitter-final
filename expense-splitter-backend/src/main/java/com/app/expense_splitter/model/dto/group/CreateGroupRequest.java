package com.app.expense_splitter.model.dto.group;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class CreateGroupRequest {

    @NotBlank
    private String name;

    public void setName(String name) {
        this.name = name;
    }
}
