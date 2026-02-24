package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "document_templates", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "school_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentTemplate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    // School branding
    @Column(name = "school_name")
    private String schoolName;

    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(name = "phone1", length = 20)
    private String phone1;

    @Column(name = "phone2", length = 20)
    private String phone2;

    @Column(name = "reg_no", length = 50)
    private String regNo;

    @Column(name = "school_code", length = 50)
    private String schoolCode;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "primary_color_hex", length = 10)
    private String primaryColorHex;

    // Customizable footer notes per document type
    @Column(name = "admit_card_footer_note", columnDefinition = "TEXT")
    private String admitCardFooterNote;

    @Column(name = "fee_receipt_footer_note", columnDefinition = "TEXT")
    private String feeReceiptFooterNote;

    // Designation labels for signature blocks
    @Column(name = "controller_designation", length = 100)
    private String controllerDesignation;

    @Column(name = "principal_designation", length = 100)
    private String principalDesignation;
}
