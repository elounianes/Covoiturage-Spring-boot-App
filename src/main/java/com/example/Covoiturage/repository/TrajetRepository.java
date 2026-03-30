package com.example.Covoiturage.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Covoiturage.model.Trajet;
import com.example.Covoiturage.model.enums.TrajetStatus;

public interface TrajetRepository extends JpaRepository<Trajet, String> {
    List<Trajet> findByOrigineAndDestinationAndStatus(
        String origine, String destination, TrajetStatus status);

    List<Trajet> findByChauffeurId(String chauffeurId);

    List<Trajet> findByStatus(TrajetStatus status);
}