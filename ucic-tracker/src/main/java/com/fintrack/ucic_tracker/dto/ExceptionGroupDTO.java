package com.fintrack.ucic_tracker.dto;

import com.fintrack.ucic_tracker.enums.GroupStatus;
import com.fintrack.ucic_tracker.enums.RootCause;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExceptionGroupDTO {
    private Long id;
    private String groupRef;
    private RootCause rootCause;
    private GroupStatus status;
    private Integer recordCount;
    private String vendorToken;
    private LocalDateTime sentAt;
    private LocalDateTime resolvedAt;
    private String resolutionNote;
    private LocalDateTime detectedAt;
    private List<CustomerDTO> members;
}
