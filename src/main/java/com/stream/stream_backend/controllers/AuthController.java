package com.stream.stream_backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stream.stream_backend.dto.LoginRequest;
import com.stream.stream_backend.dto.LoginResponse;
import com.stream.stream_backend.dto.MessageResponse;
import com.stream.stream_backend.dto.SignupRequest;
import com.stream.stream_backend.services.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/test")
    public String test() {
        return "Hello";
    }

    @PostMapping("/signin")
    public ResponseEntity<LoginResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return new ResponseEntity<>(authService.authenticateUser(loginRequest), HttpStatus.OK);
    }

    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> signup(@RequestBody SignupRequest signupRequest) {
        MessageResponse response = authService.signup(signupRequest);

        if (response.getMessage().contains("Error")) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }
}
