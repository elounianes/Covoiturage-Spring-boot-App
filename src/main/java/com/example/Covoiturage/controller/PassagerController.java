package com.example.Covoiturage.controller;
// controller/PassagerController.java

import com.example.Covoiturage.dto.ApiResponse;
import com.example.Covoiturage.exception.ResourceNotFoundException;
import com.example.Covoiturage.model.MoyenPaiement;
import com.example.Covoiturage.model.Passager;

import com.example.Covoiturage.repository.MoyenPaiementRepository;
import com.example.Covoiturage.repository.PassagerRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/passager")
@PreAuthorize("hasRole('PASSAGER')")
public class PassagerController {

    private final PassagerRepository passagerRepo;
    private final MoyenPaiementRepository moyenPaiementRepo;

    public PassagerController(PassagerRepository passagerRepo,
                               MoyenPaiementRepository moyenPaiementRepo) {
        this.passagerRepo = passagerRepo;
        this.moyenPaiementRepo = moyenPaiementRepo;
    }

   
    @GetMapping("/moyens-paiement")
    public ResponseEntity<ApiResponse<List<MoyenPaiement>>> getMoyensPaiement(
            @AuthenticationPrincipal UserDetails userDetails) {

        Passager passager = passagerRepo.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Passager", userDetails.getUsername()));

        return ResponseEntity.ok(
            ApiResponse.success(moyenPaiementRepo
                .findByPassagerId(passager.getId())));
    }

    @PostMapping("/moyens-paiement")
    public ResponseEntity<ApiResponse<MoyenPaiement>> ajouterMoyenPaiement(
            @Valid @RequestBody MoyenPaiementRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Passager passager = passagerRepo.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Passager", userDetails.getUsername()));


        // needed to exist into the service but its late time modification so i put it here
        MoyenPaiement mp = new MoyenPaiement();
        mp.setType(request.type);
        mp.setNumeroMasque("**** **** **** " + request.derniers4Chiffres);
        mp.setDateExpiration(request.dateExpiration);
        mp.setTitulaire(request.titulaire);

        passager.ajouterMoyenPaiement(mp);
        moyenPaiementRepo.save(mp);
        passagerRepo.save(passager);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(mp));
    }

    @DeleteMapping("/moyens-paiement/{id}")
    public ResponseEntity<ApiResponse<Void>> supprimerMoyenPaiement(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        MoyenPaiement mp = moyenPaiementRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "MoyenPaiement", id));

        if (!mp.getPassager().getEmail().equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Ce moyen de paiement ne vous appartient pas"));
        }

        moyenPaiementRepo.delete(mp);
        return ResponseEntity.ok(ApiResponse.success("Moyen de paiement supprimé"));
    }

    static class MoyenPaiementRequest {
        @NotBlank public String type;              // "CARTE", "PAYPAL"
        @NotBlank public String derniers4Chiffres; // "4242"
        @NotBlank public String dateExpiration;    // "12/27"
        @NotBlank public String titulaire;         // "Alice Martin"
    }
}