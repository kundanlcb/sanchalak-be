package com.cm.sanchalak.dto;

import java.time.LocalDateTime;

/**
 * Metadata object for mobile API responses
 */
public class ApiMeta {
    
    private String requestId;
    private LocalDateTime timestamp = LocalDateTime.now();
    private Pagination pagination;

    public ApiMeta() {}

    public ApiMeta(String requestId, LocalDateTime timestamp, Pagination pagination) {
        this.requestId = requestId;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.pagination = pagination;
    }

    public static ApiMetaBuilder builder() {
        return new ApiMetaBuilder();
    }

    public String getRequestId() { return requestId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Pagination getPagination() { return pagination; }

    public void setRequestId(String requestId) { this.requestId = requestId; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public void setPagination(Pagination pagination) { this.pagination = pagination; }

    public static class ApiMetaBuilder {
        private String requestId;
        private LocalDateTime timestamp = LocalDateTime.now();
        private Pagination pagination;

        ApiMetaBuilder() {}

        public ApiMetaBuilder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public ApiMetaBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ApiMetaBuilder pagination(Pagination pagination) {
            this.pagination = pagination;
            return this;
        }

        public ApiMeta build() {
            return new ApiMeta(requestId, timestamp, pagination);
        }
    }

    public static class Pagination {
        private Integer page;
        private Integer pageSize;
        private Long totalElements;
        private Integer totalPages;

        public Pagination() {}

        public Pagination(Integer page, Integer pageSize, Long totalElements, Integer totalPages) {
            this.page = page;
            this.pageSize = pageSize;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
        }

        public static PaginationBuilder builder() {
            return new PaginationBuilder();
        }

        public Integer getPage() { return page; }
        public Integer getPageSize() { return pageSize; }
        public Long getTotalElements() { return totalElements; }
        public Integer getTotalPages() { return totalPages; }

        public void setPage(Integer page) { this.page = page; }
        public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
        public void setTotalElements(Long totalElements) { this.totalElements = totalElements; }
        public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }

        public static class PaginationBuilder {
            private Integer page;
            private Integer pageSize;
            private Long totalElements;
            private Integer totalPages;

            PaginationBuilder() {}

            public PaginationBuilder page(Integer page) { this.page = page; return this; }
            public PaginationBuilder pageSize(Integer pageSize) { this.pageSize = pageSize; return this; }
            public PaginationBuilder totalElements(Long totalElements) { this.totalElements = totalElements; return this; }
            public PaginationBuilder totalPages(Integer totalPages) { this.totalPages = totalPages; return this; }

            public Pagination build() {
                return new Pagination(page, pageSize, totalElements, totalPages);
            }
        }
    }
}
