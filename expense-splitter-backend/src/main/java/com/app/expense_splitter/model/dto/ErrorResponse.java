package com.app.expense_splitter.model.dto;

import java.time.LocalDateTime;

public class ErrorResponse {

    private boolean success;
    private String errorMessage;
    private String timestamp;

    public ErrorResponse(boolean success, String message) {
        this.success = success;
        this.errorMessage = message;
        this.timestamp = LocalDateTime.now().toString();  // ⭐ timestamp added here
    }
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
