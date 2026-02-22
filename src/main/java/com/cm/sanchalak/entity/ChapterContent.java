package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "chapter_contents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterContent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id")
    private UUID schoolId;

    @ManyToOne(optional = true)
    @JoinColumn(name = "chapter_id", nullable = true)
    private SubjectChapter chapter;

    @Column(name = "class_id")
    private Long classId;

    @Column(name = "subject_id")
    private Long subjectId;

    @Column(nullable = false)
    private String title;

    @Column(name = "text_content", columnDefinition = "TEXT")
    private String textContent;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "pdf_url")
    private String pdfUrl;

    @Column(name = "link_url")
    private String linkUrl;

    @Column(name = "sequence_order")
    private Integer sequenceOrder;
}
