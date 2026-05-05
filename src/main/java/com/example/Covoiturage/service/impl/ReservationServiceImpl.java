package com.example.Covoiturage.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.Covoiturage.exception.PaiementEchouéException;
import com.example.Covoiturage.exception.ResourceNotFoundException;
import com.example.Covoiturage.exception.TrajetCompletException;
import com.example.Covoiturage.model.MoyenPaiement;
import com.example.Covoiturage.model.Passager;
import com.example.Covoiturage.model.PaymentTransaction;
import com.example.Covoiturage.model.Reservation;
import com.example.Covoiturage.model.Trajet;
import com.example.Covoiturage.model.enums.ReservationStatus;
import com.example.Covoiturage.repository.ReservationRepository;
import com.example.Covoiturage.repository.TrajetRepository;
import com.example.Covoiturage.service.NotificationService;
import com.example.Covoiturage.service.PaiementService;
import com.example.Covoiturage.service.ReservationService;
import com.example.Covoiturage.repository.MoyenPaiementRepository;
import com.example.Covoiturage.repository.PaymentTransactionRepository;

import jakarta.transaction.Transactional;
@Transactional
@Service
public class ReservationServiceImpl implements ReservationService{
    private final ReservationRepository reservationRepository;
    private final TrajetRepository trajetRepository;
    private final PaiementService paiementService;
    private final NotificationService notificationService;
    private final MoyenPaiementRepository moyenPaiementRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;

    public ReservationServiceImpl(ReservationRepository reservationRepository, TrajetRepository trajetRepository, MoyenPaiementRepository moyenPaiementRepository, PaiementService paiementService, NotificationService notificationService, PaymentTransactionRepository paymentTransactionRepository) {
        this.reservationRepository = reservationRepository;
        this.trajetRepository = trajetRepository;
        this.moyenPaiementRepository = moyenPaiementRepository;
        this.paiementService = paiementService;
        this.notificationService = notificationService;
        this.paymentTransactionRepository = paymentTransactionRepository;
    }
    @Override
    public Reservation creerReservation(Passager passager, Trajet trajet, int nombrePlaces) {
        if (trajet.getPlacesDisponibles() < nombrePlaces) {
            throw new IllegalArgumentException("Seulement " + trajet.getPlacesDisponibles() + " place(s) disponible(s), vous en demandez " + nombrePlaces);
        }
        List<MoyenPaiement> moyens = moyenPaiementRepository.findByPassagerId(passager.getId());

    if (moyens.isEmpty())
        {throw new PaiementEchouéException("Vous devez ajouter un moyen de paiement " +"avant de pouvoir réserver un trajet. " +"Rendez-vous dans votre espace passager.");}
    
    
    
        if (trajet.isComplet()) {
            throw new TrajetCompletException("Trajet complet");
        }
        Reservation reservation = new Reservation(trajet, passager, nombrePlaces);
        reservationRepository.save(reservation);
        notificationService.notfierUser(passager, "Demande de réservation envoyée","Votre demande pour " + trajet.getOrigine() + " vers " +trajet.getDestination() +" est en attente de confirmation du chauffeur.");
        notificationService.notfierUser(trajet.getChauffeur(), "Nouvelle demande de réservation", passager.getEmail() + " demande " + nombrePlaces + " place(s) sur votre trajet " + trajet.getOrigine() + " vers " + trajet.getDestination() + ". Veuillez confirmer ou refuser.");

        return reservation;
    }
    
    @Override
    public void confirmerReservation(String reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("Reservation non trouvée"));

        if (reservation.getTrajet().getStatus() ==
                com.example.Covoiturage.model.enums.TrajetStatus.ANNULE) {
            throw new IllegalStateException(
                "Impossible de confirmer une réservation sur un trajet annulé");
        }

        reservation.setStatus(ReservationStatus.CONFIRMEE);
        
        paiementService.payer(reservation);
        paiementService.capturerPaiement(reservation);
        
        List<MoyenPaiement> moyens = moyenPaiementRepository
        .findByPassagerId(reservation.getPassager().getId());

        MoyenPaiement moyenUtilise = moyens.get(0);

