package com.example.Covoiturage.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "moyens_paiement")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

public class MoyenPaiement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String type;
    private String numeroMasque;   
    private String dateExpiration;
    private String titulaire;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passager_id")
    private Passager passager;

    public MoyenPaiement() {}
//  always stimulated to work!!
    public boolean payer(double montant) {
        System.out.println("[PAIEMENT] " + montant + "DT débité de " + numeroMasque);
        return true;
    }
//  always stimulated to work!!
    public boolean rembourser(double montant) {
        System.out.println("[REMBOURSEMENT] " + montant + "DT remboursé sur " + numeroMasque);
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
        
