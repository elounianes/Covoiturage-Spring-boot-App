package com.example.Covoiturage.service.impl;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.Covoiturage.exception.PaiementEchouéException;
import com.example.Covoiturage.model.MoyenPaiement;
import com.example.Covoiturage.model.PaymentTransaction;
import com.example.Covoiturage.model.Reservation;
import com.example.Covoiturage.repository.MoyenPaiementRepository;
import com.example.Covoiturage.repository.PaymentTransactionRepository;
import com.example.Covoiturage.repository.ReservationRepository;
import com.example.Covoiturage.service.PaiementService;

import jakarta.transaction.Transactional;
@Service
public class PaiementServiceImpl implements PaiementService {
    private final ReservationRepository reservationRepository;
    private final MoyenPaiementRepository moyenPaiementRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    public PaiementServiceImpl(ReservationRepository reservationRepository,MoyenPaiementRepository moyenPaiementRepository,PaymentTransactionRepository paymentTransactionRepository) {
        this.reservationRepository = reservationRepository;
        this.moyenPaiementRepository = moyenPaiementRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
    }
    @Override
    public void payer(Reservation reservation) {
                List<MoyenPaiement> moyens = moyenPaiementRepository.findByPassagerId(reservation.getPassager().getId());
                if (moyens.isEmpty()) {
            throw new PaiementEchouéException("Aucun moyen de paiement disponible");
        }
        MoyenPaiement moyen = moyens.get(0);
        // dima temchi 5tr payer traj3 dima true
        if (!moyen.payer(reservation.getPrixTotal())) {
            throw new PaiementEchouéException("Paiement refusé");
        }
        PaymentTransaction transaction = new PaymentTransaction(reservation.getPrixTotal());
        transaction.autoriser();
        reservation.setTransaction(transaction);
        reservationRepository.save(reservation);
    }
    
    @Transactional
    @Override
    public void capturerPaiement(Reservation reservation) {
        PaymentTransaction transaction = reservation.getTransaction();
        if (transaction == null) {
            throw new PaiementEchouéException("Aucune transaction à capturer");
        }
        if (!transaction.capturer()) {
            throw new PaiementEchouéException("Capture échouée");
        }
        transaction.capturer();
        reservationRepository.save(reservation);
    }
    @Override
    public void rembourser(Reservation reservation,double montant) {
        PaymentTransaction transaction = reservation.getTransaction();
        if (transaction == null) {
            throw new PaiementEchouéException("Aucune transaction à rembourser");
        }
        List<MoyenPaiement> moyens = moyenPaiementRepository.findByPassagerId(reservation.getPassager().getId());
        if (!moyens.isEmpty()) {
            moyens.get(0).rembourser(montant);   // simulated refund
        }
        transaction.rembourser();
        paymentTransactionRepository.save(transaction);
    }
    @Override
    public List<MoyenPaiement> getMoyenPaiement(String passagerId) {
        return moyenPaiementRepository.findByPassagerId(passagerId);
    }

}
