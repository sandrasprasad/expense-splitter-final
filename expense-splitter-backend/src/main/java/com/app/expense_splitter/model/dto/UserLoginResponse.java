package com.app.expense_splitter.model.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserLoginResponse {

    private String message;
    private Long userId;
    private String name;
    private String token;

}
