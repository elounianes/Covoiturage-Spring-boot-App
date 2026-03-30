package com.example.Covoiturage.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Covoiturage.model.MoyenPaiement;

public interface MoyenPaiementRepository extends JpaRepository<MoyenPaiement, String> {
    List<MoyenPaiement> findByPassagerId(String passagerId);
}