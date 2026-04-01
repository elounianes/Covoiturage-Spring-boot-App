package com.example.Covoiturage.service.impl;
import com.example.Covoiturage.repository.*;

import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import com.example.Covoiturage.model.*;
import com.example.Covoiturage.service.*;



@Service
public class EvaluationServiceImpl implements EvaluationService{
    private final ChauffeurRepository chauffeurRepository;
    private final PassagerRepository passagerRepository;
    private final NotificationService notificationService;
    public EvaluationServiceImpl(ChauffeurRepository chauffeurRepository,PassagerRepository passagerRepository,NotificationService notificationService) {
        this.chauffeurRepository = chauffeurRepository;
        this.passagerRepository = passagerRepository;
        this.notificationService = notificationService;
    }
    @Override
    public void evaluerChauffeur(String chauffeurId, String passagerId, int note) {
            Chauffeur chauffeur = chauffeurRepository.findById(chauffeurId)
            .orElseThrow(() -> new IllegalArgumentException("Chauffeur non trouvé"));
            Passager passager = passagerRepository.findById(passagerId)
            .orElseThrow(() -> new IllegalArgumentException("Passager non trouvé"));   
            chauffeur.ajouterNote(note);
            chauffeurRepository.save(chauffeur);
            notificationService.notfierUser(chauffeur,"Evaluation reçue","Un passager a évalué votre trajet avec la note "+note);
    }
    @Override
    public double consulterNotesChauffeur(String chauffeurId) {
        Chauffeur chauffeur = chauffeurRepository.findById(chauffeurId)
            .orElseThrow(() -> new IllegalArgumentException("Chauffeur non trouvé"));
        return chauffeur.consulterNotesChauffeur();
    }
    
    @Transactional
    @Override
    public void evaluerPassager(String passagerId,String chauffeurId,int note) {
        if (note < 1 || note > 5)
            throw new IllegalArgumentException("La note doit être entre 1 et 5");

        Passager passager = passagerRepository.findById(passagerId)
            .orElseThrow(() -> new IllegalArgumentException("Passager non trouvé"));
        Chauffeur chauffeur = chauffeurRepository.findById(chauffeurId)
            .orElseThrow(() -> new IllegalArgumentException("Chauffeur non trouvé"));
        notificationService.notfierUser(passager,"Évaluation reçue","Le chauffeur " + chauffeur.getEmail() + " vous a donné une note de " + note + "/5.");
    }
}

