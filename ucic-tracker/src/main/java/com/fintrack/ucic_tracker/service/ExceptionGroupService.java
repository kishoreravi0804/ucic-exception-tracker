package com.fintrack.ucic_tracker.service;

import com.fintrack.ucic_tracker.dto.*;
import com.fintrack.ucic_tracker.entity.*;
import com.fintrack.ucic_tracker.enums.GroupStatus;
import com.fintrack.ucic_tracker.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExceptionGroupService {

    private final ExceptionGroupRepository exceptionGroupRepository;
    private final ExceptionGroupMemberRepository memberRepository;
    private final AuditLogRepository auditLogRepository;
    private final CustomerRepository customerRepository;
    private final AnalysisRunRepository analysisRunRepository;

    @Value("${app.vendor.token.expiry.days:7}")
    private int tokenExpiryDays;

    // ── Get all groups ──
    public List<ExceptionGroupDTO> getAllGroups(GroupStatus status) {
        List<ExceptionGroup> groups = status != null
                ? exceptionGroupRepository.findByStatus(status)
                : exceptionGroupRepository.findAllOrderByDetectedAtDesc();
        return groups.stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ── Get single group with members ──
    public ExceptionGroupDTO getGroupById(Long id) {
        ExceptionGroup group = exceptionGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found: " + id));
        return toDTOWithMembers(group);
    }

    // ── Send to vendor — generate token ──
    @Transactional
    public ExceptionGroupDTO sendToVendor(Long id) {
        ExceptionGroup group = exceptionGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found: " + id));

        if (group.getStatus() != GroupStatus.PENDING) {
            throw new RuntimeException("Only PENDING groups can be sent to vendor");
        }

        String token = UUID.randomUUID().toString().replace("-", "") +
                       UUID.randomUUID().toString().replace("-", "");
        group.setVendorToken(token);
        group.setTokenExpiresAt(LocalDateTime.now().plusDays(tokenExpiryDays));
        group.setStatus(GroupStatus.SENT_TO_VENDOR);
        group.setSentAt(LocalDateTime.now());
        exceptionGroupRepository.save(group);

        saveAudit(group, "SENT_TO_VENDOR", "ADMIN",
                "Sent to vendor. Token expires: " + group.getTokenExpiresAt());

        log.info("Group {} sent to vendor. Token generated.", group.getGroupRef());
        return toDTOWithMembers(group);
    }

    // ── Vendor resolves via token ──
    @Transactional
    public ExceptionGroupDTO resolveByToken(String token, ResolveRequest request) {
        ExceptionGroup group = exceptionGroupRepository.findByVendorToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        if (LocalDateTime.now().isAfter(group.getTokenExpiresAt())) {
            throw new RuntimeException("Vendor token has expired");
        }

        group.setStatus(GroupStatus.RESOLVED);
        group.setResolvedAt(LocalDateTime.now());
        group.setRootCause(request.getRootCause());
        group.setResolutionNote(request.getResolutionNote());
        exceptionGroupRepository.save(group);

        saveAudit(group, "RESOLVED", "VENDOR",
                "Resolved by vendor. Root cause: " + request.getRootCause()
                + " | Note: " + request.getResolutionNote());

        log.info("Group {} resolved by vendor.", group.getGroupRef());
        return toDTOWithMembers(group);
    }

    // ── Admin manually resolve ──
    @Transactional
    public ExceptionGroupDTO resolveManually(Long id, ResolveRequest request) {
        ExceptionGroup group = exceptionGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found: " + id));

        group.setStatus(GroupStatus.RESOLVED);
        group.setResolvedAt(LocalDateTime.now());
        group.setRootCause(request.getRootCause());
        group.setResolutionNote(request.getResolutionNote());
        exceptionGroupRepository.save(group);

        saveAudit(group, "RESOLVED", "ADMIN",
                "Manually resolved. Root cause: " + request.getRootCause());

        return toDTOWithMembers(group);
    }

    // ── Reject group ──
    @Transactional
    public ExceptionGroupDTO rejectGroup(Long id, String note) {
        ExceptionGroup group = exceptionGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found: " + id));

        group.setStatus(GroupStatus.REJECTED);
        group.setResolutionNote(note);
        group.setResolvedAt(LocalDateTime.now());
        exceptionGroupRepository.save(group);

        saveAudit(group, "REJECTED", "VENDOR", "Rejected: " + note);
        return toDTOWithMembers(group);
    }

    // ── Get vendor portal data by token ──
    public ExceptionGroupDTO getByVendorToken(String token) {
        ExceptionGroup group = exceptionGroupRepository.findByVendorToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (LocalDateTime.now().isAfter(group.getTokenExpiresAt())) {
            throw new RuntimeException("Token has expired");
        }
        return toDTOWithMembers(group);
    }

    // ── Dashboard summary ──
    public DashboardDTO getDashboard() {
        DashboardDTO dto = new DashboardDTO();
        dto.setTotalCustomers(customerRepository.count());
        dto.setTotalGroups(exceptionGroupRepository.count());
        dto.setPending(exceptionGroupRepository.countByStatus(GroupStatus.PENDING));
        dto.setSentToVendor(exceptionGroupRepository.countByStatus(GroupStatus.SENT_TO_VENDOR));
        dto.setResolved(exceptionGroupRepository.countByStatus(GroupStatus.RESOLVED));
        dto.setRejected(exceptionGroupRepository.countByStatus(GroupStatus.REJECTED));

        analysisRunRepository.findTop5ByOrderByRunAtDesc()
                .stream().findFirst().ifPresent(run -> {
                    dto.setLastRunDurationMs(run.getDurationMs());
                    dto.setLastRunGroupsFound((long) run.getGroupsFound());
                });
        return dto;
    }

    // ── Get audit trail for a group ──
    public List<AuditLog> getAuditLog(Long groupId) {
        return auditLogRepository.findByExceptionGroupIdOrderByCreatedAtAsc(groupId);
    }

    // ── Helpers ──
    private void saveAudit(ExceptionGroup group, String action, String actor, String note) {
        AuditLog audit = new AuditLog();
        audit.setExceptionGroup(group);
        audit.setAction(action);
        audit.setActor(actor);
        audit.setNote(note);
        auditLogRepository.save(audit);
    }

    private ExceptionGroupDTO toDTO(ExceptionGroup group) {
        ExceptionGroupDTO dto = new ExceptionGroupDTO();
        dto.setId(group.getId());
        dto.setGroupRef(group.getGroupRef());
        dto.setRootCause(group.getRootCause());
        dto.setStatus(group.getStatus());
        dto.setRecordCount(group.getRecordCount());
        dto.setVendorToken(group.getVendorToken());
        dto.setSentAt(group.getSentAt());
        dto.setResolvedAt(group.getResolvedAt());
        dto.setResolutionNote(group.getResolutionNote());
        dto.setDetectedAt(group.getDetectedAt());
        return dto;
    }

    private ExceptionGroupDTO toDTOWithMembers(ExceptionGroup group) {
        ExceptionGroupDTO dto = toDTO(group);
        List<ExceptionGroupMember> members =
                memberRepository.findByExceptionGroupId(group.getId());
        dto.setMembers(members.stream().map(m -> {
            CustomerDTO c = new CustomerDTO();
            c.setId(m.getCustomer().getId());
            c.setCustomerId(m.getCustomer().getCustomerId());
            c.setFullName(m.getCustomer().getFullName());
            c.setPan(m.getCustomer().getPan());
            c.setVoterId(m.getCustomer().getVoterId());
            c.setAadhaar(m.getCustomer().getAadhaar());
            c.setDl(m.getCustomer().getDl());
            c.setPassport(m.getCustomer().getPassport());
            c.setMobile(m.getCustomer().getMobile());
            c.setEmail(m.getCustomer().getEmail());
            c.setIsMaster(m.getIsMaster());
            return c;
        }).collect(Collectors.toList()));
        return dto;
    }
}
