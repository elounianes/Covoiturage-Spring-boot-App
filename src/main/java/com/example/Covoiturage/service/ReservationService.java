package com.example.Covoiturage.service;

import com.example.Covoiturage.model.Passager;
import com.example.Covoiturage.model.Reservation;
import com.example.Covoiturage.model.Trajet;
import java.util.List;

public interface ReservationService {
    Reservation creerReservation(Passager passager, Trajet trajet, int places);
    void confirmerReservation(String reservationId);
    void annulerReservation(String reservationId, boolean isDriverCancel);
    List<Reservation> getReservationsByPassager(String passagerId);
    Reservation getReservationByreservationId(String reservationId);
}