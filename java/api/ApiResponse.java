package com.minigalaxy.android.api;

/**
 * Generic API response wrapper
 */
public class ApiResponse<T> {
    
    private final boolean success;
    private final T data;
    private final String errorMessage;
    private final int errorCode;
    
    private ApiResponse(boolean success, T data, String errorMessage, int errorCode) {
        this.success = success;
        this.data = data;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }
    
    /**
     * Create a successful response
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, 0);
    }
    
    /**
     * Create an error response with message
     */
    public static <T> ApiResponse<T> error(String errorMessage) {
        return new ApiResponse<>(false, null, errorMessage, 0);
    }
    
    /**
     * Create an error response with message and code
     */
    public static <T> ApiResponse<T> error(String errorMessage, int errorCode) {
        return new ApiResponse<>(false, null, errorMessage, errorCode);
    }
    
    /**
     * Create an error response from exception
     */
    public static <T> ApiResponse<T> error(Throwable throwable) {
        return new ApiResponse<>(false, null, throwable.getMessage(), 0);
    }
    
    // Getters
    public boolean isSuccess() {
        return success;
    }
    
    public boolean isError() {
        return !success;
    }
    
    public T getData() {
        return data;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public int getErrorCode() {
        return errorCode;
    }
    
    /**
     * Get data or throw exception if error
     */
    public T getDataOrThrow() throws ApiException {
        if (success) {
            return data;
        } else {
            throw new ApiException(errorMessage, errorCode);
        }
    }
    
    /**
     * Get data or return default value if error
     */
    public T getDataOrDefault(T defaultValue) {
        return success ? data : defaultValue;
    }
    
    @Override
    public String toString() {
        if (success) {
            return "ApiResponse{success=true, data=" + data + "}";
        } else {
            return "ApiResponse{success=false, errorMessage='" + errorMessage + "', errorCode=" + errorCode + "}";
        }
    }
    
    /**
     * API Exception class
     */
    public static class ApiException extends Exception {
        private final int errorCode;
        
        public ApiException(String message, int errorCode) {
            super(message);
            this.errorCode = errorCode;
        }
        
        public int getErrorCode() {
            return errorCode;
        }
    }
}