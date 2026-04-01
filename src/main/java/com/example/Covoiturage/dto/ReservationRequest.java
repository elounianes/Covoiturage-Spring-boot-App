package com.example.Covoiturage.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
public class ReservationRequest {

    @NotBlank(message = "L'identifiant du trajet est obligatoire")
    private String trajetId;

    @Min(value = 1, message = "Au moins 1 place requise")
    private int nombrePlaces;

    public String getTrajetId() { return trajetId; }
    public void setTrajetId(String t) { this.trajetId = t; }
    public int getNombrePlaces() { return nombrePlaces; }
    public void setNombrePlaces(int n) { this.nombrePlaces = n; }

}
