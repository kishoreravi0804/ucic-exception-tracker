package com.fintrack.ucic_tracker.controller;

import com.fintrack.ucic_tracker.dto.*;
import com.fintrack.ucic_tracker.service.ExceptionGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendor")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VendorController {

    private final ExceptionGroupService exceptionGroupService;

    // GET /api/vendor/review?token=xxx — vendor sees their group
    @GetMapping("/review")
    public ResponseEntity<ExceptionGroupDTO> getByToken(
            @RequestParam String token) {
        return ResponseEntity.ok(exceptionGroupService.getByVendorToken(token));
    }

    // POST /api/vendor/resolve?token=xxx — vendor submits resolution
    @PostMapping("/resolve")
    public ResponseEntity<ExceptionGroupDTO> resolveByToken(
            @RequestParam String token,
            @RequestBody ResolveRequest request) {
        return ResponseEntity.ok(exceptionGroupService.resolveByToken(token, request));
    }
}
