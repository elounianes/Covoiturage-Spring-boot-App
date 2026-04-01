package com.example.Covoiturage.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import com.example.Covoiturage.dto.*;
import com.example.Covoiturage.exception.ResourceNotFoundException;

import java.util.List;
import com.example.Covoiturage.model.*;
import com.example.Covoiturage.repository.ReservationRepository;
import com.example.Covoiturage.repository.TrajetRepository;
import com.example.Covoiturage.repository.UserRepository;
import com.example.Covoiturage.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;





@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final UserRepository userRepo;
    private final TrajetRepository trajetRepo;
    private final ReservationRepository reservationRepo;
    private final AuthService authService;


    public AdminController(UserRepository userRepo,
                            TrajetRepository trajetRepo,
                            ReservationRepository reservationRepo,
                            AuthService authService) {
        this.userRepo = userRepo;
        this.trajetRepo = trajetRepo;
        this.reservationRepo = reservationRepo;
        this.authService = authService;
    }
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers(){
        return ResponseEntity.ok(ApiResponse.success(userRepo.findAll()));

    }
    
    @GetMapping("/users/{id}")
    
    public ResponseEntity<ApiResponse<User>> getUserById(
            @PathVariable String id) {
        User user = userRepo.findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("Utilisateur", id));
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    @PutMapping("/users/{id}/suspendre")
    public ResponseEntity<ApiResponse<Void>> suspendreCompte(
            @PathVariable String id) {
        authService.suspendreCompte(id);
        return ResponseEntity.ok(ApiResponse.success(
            "Compte suspendu avec succès"));
    }
    @PutMapping("/users/{id}/bloquer")
    public ResponseEntity<ApiResponse<Void>> bloquerUtilisateur(
            @PathVariable String id) {
        authService.bloquerCompte(id);
        return ResponseEntity.ok(ApiResponse.success(
            "Utilisateur bloqué avec succès"));
    }
      @GetMapping("/trajets")
    public ResponseEntity<ApiResponse<List<Trajet>>> getAllTrajets() {
        return ResponseEntity.ok(
            ApiResponse.success(trajetRepo.findAll()));
    }

  
    @GetMapping("/reservations")
    public ResponseEntity<ApiResponse<List<Reservation>>>
            getAllReservations() {
        return ResponseEntity.ok(
            ApiResponse.success(reservationRepo.findAll()));
    }

}
