package com.example.Covoiturage.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.Covoiturage.model.enums.TrajetStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "trajets")
public class Trajet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chauffeur_id", nullable = false)
    private Chauffeur chauffeur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicule_id", nullable = false)
    private Vehicule vehicule;

    @Column(nullable = false)
    private String origine;

    @Column(nullable = false)
    private String destination;

    @Column(nullable = false)
    private LocalDateTime heureDepart;

    private int placesTotales;
    private int placesReservees = 0;
    private double prixParPlace;

    @Enumerated(EnumType.STRING)
    private TrajetStatus status = TrajetStatus.PREVU;

    @OneToMany(mappedBy = "trajet", cascade = CascadeType.ALL)
    private List<Reservation> reservations = new ArrayList<>();

    public Trajet() {}

    public Trajet(Chauffeur chauffeur, Vehicule vehicule, String origine,
                  String destination, LocalDateTime heureDepart,
                  int placesTotales, double prixParPlace) {
        this.chauffeur = chauffeur;
        this.vehicule = vehicule;
        this.origine = origine;
        this.destination = destination;
        this.heureDepart = heureDepart;
        this.placesTotales = placesTotales;
        this.prixParPlace = prixParPlace;
    }


    public void ajouterPassager(Reservation res) {
        if (isComplet())
            throw new IllegalStateException("Trajet complet");
        if (res.getNombrePlaces() > getPlacesDisponibles())
            throw new IllegalStateException("Pas assez de places disponibles");

        this.reservations.add(res);
        this.placesReservees += res.getNombrePlaces();

        if (this.placesReservees >= this.placesTotales) {
            this.status = TrajetStatus.COMPLET;
        }
    }

    public void retirerPassager(Reservation res) {
        this.reservations.remove(res);
        this.placesReservees -= res.getNombrePlaces();

        if (this.status == TrajetStatus.COMPLET) {
            this.status = TrajetStatus.PREVU;
        }
    }

    public void cloreTrajet() {
        this.status = TrajetStatus.TERMINE;
    }

    public boolean isComplet() {
        return this.status == TrajetStatus.COMPLET ||
               this.placesReservees >= this.placesTotales;
    }

    public int getPlacesDisponibles() {
        return this.placesTotales - this.placesReservees;
    }

    public boolean peutEtreAnnuleParChauffeur() {
        return reservations.stream()
            .noneMatch(r -> r.getStatus() ==
                com.example.Covoiturage.model.enums.ReservationStatus.CONFIRMEE);
    }

    public List<Reservation> getReservations() {
        return Collections.unmodifiableList(new ArrayList<>(reservations));
    }

    public String getId() { return id; }
    public Chauffeur getChauffeur() { return chauffeur; }
    public void setChauffeur(Chauffeur c) { this.chauffeur = c; }
    public Vehicule getVehicule() { return vehicule; }
    public void setVehicule(Vehicule v) { this.vehicule = v; }
    public String getOrigine() { return origine; }
    public void setOrigine(String o) { this.origine = o; }
    public String getDestination() { return destination; }
    public void setDestination(String d) { this.destination = d; }
    public LocalDateTime getHeureDepart() { return heureDepart; }
    public void setHeureDepart(LocalDateTime h) { this.heureDepart = h; }
    public int getPlacesTotales() { return placesTotales; }
    public void setPlacesTotales(int p) { this.placesTotales = p; }
    public int getPlacesReservees() { return placesReservees; }
    public void setPlacesReservees(int p) { this.placesReservees = p; }
    public double getPrixParPlace() { return prixParPlace; }
    public void setPrixParPlace(double p) { this.prixParPlace = p; }
    public TrajetStatus getStatus() { return status; }
    public void setStatus(TrajetStatus status) { this.status = status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Trajet t)) return false;
        return id != null && id.equals(t.id);
    }

    @Override public int hashCode() { return getClass().hashCode(); }

    @Override
    public String toString() {
        return "Trajet{id='" + id + "', " + origine + " → " + destination +
               ", depart=" + heureDepart + ", status=" + status +
               ", places=" + placesReservees + "/" + placesTotales + "}";
    }
}