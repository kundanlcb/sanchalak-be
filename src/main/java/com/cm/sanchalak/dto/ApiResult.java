package com.cm.sanchalak.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;

/**
 * Standardized API response envelope for mobile API
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResult<T>(
    Boolean success,
    T data,
    ApiError error,
    ApiMeta meta
) implements Serializable {

    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(true, data, null, new ApiMeta());
    }
    
    public static <T> ApiResult<T> success(T data, ApiMeta meta) {
        return new ApiResult<>(true, data, null, meta);
    }

    public static <T> ApiResult<T> error(String code, String message) {
        return new ApiResult<>(false, null, ApiError.of(code, message), null);
    }
    
    public static <T> ApiResult<T> error(ApiError error) {
        return new ApiResult<>(false, null, error, null);
    }
}
