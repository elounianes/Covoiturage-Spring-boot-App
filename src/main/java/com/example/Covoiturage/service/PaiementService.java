package com.example.Covoiturage.service;
import com.example.Covoiturage.model.Reservation;
import com.example.Covoiturage.model.MoyenPaiement;
import java.util.List;
public interface PaiementService {
    void payer(Reservation reservation);
    void capturerPaiement(Reservation reservation);
    void rembourser(Reservation reservation,double montant);
    List<MoyenPaiement> getMoyenPaiement(String passagerId);
    
}
