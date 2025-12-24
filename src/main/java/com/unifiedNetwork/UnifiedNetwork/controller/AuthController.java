package com.unifiedNetwork.UnifiedNetwork.controller;

import com.unifiedNetwork.UnifiedNetwork.model.User;
import com.unifiedNetwork.UnifiedNetwork.security.JwtTokenProvider;
import com.unifiedNetwork.UnifiedNetwork.repository.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final IUserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        userService.registerUser(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        User user = userService.authenticateUser(loginRequest.getUsername(), loginRequest.getPassword());
        String token = JwtTokenProvider.generateToken(user);
        return ResponseEntity.ok(Map.of("token", token));
    }
}

class LoginRequest {
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}

