package com.example.Covoiturage;

// DataInitializer.java
// Runs on every startup and seeds realistic sample data.
// Lets you test every endpoint immediately without manual setup.

import com.example.Covoiturage.model.*;
import com.example.Covoiturage.model.enums.UserRole;
import com.example.Covoiturage.repository.*;
import com.example.Covoiturage.service.AuthService;
import com.example.Covoiturage.service.ReservationService;
import com.example.Covoiturage.service.TrajetService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional

public class DataInitializer implements CommandLineRunner {

    private final AuthService authService;
    private final TrajetService trajetService;
    private final ReservationService reservationService;
    private final ChauffeurRepository chauffeurRepo;
    private final PassagerRepository passagerRepo;
    private final VehiculeRepository vehiculeRepo;
    private final MoyenPaiementRepository moyenPaiementRepo;

    public DataInitializer(AuthService authService,
                            TrajetService trajetService,
                            ReservationService reservationService,
                            ChauffeurRepository chauffeurRepo,
                            PassagerRepository passagerRepo,
                            VehiculeRepository vehiculeRepo,
                            MoyenPaiementRepository moyenPaiementRepo) {
        this.authService = authService;
        this.trajetService = trajetService;
        this.reservationService = reservationService;
        this.chauffeurRepo = chauffeurRepo;
        this.passagerRepo = passagerRepo;
        this.vehiculeRepo = vehiculeRepo;
        this.moyenPaiementRepo = moyenPaiementRepo;
    }

    @Override
    @Transactional 
    public void run(String... args) {
        System.out.println("\n===== SEEDING SAMPLE DATA =====");

        // ── Create users ──────────────────────────────────
        authService.creerCompte(
            "alice@test.com", "0611111111", "password123", UserRole.PASSAGER);
        authService.creerCompte(
            "bob@test.com", "0622222222", "password123", UserRole.CHAUFFEUR);
        authService.creerCompte(
            "admin@test.com", "0633333333", "password123", UserRole.ADMIN);

        // ── Add vehicle to driver ─────────────────────────
        Chauffeur bob = chauffeurRepo.findByEmail("bob@test.com").get();
        Vehicule voiture = new Vehicule("Toyota", "Corolla", 4, "AB-123-CD");
        bob.ajouterVehicule(voiture);
        vehiculeRepo.save(voiture);
        chauffeurRepo.save(bob);

        // ── Create trips ──────────────────────────────────
        // Trip 1: 48h in the future — safe for booking and cancellation tests
        Trajet t1 = trajetService.proposerTrajet(
            bob, voiture,
            "Tunis", "Sfax",
            LocalDateTime.now().plusHours(48),
            3, 15.0
        );

        // Trip 2: 10h in the future — inside the 24h penalty window
        Trajet t2 = trajetService.proposerTrajet(
            bob, voiture,
            "Tunis", "Bizerte",
            LocalDateTime.now().plusHours(10),
            2, 8.0
        );

        // ── Add payment method to passenger ──────────────
        Passager alice = passagerRepo.findByEmail("alice@test.com").get();
        MoyenPaiement mp = new MoyenPaiement();
        mp.setType("CARTE");
        mp.setNumeroMasque("**** **** **** 4242");
        mp.setDateExpiration("12/27");
        mp.setTitulaire("Alice Test");
        alice.ajouterMoyenPaiement(mp);
        moyenPaiementRepo.save(mp);
        passagerRepo.save(alice);

        // ── Create one reservation for testing ────────────
        reservationService.creerReservation(alice, t1, 1);

        System.out.println("===== SAMPLE DATA READY =====\n");
        System.out.println("Logins:");
        System.out.println("  Passager  → alice@test.com / password123");
        System.out.println("  Chauffeur → bob@test.com   / password123");
        System.out.println("  Admin     → admin@test.com / password123");
        System.out.println("H2 Console → http://localhost:8080/h2-console");
    }
}
