package com.stream.stream_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotNull(message = "Provide Email or Username")
    private String userNameOrEmail;

    @NotNull(message = "Provide correct password")
    private String password;

}