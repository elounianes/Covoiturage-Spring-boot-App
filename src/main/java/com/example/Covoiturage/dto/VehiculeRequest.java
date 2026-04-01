// JSON shape when a driver adds a vehicle
package com.example.Covoiturage.dto;

import jakarta.validation.constraints.*;

public class VehiculeRequest {

    @NotBlank(message = "La marque est obligatoire")
    private String marque;

    @NotBlank(message = "Le modèle est obligatoire")
    private String modele;

    @Min(value = 1, message = "Au moins 1 place requise")
    private int capaciteMax;

    @NotBlank(message = "La plaque est obligatoire")
    private String plaqueImmatriculation;

    public String getMarque() { return marque; }
    public void setMarque(String m) { this.marque = m; }
    public String getModele() { return modele; }
    public void setModele(String m) { this.modele = m; }
    public int getCapaciteMax() { return capaciteMax; }
    public void setCapaciteMax(int c) { this.capaciteMax = c; }
    public String getPlaqueImmatriculation() { return plaqueImmatriculation; }
    public void setPlaqueImmatriculation(String p) {
        this.plaqueImmatriculation = p;
    }
}