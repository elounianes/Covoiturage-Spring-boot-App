package com.example.Covoiturage.controller;

import com.example.Covoiturage.dto.ApiResponse;
import com.example.Covoiturage.dto.TrajetRequest;
import com.example.Covoiturage.exception.ResourceNotFoundException;
import com.example.Covoiturage.model.Chauffeur;
import com.example.Covoiturage.model.Trajet;
import com.example.Covoiturage.model.Vehicule;
import com.example.Covoiturage.repository.ChauffeurRepository;
import com.example.Covoiturage.repository.VehiculeRepository;
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
// every return is JSON
@RequestMapping("/api/trajets")
public class TrajetController {

    private final TrajetService trajetService;
    private final ChauffeurRepository chauffeurRepo;
    private final VehiculeRepository VehiculeRepo;
     public TrajetController(TrajetService trajetService,ChauffeurRepository chauffeurRepo,VehiculeRepository vehiculeRepo) {
        this.trajetService = trajetService;
        this.chauffeurRepo = chauffeurRepo;
        this.VehiculeRepo = vehiculeRepo;
    }
    @GetMapping("/disponibles")
    public ResponseEntity<ApiResponse<List<Trajet>>> getTrajetDisponibles(@RequestParam(required = false) String origine,@RequestParam(required = false) String destination)
    {
        List<Trajet> trajets;
        if (origine != null && destination != null){
            trajets = trajetService.getTrajets(origine, destination);
        }
        else{
            trajets = trajetService.getTrajetsDiponibles();
        }
         return ResponseEntity.ok(ApiResponse.success(trajets));

    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Trajet>> getTrajetById(
            @PathVariable String id) {
        Trajet trajet = trajetService.getTrajet(id);
        return ResponseEntity.ok(ApiResponse.success(trajet));
    }
    @PostMapping
    @PreAuthorize("hasRole('CHAUFFEUR')") // if not chauffeur return 403
        // @AuthenticationPrincipal gives the logged-in user's details
    public ResponseEntity<ApiResponse<Trajet>>proposerTrajet( @Valid @RequestBody TrajetRequest request,@AuthenticationPrincipal UserDetails userDetails){
         Chauffeur chauffeur = chauffeurRepo
            .findByEmail(userDetails.getUsername())
            .orElseThrow(() ->
                new ResourceNotFoundException("Chauffeur",
                    userDetails.getUsername()));
        Vehicule vehicule = VehiculeRepo
            .findById(request.getVehiculeId())
            .orElseThrow(() ->
                new ResourceNotFoundException("Vehicule",
                    request.getVehiculeId()));

        // to fix !!!! a vehicule owner can not acces to all exsited vehicule frontend  fix !!

        if (!vehicule.getProprietaire().getId().equals(chauffeur.getId())) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(
                    "Ce véhicule ne vous appartient pas"));
        } 
          Trajet trajet = trajetService.proposerTrajet(
            chauffeur, vehicule,
            request.getOrigine(),
            request.getDestination(),
            request.getHeureDepart(),
            request.getPlacesTotales(),
            request.getPrixParPlace()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(trajet)); 

    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CHAUFFEUR')")
    public ResponseEntity<ApiResponse<Void>> cloreTrajet(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Trajet trajet = trajetService.getTrajet(id);

        if (!trajet.getChauffeur().getEmail()
                .equals(userDetails.getUsername())) {
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(
                    "Vous ne pouvez pas annuler le trajet d'un autre chauffeur"));
        }

        trajetService.cloreTrajet(id);
        return ResponseEntity.ok(ApiResponse.success(
            "Trajet clôturé avec succès"));
    }
    
}
