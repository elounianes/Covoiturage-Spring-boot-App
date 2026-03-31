package com.example.Covoiturage.service.impl;

import org.springframework.stereotype.Service;
import java.util.List;
import com.example.Covoiturage.repository.ReservationRepository;
import com.example.Covoiturage.repository.TrajetRepository;
import com.example.Covoiturage.model.*;
import com.example.Covoiturage.model.enums.*;
import com.example.Covoiturage.service.NotificationService;
import com.example.Covoiturage.service.PaiementService;
import com.example.Covoiturage.service.ReservationService;
import com.example.Covoiturage.exception.*;
import jakarta.transaction.Transactional;
@Transactional
@Service
public class ReservationServiceImpl implements ReservationService{
    private final ReservationRepository reservationRepository;
    private final TrajetRepository trajetRepository;
    private final PaiementService paiementService;
    private final NotificationService notificationService;
    

    public ReservationServiceImpl(ReservationRepository reservationRepository, TrajetRepository trajetRepository, PaiementService paiementService, NotificationService notificationService) {
        this.reservationRepository = reservationRepository;
        this.trajetRepository = trajetRepository;
        this.paiementService = paiementService;
        this.notificationService = notificationService;
    }
    @Override
    public Reservation creerReservation(Passager passager, Trajet trajet, int nombrePlaces) {
        if (trajet.getPlacesDisponibles() < nombrePlaces) {
            throw new IllegalArgumentException("Pas assez de places disponibles");
        }
        if (trajet.isComplet()) {
            throw new TrajetCompletException("Trajet complet");
        }
        Reservation reservation = new Reservation(trajet, passager, nombrePlaces);
        paiementService.payer(reservation);
        reservation.confirmerReservation();
        trajet.ajouterPassager(reservation);
        reservationRepository.save(reservation);
        notificationService.notfierUser(passager,"Reservation confirmée","Votre reservation a été confirmée.");
        return reservation;
    }
    
    @Override
    public void confirmerReservation(String reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("Reservation non trouvée"));
        reservation.confirmerReservation();
        reservationRepository.save(reservation);
        notificationService.notfierUser(reservation.getPassager(),"Reservation confirmée","Votre reservation a été confirmée.");
    }
    @Override
    public void annulerReservation(String reservationId,boolean isDriverCancel) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("Reservation non trouvée"));
            if(reservation.getStatus() == ReservationStatus.ANNULEE){
                throw new IllegalArgumentException("Reservation déjà annulée");
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
                    notificationService.notfierUser(reservation.getPassager(),"Annulation de reservation","Votre reservation a été annulée. " + "Un remboursement de "+prixTotal+"€ vous a été effectué.");
                }
                else{
                    double remboursement = prixTotal *0.5;
                    paiementService.rembourser(reservation,remboursement);
                    notificationService.notfierUser(reservation.getPassager(),"Annulation de reservation","Votre reservation a été annulée moins de 24h avant le départ. " + "Une pénalité de 50% vous sera remboursée en supplément.");
                    
                    
                }

            }
            reservation.annulerReservation();
            reservation.getTrajet().retirerPassager(reservation);
            reservationRepository.save(reservation);
            trajetRepository.save(reservation.getTrajet());
            
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