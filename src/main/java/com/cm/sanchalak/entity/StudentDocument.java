package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class StudentDocument extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private String documentType; // AADHAR, PAN, CERTIFICATE, TRANSFER_CERT, OTHERS

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileUrl; // S3 Key or URL

    @Column(nullable = false)
    private String mimeType;

    private Long fileSize;

    private String description;
}
