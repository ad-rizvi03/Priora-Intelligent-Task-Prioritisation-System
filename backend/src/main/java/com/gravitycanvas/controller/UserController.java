package com.gravitycanvas.controller;

import com.gravitycanvas.dto.ChangePasswordRequest;
import com.gravitycanvas.dto.CreateUserRequest;
import com.gravitycanvas.dto.UserDto;
import com.gravitycanvas.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller exposing operations for User Accounts, Admin User Creation, and Double-Validation.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/users
     * Allowed Roles: ROLE_ADMIN, ROLE_MANAGER, ROLE_TEAM_MEMBER
     */
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * POST /api/users/register
     * Public Registration Endpoint (Allows creating users without pre-seeding)
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody CreateUserRequest request) {
        try {
            UserDto created = userService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : "User registration failed.";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", errorMsg));
        }
    }

    /**
     * POST /api/users
     * Allowed Roles: ROLE_PROJECT_MANAGER
     * Admin creates initial credentials for Developers, Managers, or Custom Accounts.
     */
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        try {
            UserDto created = userService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : "User creation failed due to system error.";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", errorMsg));
        }
    }

    /**
     * PUT /api/users/change-password
     * Allowed Roles: ROLE_ADMIN, ROLE_MANAGER, ROLE_TEAM_MEMBER
     * Authenticated user changes their own password.
     */
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            userService.changePassword(currentUsername, request);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Password change failed.";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", errorMsg));
        }
    }
}
