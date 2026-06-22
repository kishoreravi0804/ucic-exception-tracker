package com.fintrack.ucic_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardDTO {
    private Long totalCustomers;
    private Long totalGroups;
    private Long pending;
    private Long sentToVendor;
    private Long resolved;
    private Long rejected;
    private Long lastRunDurationMs;
    private Long lastRunGroupsFound;
}
