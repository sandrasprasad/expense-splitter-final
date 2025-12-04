package com.app.expense_splitter.model.dto.group;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class AddMemberRequest {
    @NotNull
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
