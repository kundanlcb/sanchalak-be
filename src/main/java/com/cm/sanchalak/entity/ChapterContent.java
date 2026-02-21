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

    @ManyToOne(optional = false)
    @JoinColumn(name = "chapter_id", nullable = false)
    private SubjectChapter chapter;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    private ContentType contentType;

    @Column(name = "content_data", columnDefinition = "TEXT", nullable = false)
    private String contentData;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    public enum ContentType {
        TEXT,
        VIDEO,
        PDF,
        LINK
    }
}
