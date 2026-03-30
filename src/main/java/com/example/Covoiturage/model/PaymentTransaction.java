package com.example.Covoiturage.model;
import java.time.LocalDateTime;

import com.example.Covoiturage.model.enums.PaymentStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private double montant;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private LocalDateTime dateTransaction;

    @OneToOne(mappedBy = "transaction")
    private Reservation reservation;

    public PaymentTransaction() {}

    public PaymentTransaction(double montant) {
        this.montant = montant;
        this.dateTransaction = LocalDateTime.now();
    }

    // simulated operations 
    public boolean autoriser() {
        this.status = PaymentStatus.AUTORISE;
        System.out.println("[TRANSACTION] Autorisation de " + montant + "€");
        return true;
    }

    public boolean capturer() {
        if (this.status != PaymentStatus.AUTORISE)
            throw new IllegalStateException("Transaction non autorisée");
        this.status = PaymentStatus.CAPTURE;
        System.out.println("[TRANSACTION] Capture de " + montant + "€");
        return true;
    }

    public boolean rembourser() {
        this.status = PaymentStatus.REMBOURSE;
        System.out.println("[TRANSACTION] Remboursement de " + montant + "€");
        return true;
    }

    public String getId() { return id; }
    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public LocalDateTime getDateTransaction() { return dateTransaction; }
    public void setDateTransaction(LocalDateTime d) { this.dateTransaction = d; }
    public Reservation getReservation() { return reservation; }
    public void setReservation(Reservation reservation) { this.reservation = reservation; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaymentTransaction t)) return false;
        return id != null && id.equals(t.id);
    }

    @Override public int hashCode() { return getClass().hashCode(); }

    @Override
    public String toString() {
        return "PaymentTransaction{id='" + id + "', montant=" + montant +
               ", status=" + status + "}";
    }
}  
