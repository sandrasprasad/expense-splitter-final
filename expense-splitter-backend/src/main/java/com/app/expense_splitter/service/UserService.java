package com.app.expense_splitter.service;

import com.app.expense_splitter.model.dto.UserLoginRequest;
import com.app.expense_splitter.model.dto.UserLoginResponse;
import com.app.expense_splitter.model.dto.UserRegisterRequest;
import com.app.expense_splitter.model.dto.UserRegisterResponse;

public interface UserService {
    UserRegisterResponse registerUser(UserRegisterRequest request);

    UserLoginResponse login(UserLoginRequest loginRequest);
}