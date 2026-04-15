package com.zorvyn.finance_backend.controller;

import com.zorvyn.finance_backend.dto.LoginRequestDto;
import com.zorvyn.finance_backend.dto.LoginResponseDto;
import com.zorvyn.finance_backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.zorvyn.finance_backend.dto.ChangePasswordDto;
import com.zorvyn.finance_backend.security.JwtUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        LoginResponseDto response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordDto dto,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);
        authService.changePassword(email, dto);

        return ResponseEntity.ok("Password changed successfully");
    }
}