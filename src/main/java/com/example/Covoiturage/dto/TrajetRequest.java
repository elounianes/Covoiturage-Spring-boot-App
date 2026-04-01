package com.example.Covoiturage.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.*;
import jakarta.validation.constraints.NotBlank;
public class TrajetRequest {
   
    @NotBlank(message = "L'origine est obligatoire")
    private String origine;

    @NotBlank(message = "La destination est obligatoire")
    private String destination;

    @NotNull(message = "L'heure de départ est obligatoire")
    @Future(message = "L'heure de départ doit être dans le futur")
    private LocalDateTime heureDepart;

    @Min(value = 1, message = "Au moins 1 place requise")
    private int placesTotales;

    @DecimalMin(value = "0.0", message = "Le prix ne peut pas être négatif")
    private double prixParPlace;

    @NotBlank(message = "L'identifiant du véhicule est obligatoire")
    private String vehiculeId;

    // Getters and setters
    public String getOrigine() { return origine; }
    public void setOrigine(String o) { this.origine = o; }
    public String getDestination() { return destination; }
    public void setDestination(String d) { this.destination = d; }
    public LocalDateTime getHeureDepart() { return heureDepart; }
    public void setHeureDepart(LocalDateTime h) { this.heureDepart = h; }
    public int getPlacesTotales() { return placesTotales; }
    public void setPlacesTotales(int p) { this.placesTotales = p; }
    public double getPrixParPlace() { return prixParPlace; }
    public void setPrixParPlace(double p) { this.prixParPlace = p; }
    public String getVehiculeId() { return vehiculeId; }
    public void setVehiculeId(String v) { this.vehiculeId = v; }
}
    


