package com.app.expense_splitter.model.dto;

import lombok.Data;

@Data
public class UserRegisterRequest {

    private String name;
    private String email;
    private String phoneNumber;
    private String password;

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPassword() {
        return password;
    }
}
