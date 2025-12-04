package com.app.expense_splitter.model.dto;

import lombok.Data;

@Data
public class UserRegisterResponse {

    private Long id;
    private String name;
    private String email;
    private String phoneNumber;

}
