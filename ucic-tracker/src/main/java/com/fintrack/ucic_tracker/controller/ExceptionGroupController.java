package com.fintrack.ucic_tracker.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fintrack.ucic_tracker.dto.ExceptionGroupDTO;
import com.fintrack.ucic_tracker.dto.ResolveRequest;
import com.fintrack.ucic_tracker.entity.AuditLog;
import com.fintrack.ucic_tracker.enums.GroupStatus;
import com.fintrack.ucic_tracker.service.ExceptionGroupService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/exceptions")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class ExceptionGroupController {

    private final ExceptionGroupService exceptionGroupService;

    // GET /api/exceptions — all groups, optional filter by status
    @GetMapping
    public ResponseEntity<List<ExceptionGroupDTO>> getAllGroups(
            @RequestParam(required = false) GroupStatus status) {
        return ResponseEntity.ok(exceptionGroupService.getAllGroups(status));
    }

    // GET /api/exceptions/{id} — single group with members
    @GetMapping("/{id}")
    public ResponseEntity<ExceptionGroupDTO> getGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(exceptionGroupService.getGroupById(id));
    }

    // PUT /api/exceptions/{id}/send-vendor — generate token, mark sent
    @PutMapping("/{id}/send-vendor")
    public ResponseEntity<ExceptionGroupDTO> sendToVendor(@PathVariable Long id) {
        return ResponseEntity.ok(exceptionGroupService.sendToVendor(id));
    }

    // PUT /api/exceptions/{id}/resolve — admin manually resolves
    @PutMapping("/{id}/resolve")
    public ResponseEntity<ExceptionGroupDTO> resolveManually(
            @PathVariable Long id,
            @RequestBody ResolveRequest request) {
        return ResponseEntity.ok(exceptionGroupService.resolveManually(id, request));
    }

    // PUT /api/exceptions/{id}/reject — reject group
    @PutMapping("/{id}/reject")
    public ResponseEntity<ExceptionGroupDTO> rejectGroup(
            @PathVariable Long id,
            @RequestParam String note) {
        return ResponseEntity.ok(exceptionGroupService.rejectGroup(id, note));
    }

    // GET /api/exceptions/{id}/audit — audit trail
    @GetMapping("/{id}/audit")
    public ResponseEntity<List<AuditLog>> getAuditLog(@PathVariable Long id) {
        return ResponseEntity.ok(exceptionGroupService.getAuditLog(id));
    }
}
