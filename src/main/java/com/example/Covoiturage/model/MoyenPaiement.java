package com.example.Covoiturage.model;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "moyens_paiement")
public class MoyenPaiement {
    private String id;

    private String type;
    private String numeroMasque;   
    private String dateExpiration;
    private String titulaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passager_id")
    private Passager passager;

    public MoyenPaiement() {}

    public boolean payer(double montant) {
        // Simulation: always succeeds
        System.out.println("[PAIEMENT] " + montant + "€ débité de " + numeroMasque);
        return true;
    }

    public boolean rembourser(double montant) {
        // Simulation: always succeeds
        System.out.println("[REMBOURSEMENT] " + montant + "€ remboursé sur " + numeroMasque);
        return true;
    }


    public String getId() { return id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getNumeroMasque() { return numeroMasque; }
    public void setNumeroMasque(String n) { this.numeroMasque = n; }
    public String getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(String d) { this.dateExpiration = d; }
    public String getTitulaire() { return titulaire; }
    public void setTitulaire(String t) { this.titulaire = t; }
    public Passager getPassager() { return passager; }
    public void setPassager(Passager passager) { this.passager = passager; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MoyenPaiement m)) return false;
        return id != null && id.equals(m.id);
    }

    @Override public int hashCode() { return getClass().hashCode(); }

    @Override
    public String toString() {
        return "MoyenPaiement{type='" + type + "', numero='" + numeroMasque + "'}";
    }
}
        
