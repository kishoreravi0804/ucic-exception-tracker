package com.fintrack.ucic_tracker.entity;


import com.fintrack.ucic_tracker.enums.GroupStatus;
import com.fintrack.ucic_tracker.enums.RootCause;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exception_groups")
@Getter @Setter
public class ExceptionGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_ref", unique = true, nullable = false, length = 30)
    private String groupRef;

    @Enumerated(EnumType.STRING)
    @Column(name = "root_cause")
    private RootCause rootCause = RootCause.UNIDENTIFIED;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private GroupStatus status = GroupStatus.PENDING;

    @Column(name = "record_count")
    private Integer recordCount = 0;

    @Column(name = "vendor_token", unique = true, length = 64)
    private String vendorToken;

    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_note", columnDefinition = "TEXT")
    private String resolutionNote;

    @CreationTimestamp
    @Column(name = "detected_at", updatable = false)
    private LocalDateTime detectedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "exceptionGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ExceptionGroupMember> members = new ArrayList<>();
}