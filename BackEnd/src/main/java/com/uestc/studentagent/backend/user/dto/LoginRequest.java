package com.uestc.studentagent.backend.user.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {

    @NotBlank(message = "account is required")
    @Size(max = 128, message = "account is too long")
    @JsonAlias({"username", "email", "usernameOrEmail"})
    private String account;

    @NotBlank(message = "password is required")
    @Size(min = 8, max = 64, message = "password length must be between 8 and 64")
    private String password;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
