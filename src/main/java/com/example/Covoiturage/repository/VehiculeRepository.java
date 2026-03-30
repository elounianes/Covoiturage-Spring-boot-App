package com.example.Covoiturage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Covoiturage.model.Vehicule;

public interface VehiculeRepository extends JpaRepository<Vehicule, String> {
    List<Vehicule> findByProprietaireId(String chauffeurId);
}