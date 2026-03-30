package com.example.Covoiturage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Covoiturage.model.Reservation;
import com.example.Covoiturage.model.enums.ReservationStatus;

public interface ReservationRepository extends JpaRepository<Reservation, String> {
    List<Reservation> findByPassagerId(String passagerId);
    List<Reservation> findByTrajetId(String trajetId);
    List<Reservation> findByTrajetIdAndStatus(String trajetId, ReservationStatus status);
}
