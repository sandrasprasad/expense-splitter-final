package com.app.expense_splitter.controller;

import com.app.expense_splitter.model.dto.UserLoginRequest;
import com.app.expense_splitter.model.dto.UserLoginResponse;
import com.app.expense_splitter.model.dto.UserRegisterRequest;
import com.app.expense_splitter.model.dto.UserRegisterResponse;
import com.app.expense_splitter.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("http://localhost:5173/")
@RequiredArgsConstructor
public class UserController  {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponse> registerUser (@RequestBody UserRegisterRequest request){

        System.out.println("request" + request);
        UserRegisterResponse response = userService.registerUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> loginUser(@RequestBody UserLoginRequest loginRequest){

        UserLoginResponse loginResponse = userService.login(loginRequest);
        System.out.println("loginrespone " + ResponseEntity.ok(loginResponse)   );
        return ResponseEntity.ok(loginResponse);
    }

    @RestController
    @RequestMapping("/api/test")
    public class TestAuthController {

        @GetMapping("/secure")
        public String secureEndpoint() {
            return "You are authenticated!";
        }
    }

}
