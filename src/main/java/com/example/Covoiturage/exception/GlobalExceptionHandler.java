package com.example.Covoiturage.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// Every method here intercepts a specific exception type thrown
// anywhere in any controller and converts it to a clean JSON response.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Helper to build a consistent error response shape ─
    // Every error your API returns will look like:
    // { "error": "...", "status": 404, "timestamp": "..." }
    // This consistency is critical — your frontend can handle
    // one error shape instead of guessing at the structure.
    private ResponseEntity<Map<String, Object>> buildError(
            HttpStatus status, String message) {

        Map<String, Object> body = new HashMap<>();
        body.put("error", message);
        body.put("status", status.value());
        body.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.status(status).body(body);
    }

    // 404 — resource not found (trajet, reservation, user)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            ResourceNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 409 — booking a full trip
    @ExceptionHandler(TrajetCompletException.class)
    public ResponseEntity<Map<String, Object>> handleTrajetComplet(
            TrajetCompletException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage());
    }

    // 402 — payment failed
    @ExceptionHandler(PaiementEchouéException.class)
    public ResponseEntity<Map<String, Object>> handlePaiement(
            PaiementEchouéException ex) {
        return buildError(HttpStatus.PAYMENT_REQUIRED, ex.getMessage());
    }

    // 400 — late cancellation (still processes partial refund first)
    @ExceptionHandler(AnnulationHorsDelaiException.class)
    public ResponseEntity<Map<String, Object>> handleAnnulation(
            AnnulationHorsDelaiException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 403 — suspended or blocked user trying to log in
    @ExceptionHandler(UtilisateurInactifException.class)
    public ResponseEntity<Map<String, Object>> handleInactif(
            UtilisateurInactifException ex) {
        return buildError(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // 400 — generic bad input (wrong role, invalid seat count, etc.)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegal(
            IllegalArgumentException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 500 — anything unexpected — never expose raw stack traces
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        ex.printStackTrace(); // log it server-side
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR,
            "Une erreur interne est survenue");
    }
}
