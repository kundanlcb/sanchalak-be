package com.cm.sanchalak.dto.hr;

import lombok.Data;

import java.util.List;

@Data
public class LeaveTypeDto {
    private Long id;
    private String name;
    private boolean isPaid;
    private int defaultAnnualQuota;
    private List<String> applicableRoles;
    private boolean requiresDocumentUpload;
}
