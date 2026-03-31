package com.example.Covoiturage.controller;
import com.example.Covoiturage.dto.*;
import com.example.Covoiturage.exception.ResourceNotFoundException;
import com.example.Covoiturage.model.*;
import com.example.Covoiturage.service.*;

import jakarta.validation.Valid;

import com.example.Covoiturage.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
// Every method return value is automatically serialized to JSON.
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepo;
    public AuthController(AuthService authService,UserRepository userRepo){
        this.authService=authService;
        this.userRepo=userRepo;
    }
    // ── POST /api/auth/register 
    // Frontend sends: { email, phone, password, role }
    // Returns: { success, userId, email, role, status }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request){
        User user = authService.creerCompte(request.getEmail(),request.getPhone(),request.getPassword(),request.getRole());  
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.success(user.getId(),user.getEmail(),user.getRole().name(),user.getStatus().name()));
    }
    //GET /api/auth/me
    // Called by the frontend on page load to check if the user is still logged in and get their role for routing.
    // If nobody is logged in, userDetails is null → returns 401.
    @GetMapping("/me")
    public ResponseEntity<AuthResponse>getCurrentUser(@AuthenticationPrincipal UserDetails userDetails){
        if(userDetails == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AuthResponse.error("Non authentifie"));
        }
         User user = userRepo.findByEmail(userDetails.getUsername())
            .orElseThrow(() ->
                new ResourceNotFoundException("User", userDetails.getUsername()));

            return ResponseEntity.ok(AuthResponse.success(
            user.getId(),
            user.getEmail(),
            user.getRole().name(),
            user.getStatus().name()
        ));                

            }
    // ── Global validation error handler for this controller ──
    // When @Valid fails, Spring throws MethodArgumentNotValidException.
    // This catches it and returns a clean JSON 
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<AuthResponse> handleValidationErrors(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {

        String firstError = ex.getBindingResult()
            .getFieldErrors()
            .get(0)
            .getDefaultMessage();

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(AuthResponse.error(firstError));
    }

    // ── IllegalArgumentException (e.g. email already used) ───
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AuthResponse> handleIllegalArgument(
            IllegalArgumentException ex) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)   // 409 — resource already exists
            .body(AuthResponse.error(ex.getMessage()));
    }



}
