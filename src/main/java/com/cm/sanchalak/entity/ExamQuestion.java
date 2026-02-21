package com.cm.sanchalak.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exam_questions", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "exam_schedule_id", "question_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamQuestion extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "exam_schedule_id", nullable = false)
    private ExamSchedule examSchedule;

    @ManyToOne(optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "marks", nullable = false)
    private Integer marks;

    @Column(name = "sequence_order")
    private Integer sequenceOrder;
}
