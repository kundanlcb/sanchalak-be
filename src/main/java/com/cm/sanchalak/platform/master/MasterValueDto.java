package com.cm.sanchalak.platform.master;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterValueDto {
    private String code;
    private String label;
    private Integer sortOrder;
    private boolean isActive;
}
