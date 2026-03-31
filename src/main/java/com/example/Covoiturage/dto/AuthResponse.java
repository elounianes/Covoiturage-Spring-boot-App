
// This is the JSON your backend sends back after register or /me
package com.example.Covoiturage.dto;

public class AuthResponse {

    private boolean success;
    private String message;
    private String userId;
    private String email;
    private String role;   
    private String status;

    // Private constructor — use the static factory methods below.
    // This pattern prevents accidentally creating half-built responses.
    private AuthResponse() {}

    // Factory method for success
    public static AuthResponse success(String userId, String email,
                                        String role, String status) {
        AuthResponse r = new AuthResponse();
        r.success = true;
        r.userId = userId;
        r.email = email;
        r.role = role;
        r.status = status;
        return r;
    }

    // Factory method for errors
    public static AuthResponse error(String message) {
        AuthResponse r = new AuthResponse();
        r.success = false;
        r.message = message;
        return r;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
}