    System.out.println("[PAIEMENT] Carte utilisée : "
    + moyenUtilise.getNumeroMasque()
    + " pour " + reservation.getPrixTotal() + "DT");
        
        reservation.getTrajet().ajouterPassager(reservation);
        reservationRepository.save(reservation);
        trajetRepository.save(reservation.getTrajet());
        notificationService.notfierUser(reservation.getPassager(),
        "Réservation confirmée !",
        "Votre réservation pour " +
        reservation.getTrajet().getOrigine() + " vers " +
        reservation.getTrajet().getDestination() +
        " a été confirmée par le chauffeur. Paiement de " +
        reservation.getPrixTotal() + "DT effectué.");

    
    }
    @Override
    public void annulerReservation(String reservationId,boolean isDriverCancel) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("Reservation non trouvée"));
            if(reservation.getStatus() == ReservationStatus.ANNULEE){
                throw new IllegalArgumentException("Reservation déjà annulée");
            }
            if (reservation.getTrajet().getStatus() ==
                    com.example.Covoiturage.model.enums.TrajetStatus.ANNULE) {
                throw new IllegalStateException(
                    "Ce trajet est déjà annulé — aucune action sur ses réservations n'est possible");
            }
            boolean avantDelai = reservation.isPlusDe24hAvantDepart();
            double prixTotal   = reservation.getPrixTotal();
            if(isDriverCancel){
                if(avantDelai){
                    paiementService.rembourser(reservation,prixTotal);
                }else{
                    double remboursement = prixTotal * 1.2 ;
                    paiementService.rembourser(reservation,remboursement);
                    notificationService.notfierUser(reservation.getPassager(),"Annulation de reservation","Votre reservation a été annulée moins de 24h avant le départ. " + "Une pénalité de 20% vous sera remboursée en supplément.");
                    notificationService.notfierUser(reservation.getTrajet().getChauffeur(),"Annulation de reservation","Une reservation a été annulée moins de 24h avant le départ. " + "Une pénalité de 20% vous sera déduite.");
               }
            

            }
            else{
                if(avantDelai){
                    paiementService.rembourser(reservation,prixTotal);
                    notificationService.notfierUser(reservation.getPassager(),"Annulation de reservation","Votre reservation a été annulée. " + "Un remboursement de "+prixTotal+"DT vous a été effectué.");
                }
                else{
                    double remboursement = prixTotal *0.5;
                    paiementService.rembourser(reservation,remboursement);
                    notificationService.notfierUser(reservation.getPassager(),"Annulation de reservation","Votre reservation a été annulée moins de 24h avant le départ. " + "Une pénalité de 50% vous sera remboursée en supplément.");
                    
                    
                }
                notificationService.notfierUser(reservation.getTrajet().getChauffeur(),"Annulation de reservation","Une reservation a été annulée moins de 24h avant le départ. " + "Une pénalité de 50% vous sera déduite.");

            }
            reservation.setStatus(ReservationStatus.ANNULEE);
            reservation.getTrajet().retirerPassager(reservation);
            reservationRepository.save(reservation);
            trajetRepository.save(reservation.getTrajet());
    }

    @Override
    public void refuserReservation(String reservationId){
       Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("Reservation non trouvée"));
        if (reservation.getStatus() != ReservationStatus.EN_ATTENTE) {
            throw new IllegalArgumentException("Seules les réservations en attente peuvent être refusées");
        }
        reservation.setStatus(ReservationStatus.ANNULEE);
        reservationRepository.save(reservation);
        notificationService.notfierUser(reservation.getPassager(),
        "Réservation refusée",
        "Votre demande pour " +
        reservation.getTrajet().getOrigine() + " → " +
        reservation.getTrajet().getDestination() +
        " a été refusée par le chauffeur. Aucun paiement n'a été effectué.");
}   
    @Override
    public List<Reservation> getReservationsByPassager(String passagerId) {
        return reservationRepository.findByPassagerId(passagerId);
    }
    @Override
    public Reservation getReservationByreservationId(String reservationId) {
            return reservationRepository.findById(reservationId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Reservation", reservationId));
    }

}