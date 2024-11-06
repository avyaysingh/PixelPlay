package com.stream.stream_backend.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stream.stream_backend.dto.UserRequest;
import com.stream.stream_backend.dto.UserResponse;
import com.stream.stream_backend.entities.Role;
import com.stream.stream_backend.entities.User;
import com.stream.stream_backend.repositories.RoleRepository;
import com.stream.stream_backend.repositories.UserRepository;

@Service
public class UserService {

    // @Autowired
    // private PasswordEncoder encoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    public List<UserResponse> getAll() {
        return userRepository.findAll().stream()
                .map(this::convertUsertoUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for id: " + userId));

        return convertUsertoUserResponse(user);
    }

    public UserResponse searchUser(String keywords) {
        User user = userRepository.findByUsernameOrEmail(keywords, keywords)
                .orElseThrow(() -> new RuntimeException("User Not Found for given keyword"));

        return convertUsertoUserResponse(user);
    }

    public void updateUserById(UserRequest userDto) {
        User user = userRepository.findByUsernameOrEmail(userDto.getUsername(), userDto.getEmail())
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        user.setEmail(userDto.getEmail());
        user.setName(userDto.getName());
        // user.setPassword(encoder.encode(userDto.getPassword()));
        user.setUsername(userDto.getUsername());

        Role role = roleRepository.findByName(userDto.getRole())
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(userDto.getRole());
                    return roleRepository.save(newRole);
                });

        user.getRoles().add(role);

        userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        // User user = userRepository.findById(userId)
        // .orElseThrow(() -> new RuntimeException("User not found for id: " + userId));
        userRepository.deleteById(userId);
    }

    public String findUsernameById(Long userId) {
        return userRepository.findUsernameById(userId).orElseThrow(() -> new RuntimeException("User Not Found"))
                .getUsername();
    }

    private UserResponse convertUsertoUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .username(user.getUsername())
                .build();
    }

}