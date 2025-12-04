package com.app.expense_splitter.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class ErrorResponse {

    private boolean success;
    private String errorMessage;
    private String timestamp;

    public ErrorResponse(boolean success, String message) {
        this.success = success;
        this.errorMessage = message;
        this.timestamp = LocalDateTime.now().toString();
    }

}
