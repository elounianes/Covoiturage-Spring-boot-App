package com.example.Covoiturage.model;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "chauffeurs")
@DiscriminatorValue("CHAUFFEUR")
public class Chauffeur extends User {

    @OneToMany(mappedBy = "proprietaire", cascade = CascadeType.ALL)
    private List<Vehicule> vehicules = new ArrayList<>();

    @OneToMany(mappedBy = "chauffeur", cascade = CascadeType.ALL)
    private List<Trajet> trajetsProposes = new ArrayList<>();

    private double noteMoyenne = 0.0;
    private int nombreEvaluations = 0;

    public Chauffeur() {}

    public Chauffeur(String email, String phone, String passwordHash) {
        super(email, phone, passwordHash);
    }

    public List<Vehicule> getVehicules() {
        return Collections.unmodifiableList(new ArrayList<>(vehicules));
    }

    public List<Trajet> getTrajets() {
        return Collections.unmodifiableList(new ArrayList<>(trajetsProposes));
    }

    public void ajouterVehicule(Vehicule v) {
        v.setProprietaire(this);
        this.vehicules.add(v);
    }

    public void ajouterTrajet(Trajet t) {
        this.trajetsProposes.add(t);
    }

    public double consulterNotesChauffeur() {
        return this.noteMoyenne;
    }

    public void ajouterNote(int note) {
        this.nombreEvaluations++;
        this.noteMoyenne = ((noteMoyenne * (nombreEvaluations - 1)) + note)
                           / nombreEvaluations;
    }

    public double getNoteMoyenne() { return noteMoyenne; }
    public void setNoteMoyenne(double n) { this.noteMoyenne = n; }
    public int getNombreEvaluations() { return nombreEvaluations; }
    public void setNombreEvaluations(int n) { this.nombreEvaluations = n; }
}