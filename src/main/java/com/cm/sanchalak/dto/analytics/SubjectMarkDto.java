package com.cm.sanchalak.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubjectMarkDto {
    private String subject;
    private Double score;
    private Double maxMarks;
    private String grade;
}
