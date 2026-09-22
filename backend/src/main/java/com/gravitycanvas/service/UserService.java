package com.gravitycanvas.service;

import com.gravitycanvas.dto.ChangePasswordRequest;
import com.gravitycanvas.dto.CreateUserRequest;
import com.gravitycanvas.dto.UserDto;
import com.gravitycanvas.entity.UserAccount;
import com.gravitycanvas.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing System User Accounts, Admin Account Creation, and OWASP Production-Grade Double-Validation.
 */
@Service
public class UserService {

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserValidationService validationService;

    public UserService(UserAccountRepository userRepository, PasswordEncoder passwordEncoder, UserValidationService validationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.validationService = validationService;
    }

    /**
     * Retrieves all system users formatted as UserDto.
     */
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    /**
     * Gets user profile by username.
     */
    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String username) {
        UserAccount user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return convertToDto(user);
    }

    /**
     * Admin creates a new Developer, Manager, or Custom Account with OWASP production-grade validation.
     */
    @Transactional
    public UserDto createUser(CreateUserRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("User creation request cannot be null.");
        }

        // 1. Full Name OWASP Validation
        validationService.validateFullName(req.getFullName());

        // 2. Username OWASP Validation & Case-Insensitive Uniqueness Check
        String username = req.getUsername() != null ? req.getUsername().trim() : "";
        validationService.validateUsername(username);
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken. Please enter a unique username.");
        }

        // 3. Password OWASP Validation
        validationService.validatePassword(req.getPassword());

        // 4. Employee Number Validation & Uniqueness Check
        String employeeNo = req.getEmployeeNo() != null ? req.getEmployeeNo().trim() : "";
        validationService.validateEmployeeNo(employeeNo);
        if (userRepository.existsByEmployeeNo(employeeNo)) {
            throw new IllegalArgumentException("Employee Number '" + employeeNo + "' already exists. Please enter a unique Employee Number.");
        }

        // 5. Department Validation
        String department = req.getDepartment() != null ? req.getDepartment().trim() : "";
        if (department.isEmpty()) {
            throw new IllegalArgumentException("Department is required.");
        }

        // 6. Role Validation
        String rawRole = req.getRole() != null ? req.getRole().trim() : "";
        if (rawRole.isEmpty()) {
            throw new IllegalArgumentException("Role is required.");
        }

        // Normalize Role Format
        String formattedRole = rawRole.toUpperCase();
        if (!formattedRole.startsWith("ROLE_")) {
            formattedRole = "ROLE_" + formattedRole.replaceAll("[^A-Z0-9]", "_");
        }

        UserAccount newUser = new UserAccount();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(req.getPassword().trim()));
        newUser.setFullName(req.getFullName().trim());
        newUser.setRole(formattedRole);
        newUser.setEmployeeNo(employeeNo);
        newUser.setDepartment(department);

        UserAccount saved = userRepository.save(newUser);
        return convertToDto(saved);
    }

    /**
     * Changes password for authenticated user after verifying current password.
     */
    @Transactional
    public void changePassword(String username, ChangePasswordRequest req) {
        UserAccount user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Incorrect current password.");
        }

        String newPassword = req.getNewPassword() != null ? req.getNewPassword().trim() : "";
        validationService.validatePassword(newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public UserDto convertToDto(UserAccount user) {
        return new UserDto(
            user.getId(),
            user.getUsername(),
            user.getFullName(),
            user.getRole(),
            user.getEmployeeNo(),
            user.getDepartment()
        );
    }
}
