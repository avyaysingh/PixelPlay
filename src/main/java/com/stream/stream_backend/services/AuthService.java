package com.stream.stream_backend.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.stream.stream_backend.dto.LoginRequest;
import com.stream.stream_backend.dto.LoginResponse;
import com.stream.stream_backend.dto.MessageResponse;
import com.stream.stream_backend.dto.SignupRequest;
import com.stream.stream_backend.entities.Role;
import com.stream.stream_backend.entities.User;
import com.stream.stream_backend.jwt.JwtAuthenticationHelper;
import com.stream.stream_backend.repositories.RoleRepository;
import com.stream.stream_backend.repositories.UserRepository;

@Service
public class AuthService {

    Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private JwtAuthenticationHelper jwtHelper;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private RoleRepository roleRepository;

    public LoginResponse authenticateUser(LoginRequest loginRequest) {

        String usernameOrEmail = loginRequest.getUserNameOrEmail();

        // User user = userRepository.findByUsernameOrEmail(usernameOrEmail,
        // usernameOrEmail)
        // .orElseThrow(() -> new RuntimeException("User not Found"));

        doAuthenticate(usernameOrEmail, loginRequest.getPassword());

        UserDetails userDetails = userDetailsService.loadUserByUsername(usernameOrEmail);

        String token = jwtHelper.generateToken(userDetails);

        return LoginResponse.builder()
                .username(userDetails.getUsername())
                .token(token).build();
    }

    private void doAuthenticate(String username, String password) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username,
                password);

        try {
            manager.authenticate(authenticationToken);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid Username or Password");
        }
    }

    public MessageResponse signup(SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            return new MessageResponse("Error: Username is already taken!");
        }
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return new MessageResponse("Error: Email is already in use!");
        }

        User user = User.builder()
                .name(signupRequest.getName())
                .username(signupRequest.getUsername())
                .email(signupRequest.getEmail())
                .password(encoder.encode(signupRequest.getPassword()))
                .build();

        Role role = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName("ROLE_USER");
                    return roleRepository.save(newRole);
                });

        user.getRoles().add(role);

        userRepository.save(user);

        logger.info("User details log: " + user.getName() + " " + user.getEmail());

        return new MessageResponse("User Registered successfully !");
    }
}
