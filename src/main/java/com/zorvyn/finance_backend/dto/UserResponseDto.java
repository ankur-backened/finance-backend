package com.zorvyn.finance_backend.dto;

import com.zorvyn.finance_backend.enums.Role;
import com.zorvyn.finance_backend.enums.UserStatus;
import lombok.Data;

@Data
public class UserResponseDto {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private UserStatus status;
}