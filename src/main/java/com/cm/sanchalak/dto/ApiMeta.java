package com.cm.sanchalak.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Metadata object for mobile API responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiMeta {

    private String requestId;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private Pagination pagination;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Pagination {
        private Integer page;
        private Integer pageSize;
        private Long totalElements;
        private Integer totalPages;
    }
}
