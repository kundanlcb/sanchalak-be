package com.cm.sanchalak.dto.academic;

import com.cm.sanchalak.dto.curriculum.QuestionDto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamQuestionDto {
    private Long id;
    private Long examScheduleId;
    private Integer marks;
    private Integer sequenceOrder;
    private QuestionDto question;
}
