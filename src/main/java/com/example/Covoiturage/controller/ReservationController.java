package com.example.Covoiturage.controller;

import com.example.Covoiturage.dto.ApiResponse;
import com.example.Covoiturage.dto.ReservationRequest;
import com.example.Covoiturage.exception.ResourceNotFoundException;
import com.example.Covoiturage.model.Passager;
import com.example.Covoiturage.model.Reservation;
import com.example.Covoiturage.model.Trajet;
import com.example.Covoiturage.repository.PassagerRepository;
import com.example.Covoiturage.service.ReservationService;
import com.example.Covoiturage.service.TrajetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final TrajetService trajetService;
    private final PassagerRepository passagerRepo;

    public ReservationController(ReservationService reservationService,
                                  TrajetService trajetService,
                                  PassagerRepository passagerRepo) {
        this.reservationService = reservationService;
        this.trajetService = trajetService;
        this.passagerRepo = passagerRepo;
    }

    @PostMapping
    @PreAuthorize("hasRole('PASSAGER')")
    public ResponseEntity<ApiResponse<Reservation>> creerReservation(@Valid @RequestBody ReservationRequest request,@AuthenticationPrincipal UserDetails userDetails) {

        Passager passager = passagerRepo
            .findByEmail(userDetails.getUsername())
            .orElseThrow(() ->
                new ResourceNotFoundException("Passager",
                    userDetails.getUsername()));

        // Resolve the trip
        Trajet trajet = trajetService
            .getTrajet(request.getTrajetId());

        Reservation reservation = reservationService
            .creerReservation(passager, trajet, request.getNombrePlaces());

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(reservation));
    }

    
    @GetMapping("/mes-reservations")
    @PreAuthorize("hasRole('PASSAGER')")
    public ResponseEntity<ApiResponse<List<Reservation>>> getMesReservations(
            @AuthenticationPrincipal UserDetails userDetails) {

        Passager passager = passagerRepo
            .findByEmail(userDetails.getUsername())
            .orElseThrow(() ->
                new ResourceNotFoundException("Passager",
                    userDetails.getUsername()));

        List<Reservation> reservations = reservationService
            .getReservationsByPassager(passager.getId());

        return ResponseEntity.ok(ApiResponse.success(reservations));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PASSAGER')")
    public ResponseEntity<ApiResponse<Reservation>> getReservation(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Reservation reservation = reservationService
            .getReservationByreservationId(id);

        // Ownership check — you can only view your own reservation
        if (!reservation.getPassager().getEmail()
                .equals(userDetails.getUsername())) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(
                    "Cette réservation ne vous appartient pas"));
        }

        return ResponseEntity.ok(ApiResponse.success(reservation));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PASSAGER')")
    public ResponseEntity<ApiResponse<Void>> annulerReservation(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Reservation reservation = reservationService
            .getReservationByreservationId(id);

        if (!reservation.getPassager().getEmail()
                .equals(userDetails.getUsername())) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(
                    "Vous ne pouvez pas annuler la réservation d'un autre passager"));
        }

        // isDriverCancel = false → passenger initiated this cancellation
        reservationService.annulerReservation(id, false);

        return ResponseEntity.ok(ApiResponse.success(
            "Réservation annulée — remboursement traité"));
    }

    @PostMapping("/{id}/confirmer")
    @PreAuthorize("hasRole('CHAUFFEUR')")
    public ResponseEntity<ApiResponse<Void>> confirmerReservation(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Reservation reservation = reservationService
            .getReservationByreservationId(id);

        if (!reservation.getTrajet().getChauffeur().getEmail()
                .equals(userDetails.getUsername())) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(
                    "Vous ne pouvez confirmer que vos propres réservations"));
        }

        reservationService.confirmerReservation(id);

        return ResponseEntity.ok(ApiResponse.success(
            "Réservation confirmée — paiement capturé"));
    }
}