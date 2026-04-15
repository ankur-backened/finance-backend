package com.zorvyn.finance_backend.service;

import com.zorvyn.finance_backend.dto.LoginRequestDto;
import com.zorvyn.finance_backend.dto.LoginResponseDto;
import com.zorvyn.finance_backend.entity.User;
import com.zorvyn.finance_backend.enums.UserStatus;
import com.zorvyn.finance_backend.exception.ResourceNotFoundException;
import com.zorvyn.finance_backend.repository.UserRepository;
import com.zorvyn.finance_backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.zorvyn.finance_backend.dto.ChangePasswordDto;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public LoginResponseDto login(LoginRequestDto request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email"));

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new RuntimeException("Your account has been deactivated. Contact admin.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Incorrect password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return new LoginResponseDto(token, user.getName(), user.getEmail(), user.getRole().name());
    }

    public void changePassword(String email, ChangePasswordDto dto) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }
}