package com.example.Covoiturage.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

@Entity
@DiscriminatorValue("PASSAGER")
public class Passager extends User {

    @OneToMany(mappedBy = "passager", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MoyenPaiement> moyensPaiement = new ArrayList<>();

    @OneToMany(mappedBy = "passager", cascade = CascadeType.ALL)
    private List<Reservation> historiqueReservations = new ArrayList<>();

    public Passager() {}

    public Passager(String email, String phone, String passwordHash) {
        super(email, phone, passwordHash);
    }

    public List<MoyenPaiement> getMoyenPaiement() {
        return Collections.unmodifiableList(new ArrayList<>(moyensPaiement));
    }

    public List<Reservation> getReservations() {
        return Collections.unmodifiableList(new ArrayList<>(historiqueReservations));
    }

    public void ajouterMoyenPaiement(MoyenPaiement mp) {
        mp.setPassager(this);
        this.moyensPaiement.add(mp);
    }

    public void ajouterReservation(Reservation r) {
        this.historiqueReservations.add(r);
    }

    public void evaluerChauffeur(Chauffeur chauffeur, int note) {
        if (note < 1 || note > 5)
            throw new IllegalArgumentException("La note doit être entre 1 et 5");
        chauffeur.ajouterNote(note);
    }
}