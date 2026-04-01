package com.example.Covoiturage.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Covoiturage.model.Chauffeur;

public interface ChauffeurRepository extends JpaRepository<Chauffeur, String> {
    Optional<Chauffeur> findByEmail(String email);
    Optional<Chauffeur> findById(String id);
}
