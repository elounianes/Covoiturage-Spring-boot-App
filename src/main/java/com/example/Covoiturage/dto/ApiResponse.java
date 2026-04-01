package com.example.Covoiturage.dto;
// A generic wrapper for every endpoint response.
// success=true means the operation worked.
// success=false means something went wrong.
// data holds whatever the endpoint returns (can be any object).
public class ApiResponse <T> {
    private boolean success;
    private String message;
    private T data;

   // private ApiRespone() {}
    // use when succes = true
    public static <T> ApiResponse<T> success(T data){
        ApiResponse<T> r =new ApiResponse<>();
        r.success = true;
        r.data=data;
        return r;
    }
    // use when message exist
     public static <T> ApiResponse<T> success(String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.message = message;
        return r;
    }
     // Use when something went wrong
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = false;
        r.message = message;
        return r;
    }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    
}
