package com.gravitycanvas.controller;

import com.gravitycanvas.dto.GravitySettingsDto;
import com.gravitycanvas.service.SettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing customizable Gravity Engine formula factors
 * and developer ticket authorization policies.
 */
@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    /**
     * GET /api/settings
     * Allowed Roles: ROLE_MANAGER, ROLE_TEAM_MEMBER
     */
    @GetMapping
    public ResponseEntity<GravitySettingsDto> getSettings() {
        return ResponseEntity.ok(settingsService.getSettingsDto());
    }

    /**
     * PUT /api/settings
     * Allowed Roles: ROLE_MANAGER ONLY
     */
    @PutMapping
    public ResponseEntity<GravitySettingsDto> updateSettings(@RequestBody GravitySettingsDto request) {
        return ResponseEntity.ok(settingsService.updateSettings(request));
    }
}
