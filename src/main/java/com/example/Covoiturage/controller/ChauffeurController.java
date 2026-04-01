package com.example.Covoiturage.controller;


import com.example.Covoiturage.dto.ApiResponse;
import com.example.Covoiturage.dto.VehiculeRequest;
import com.example.Covoiturage.exception.ResourceNotFoundException;
import com.example.Covoiturage.model.Chauffeur;
import com.example.Covoiturage.model.Reservation;
import com.example.Covoiturage.model.Trajet;
import com.example.Covoiturage.model.Vehicule;
import com.example.Covoiturage.repository.ChauffeurRepository;
import com.example.Covoiturage.repository.ReservationRepository;
import com.example.Covoiturage.repository.TrajetRepository;
import com.example.Covoiturage.repository.VehiculeRepository;
import com.example.Covoiturage.service.EvaluationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/chauffeur")
@PreAuthorize("hasRole('CHAUFFEUR')")

public class ChauffeurController {

    private final ChauffeurRepository chauffeurRepo;
    private final VehiculeRepository vehiculeRepo;
    private final TrajetRepository trajetRepo;
    private final ReservationRepository reservationRepo;
    private final EvaluationService evaluationService;

    public ChauffeurController(ChauffeurRepository chauffeurRepo,
                                VehiculeRepository vehiculeRepo,
                                TrajetRepository trajetRepo,
                                ReservationRepository reservationRepo,
                                EvaluationService evaluationService) {
        this.chauffeurRepo = chauffeurRepo;
        this.vehiculeRepo = vehiculeRepo;
        this.trajetRepo = trajetRepo;
        this.reservationRepo = reservationRepo;
        this.evaluationService = evaluationService;
    }

    private Chauffeur getChauffeurFromSession(UserDetails userDetails) {
        return chauffeurRepo.findByEmail(userDetails.getUsername())
            .orElseThrow(() ->
                new ResourceNotFoundException("Chauffeur",
                    userDetails.getUsername()));
    }

    @GetMapping("/vehicules")
    public ResponseEntity<ApiResponse<List<Vehicule>>> getMesVehicules(@AuthenticationPrincipal UserDetails userDetails) {

        Chauffeur chauffeur = getChauffeurFromSession(userDetails);
        List<Vehicule> vehicules = vehiculeRepo
            .findByProprietaireId(chauffeur.getId());

        return ResponseEntity.ok(ApiResponse.success(vehicules));
    }

    @PostMapping("/vehicules")
    public ResponseEntity<ApiResponse<Vehicule>> ajouterVehicule(
            @Valid @RequestBody VehiculeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Chauffeur chauffeur = getChauffeurFromSession(userDetails);

        Vehicule vehicule = new Vehicule(
            request.getMarque(),
            request.getModele(),
            request.getCapaciteMax(),
            request.getPlaqueImmatriculation()
        );

        chauffeur.ajouterVehicule(vehicule);  
        vehiculeRepo.save(vehicule);
        chauffeurRepo.save(chauffeur);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(vehicule));
    }

    @GetMapping("/trajets")
    public ResponseEntity<ApiResponse<List<Trajet>>> mesTrajets(
            @AuthenticationPrincipal UserDetails userDetails) {

        Chauffeur chauffeur = getChauffeurFromSession(userDetails);
        List<Trajet> trajets = trajetRepo
            .findByChauffeurId(chauffeur.getId());

        return ResponseEntity.ok(ApiResponse.success(trajets));
    }

    @GetMapping("/trajets/{trajetId}/reservations")
    public ResponseEntity<ApiResponse<List<Reservation>>>
            getReservationsDuTrajet(
            @PathVariable String trajetId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Chauffeur chauffeur = getChauffeurFromSession(userDetails);

        // Fetch the trip and verify it belongs to this driver
        Trajet trajet = trajetRepo.findById(trajetId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Trajet", trajetId));

        if (!trajet.getChauffeur().getId().equals(chauffeur.getId())) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(
                    "Ce trajet ne vous appartient pas"));
        }

        List<Reservation> reservations = reservationRepo
            .findByTrajetId(trajetId);

        return ResponseEntity.ok(ApiResponse.success(reservations));
    }

   
    @GetMapping("/notes")
    public ResponseEntity<ApiResponse<Double>> getMesNotes(
            @AuthenticationPrincipal UserDetails userDetails) {

        Chauffeur chauffeur = getChauffeurFromSession(userDetails);
        double note = evaluationService
            .consulterNotesChauffeur(chauffeur.getId());

        return ResponseEntity.ok(ApiResponse.success(note));
    }
}
