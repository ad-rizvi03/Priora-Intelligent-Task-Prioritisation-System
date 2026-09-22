package com.gravitycanvas.controller;

import com.gravitycanvas.dto.UserDto;
import com.gravitycanvas.entity.UserAccount;
import com.gravitycanvas.repository.UserAccountRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller providing user context endpoints for authentication state verification.
 */
@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserAccountRepository userRepository;

    public AuthController(UserAccountRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * GET /api/auth/me
     * Returns current authenticated user profile and assigned security role.
     */
    @GetMapping("/auth/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).build();
        }

        String username = auth.getName();
        UserAccount user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Authenticated user record not found in database"));

        return ResponseEntity.ok(new UserDto(
            user.getId(),
            user.getUsername(),
            user.getFullName(),
            user.getRole(),
            user.getEmployeeNo(),
            user.getDepartment()
        ));
    }
}
