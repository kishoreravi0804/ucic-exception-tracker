package com.fintrack.ucic_tracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_runs")
@Getter @Setter
public class AnalysisRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_customers")
    private Long totalCustomers;

    @Column(name = "groups_found")
    private Integer groupsFound;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "triggered_by", length = 50)
    private String triggeredBy = "SYSTEM";

    @CreationTimestamp
    @Column(name = "run_at", updatable = false)
    private LocalDateTime runAt;
}