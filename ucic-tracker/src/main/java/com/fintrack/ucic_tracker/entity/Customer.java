package com.fintrack.ucic_tracker.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "customers")
@Getter @Setter
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false, length = 20)
    private String customerId;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "pan", length = 10)
    private String pan;

    @Column(name = "voter_id", length = 20)
    private String voterId;

    @Column(name = "aadhaar", length = 12)
    private String aadhaar;

    @Column(name = "dl", length = 20)
    private String dl;

    @Column(name = "passport", length = 10)
    private String passport;

    @Column(name = "mobile", length = 10)
    private String mobile;

    @Column(name = "email", length = 100)
    private String email;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}