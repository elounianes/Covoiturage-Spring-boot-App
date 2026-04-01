package com.example.Covoiturage.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Covoiturage.model.Passager;

public interface PassagerRepository extends JpaRepository<Passager, String> {
    Optional<Passager> findByEmail(String email);
    Optional<Passager> findById(String id);

}