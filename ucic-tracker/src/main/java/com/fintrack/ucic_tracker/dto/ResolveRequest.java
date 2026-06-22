package com.fintrack.ucic_tracker.dto;

import com.fintrack.ucic_tracker.enums.RootCause;
import lombok.Data;

@Data
public class ResolveRequest {
    private RootCause rootCause;
    private String resolutionNote;
}
