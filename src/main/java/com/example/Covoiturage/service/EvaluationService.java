package com.example.Covoiturage.service;
public interface EvaluationService {
    void evaluerChauffeur(String chauffeurId, String passagerId, int note);
    double consulterNotesChauffeur(String chauffeurId);
    void evaluerPassager(String passagerId, String chauffeurId, int note);
    
    
}
