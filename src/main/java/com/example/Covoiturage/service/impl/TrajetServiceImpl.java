package com.example.Covoiturage.service.impl;
import org.springframework.stereotype.Service;
import com.example.Covoiturage.repository.*;
import com.example.Covoiturage.model.*;
import com.example.Covoiturage.model.enums.*;
import com.example.Covoiturage.service.NotificationService;
import com.example.Covoiturage.service.TrajetService;
import java.time.LocalDateTime;
import java.util.List;
@Service
public class TrajetServiceImpl implements TrajetService{

    private final TrajetRepository trajetRepo;
    private final ChauffeurRepository chauffeurRepo;
    private final ReservationRepository reservationRepo;
    private final NotificationService notificationService;

    public TrajetServiceImpl(TrajetRepository trajetRepo,
                              ChauffeurRepository chauffeurRepo,
                              ReservationRepository reservationRepo,
                              NotificationService notificationService) {
        this.trajetRepo = trajetRepo;
        this.chauffeurRepo = chauffeurRepo;
        this.reservationRepo = reservationRepo;
        this.notificationService = notificationService;
    }
    @Override
    public Trajet proposerTrajet(Chauffeur chauffeur,Vehicule vehicule, String origine, String destination, LocalDateTime heureDepart, int placesTotales, double prixParPlace){
        if(heureDepart.isBefore(LocalDateTime.now())){
            throw new IllegalArgumentException("Heure de départ doit être dans le futur");
        }
        if(placesTotales > vehicule.getCapaciteMax()){
            throw new IllegalArgumentException("Nombre de places supérieur à la capacité du véhicule");
        }
        Trajet trajet = new Trajet(chauffeur, vehicule, origine, destination, heureDepart, placesTotales, prixParPlace);
        trajetRepo.save(trajet);
        notificationService.notfierUser(chauffeur,"Trajet proposé","Votre trajet a été proposé avec succès.");
        
        return trajet;
    }
    @Override
    public void cloreTrajet(String trajetId) {
        Trajet trajet = trajetRepo.findById(trajetId)
            .orElseThrow(() -> new IllegalArgumentException("Trajet non trouvé"));
        List<Reservation> reservations = reservationRepo.findByTrajetIdAndStatus(trajetId,ReservationStatus.CONFIRMEE);
         boolean moins24h = LocalDateTime.now()
            .isAfter(trajet.getHeureDepart().minusHours(24));

        // Notify every affected passenger
        for (Reservation r : reservations) {
            trajet.retirerPassager(r);
            r.setStatus(ReservationStatus.ANNULEE);
            reservationRepo.save(r);

            if (moins24h) {
                // Simulated: just notify, real payout handled by PaiementService
                notificationService.notfierUser(r.getPassager(),"Trajet annulé par le chauffeur","Votre trajet a été annulé moins de 24h avant le départ. " + "Une pénalité de 20% vous sera remboursée en supplément.");
            } else {
                notificationService.notfierUser(r.getPassager(),"Trajet annulé","Le trajet " + trajet.getOrigine() + " → " +trajet.getDestination() + " a été annulé par le chauffeur.");
            }
        }

        trajet.cloreTrajet();
        trajetRepo.save(trajet);
    }
    @Override
    public List<Trajet> getTrajets(String origine, String destination) {
        return trajetRepo.findByOrigineAndDestinationAndStatus(origine, destination, TrajetStatus.PREVU);
    }
    @Override
    public List<Trajet>getTrajetsDiponibles(){
        return trajetRepo.findByStatus(TrajetStatus.PREVU);
    }
    @Override
    public Trajet getTrajet(String trajetId) {
        return trajetRepo.findById(trajetId)
            .orElseThrow(() -> new IllegalArgumentException("Trajet non trouvé"));
    }

       }






