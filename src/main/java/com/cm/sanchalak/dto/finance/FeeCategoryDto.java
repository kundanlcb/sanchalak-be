package com.cm.sanchalak.dto.finance;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FeeCategoryDto {
    private Long id;
    @NotBlank(message = "Name is required")
    private String name;
    private String description;
    private Boolean isMandatory;
}
