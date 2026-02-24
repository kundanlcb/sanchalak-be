package com.cm.sanchalak.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentTemplateDto {
    private Long id;
    private String schoolName;
    private String addressLine1;
    private String addressLine2;
    private String phone1;
    private String phone2;
    private String regNo;
    private String schoolCode;
    private String logoUrl;
    private String primaryColorHex;
    private String admitCardFooterNote;
    private String feeReceiptFooterNote;
    private String controllerDesignation;
    private String principalDesignation;
}
