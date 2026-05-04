package com.example.Covoiturage.controller;


import com.example.Covoiturage.dto.ApiResponse;
import com.example.Covoiturage.exception.ResourceNotFoundException;
import com.example.Covoiturage.model.Chauffeur;
import com.example.Covoiturage.model.Passager;
import com.example.Covoiturage.repository.ChauffeurRepository;
import com.example.Covoiturage.repository.PassagerRepository;
import com.example.Covoiturage.service.EvaluationService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final ChauffeurRepository chauffeurRepo;
    private final PassagerRepository passagerRepo;

    public EvaluationController(EvaluationService evaluationService,
                                 ChauffeurRepository chauffeurRepo,
                                 PassagerRepository passagerRepo) {
        this.evaluationService = evaluationService;
        this.chauffeurRepo = chauffeurRepo;
        this.passagerRepo = passagerRepo;
    }

    @PostMapping("/chauffeur/{chauffeurId}")
    @PreAuthorize("hasRole('PASSAGER')")
    public ResponseEntity<ApiResponse<Void>> evaluerChauffeur(
            @PathVariable String chauffeurId,
            @RequestBody NoteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Chauffeur chauffeur = chauffeurRepo.findById(chauffeurId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Chauffeur", chauffeurId));

        Passager passager = passagerRepo.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Passager", userDetails.getUsername()));

        evaluationService.evaluerChauffeur(chauffeur.getId(), passager.getId(), request.note);

        return ResponseEntity.ok(ApiResponse.success("Évaluation enregistrée — merci !"));
    }

    static class NoteRequest {
        @Min(1) @Max(5)
        public int note;
    }
}