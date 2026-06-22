package com.fintrack.ucic_tracker.dto;

import lombok.Data;

@Data
public class CustomerDTO {
    private Long id;
    private String customerId;
    private String fullName;
    private String pan;
    private String voterId;
    private String aadhaar;
    private String dl;
    private String passport;
    private String mobile;
    private String email;
    private Boolean isMaster;
}
