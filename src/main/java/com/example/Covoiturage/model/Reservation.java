package com.example.Covoiturage.model;

import java.time.LocalDateTime;

import com.example.Covoiturage.model.enums.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "reservations")

public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trajet_id", nullable = false)
    @JsonIgnoreProperties({
    "reservations",   
    "chauffeur",           
    "hibernateLazyInitializer",
    "handler"
})
    private Trajet trajet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passager_id", nullable = false)
    @JsonIgnore

    private Passager passager;

    private int nombrePlaces;
    private double prixTotal;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status = ReservationStatus.EN_ATTENTE;

    private LocalDateTime dateReservation = LocalDateTime.now();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "transaction_id")
    private PaymentTransaction transaction;

    public Reservation() {}

    public Reservation(Trajet trajet, Passager passager, int nombrePlaces) {
        this.trajet = trajet;
        this.passager = passager;
        this.nombrePlaces = nombrePlaces;
        this.prixTotal = trajet.getPrixParPlace() * nombrePlaces;
        this.dateReservation = LocalDateTime.now();
    }

    public boolean isPlusDe24hAvantDepart() {
        LocalDateTime limite = trajet.getHeureDepart().minusHours(24);
        return LocalDateTime.now().isBefore(limite);
    }



    public void rembourserReservation() {
        if (this.transaction != null) {
            this.transaction.rembourser();
        }
    }

    public String getId() { return id; }
    public Trajet getTrajet() { return trajet; }
    public void setTrajet(Trajet trajet) { this.trajet = trajet; }
    public Passager getPassager() { return passager; }
    public void setPassager(Passager passager) { this.passager = passager; }
    public int getNombrePlaces() { return nombrePlaces; }
    public void setNombrePlaces(int n) { this.nombrePlaces = n; }
    public double getPrixTotal() { return prixTotal; }
    public void setPrixTotal(double p) { this.prixTotal = p; }
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
    public LocalDateTime getDateReservation() { return dateReservation; }
    public PaymentTransaction getTransaction() { return transaction; }
    public void setTransaction(PaymentTransaction transaction) { this.transaction = transaction; }
    public String getPassagerEmail() {
    return passager != null ? passager.getEmail() : null;
}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation r)) return false;
        return id != null && id.equals(r.id);
    }

    @Override public int hashCode() { return getClass().hashCode(); }

    @Override
    public String toString() {
        return "Reservation{id='" + id + "', passager=" + passager.getEmail() +
               ", trajet=" + trajet.getId() + ", status=" + status +
               ", places=" + nombrePlaces + ", total=" + prixTotal + "€}";
    }
}