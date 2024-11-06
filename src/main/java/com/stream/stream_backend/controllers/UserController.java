package com.stream.stream_backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.stream.stream_backend.dto.UserRequest;
import com.stream.stream_backend.dto.UserResponse;
import com.stream.stream_backend.security.CustomUserDetailsService;
import com.stream.stream_backend.services.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAll() {
        return userService.getAll();
    }

    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getByUserId(@PathVariable Long userId) {
        return userService.getByUserId(userId);
    }

    @GetMapping("/search/{keywords}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse searchUser(@PathVariable String keywords) {
        return userService.searchUser(keywords);
    }

    @PutMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public void updateUserById(@RequestBody UserRequest userRequest) {
        userService.updateUserById(userRequest);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
    }

    @GetMapping("/{userId}/username")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<String> findUsernameById(@PathVariable Long userId) {
        return new ResponseEntity<>(userService.findUsernameById(userId), HttpStatus.OK);
    }

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @GetMapping("/username/{username}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserDetails> getUserDetailsByUsername(@PathVariable String username) {
        // System.out.println(username);

        // System.out.println("Find By Username");
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return ResponseEntity.ok(userDetails);
    }

}