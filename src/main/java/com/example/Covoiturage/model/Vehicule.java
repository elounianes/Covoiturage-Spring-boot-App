package com.example.Covoiturage.model;
import jakarta.persistence.*;

@Entity
@Table(name = "vehicules")
public class Vehicule{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String marque;
    private String modele;
    private int capaciteMax;
    private String plaqueImmatriculation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chauffeur_id")
    private Chauffeur proprietaire;

    public Vehicule() {}

    public Vehicule(String marque, String modele,
                    int capaciteMax, String plaque) {
        this.marque = marque;
        this.modele = modele;
        this.capaciteMax = capaciteMax;
        this.plaqueImmatriculation = plaque;
    }

    public String getId() { return id; }
    public String getMarque() { return marque; }
    public void setMarque(String marque) { this.marque = marque; }
    public String getModele() { return modele; }
    public void setModele(String modele) { this.modele = modele; }
    public int getCapaciteMax() { return capaciteMax; }
    public void setCapaciteMax(int capaciteMax) { this.capaciteMax = capaciteMax; }
    public String getPlaqueImmatriculation() { return plaqueImmatriculation; }
    public void setPlaqueImmatriculation(String p) { this.plaqueImmatriculation = p; }
    public Chauffeur getProprietaire() { return proprietaire; }
    public void setProprietaire(Chauffeur proprietaire) { this.proprietaire = proprietaire; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicule v)) return false;
        return id != null && id.equals(v.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }

    @Override
    public String toString() {
        return "Vehicule{id='" + id + "', marque='" + marque +
               "', modele='" + modele + "', plaque='" + plaqueImmatriculation + "'}";
    }
}

