package com.gravitycanvas.service;

import com.gravitycanvas.dto.ChangePasswordRequest;
import com.gravitycanvas.dto.CreateUserRequest;
import com.gravitycanvas.dto.TaskResponseDto;
import com.gravitycanvas.dto.UserDto;
import com.gravitycanvas.entity.Task;
import com.gravitycanvas.entity.TaskStatus;
import com.gravitycanvas.entity.UserAccount;
import com.gravitycanvas.repository.TaskRepository;
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
    private final TaskRepository taskRepository;
    private final GravityCalculatorService gravityCalculatorService;

    public UserService(UserAccountRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       UserValidationService validationService,
                       TaskRepository taskRepository,
                       GravityCalculatorService gravityCalculatorService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.validationService = validationService;
        this.taskRepository = taskRepository;
        this.gravityCalculatorService = gravityCalculatorService;
    }

    /**
     * Retrieves all system users formatted as UserDto with live Gravitational Load & Burnout metrics.
     */
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        List<Task> allTasks = taskRepository.findAll();
        return userRepository.findAll().stream()
            .map(u -> convertToDtoWithTasks(u, allTasks))
            .collect(Collectors.toList());
    }

    /**
     * Gets user profile by username with live Gravitational Load & Burnout metrics.
     */
    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String username) {
        UserAccount user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return convertToDtoWithTasks(user, taskRepository.findAll());
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
        return convertToDtoWithTasks(user, taskRepository.findAll());
    }

    public UserDto convertToDtoWithTasks(UserAccount user, List<Task> allTasks) {
        UserDto dto = new UserDto(
            user.getId(),
            user.getUsername(),
            user.getFullName(),
            user.getRole(),
            user.getEmployeeNo(),
            user.getDepartment()
        );

        if (allTasks != null && gravityCalculatorService != null) {
            List<Task> activeAssigned = allTasks.stream()
                .filter(t -> t.getAssignee() != null && t.getAssignee().getId().equals(user.getId()) && t.getStatus() != TaskStatus.DONE)
                .collect(Collectors.toList());

            double totalLoad = 0.0;
            int focusCount = 0;
            for (Task t : activeAssigned) {
                TaskResponseDto taskDto = gravityCalculatorService.calculateGravity(t, allTasks);
                totalLoad += taskDto.getTotalGravityScore();
                if (taskDto.getCanvasZone() != null && "FOCUS".equalsIgnoreCase(taskDto.getCanvasZone())) {
                    focusCount++;
                }
            }

            double roundedLoad = Math.round(totalLoad * 10.0) / 10.0;
            double capacityPct = Math.round((roundedLoad / 200.0) * 1000.0) / 10.0;
            String burnoutStatus = "NORMAL";
            if (roundedLoad >= 200.0 || focusCount >= 3) {
                burnoutStatus = "SUPERNOVA_LOCKED";
            } else if (roundedLoad >= 120.0 || focusCount >= 2) {
                burnoutStatus = "HEAVY";
            }

            dto.setActiveGravityLoad(roundedLoad);
            dto.setActiveTasksCount(activeAssigned.size());
            dto.setFocusTasksCount(focusCount);
            dto.setCapacityPercentage(capacityPct);
            dto.setBurnoutStatus(burnoutStatus);
        }

        return dto;
    }
}

