package com.example.Covoiturage.service.impl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.Covoiturage.exception.UtilisateurInactifException;
import com.example.Covoiturage.model.Admin;
import com.example.Covoiturage.model.Chauffeur;
import com.example.Covoiturage.model.Passager;
import com.example.Covoiturage.model.User;
import com.example.Covoiturage.model.enums.UserRole;
import com.example.Covoiturage.model.enums.UserStatus;
import com.example.Covoiturage.repository.UserRepository;
import com.example.Covoiturage.service.AuthService;
import com.example.Covoiturage.service.NotificationService;
@Service
public class AuthServiceImpl implements AuthService{
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final PasswordEncoder passwordEncoder;
    public AuthServiceImpl(UserRepository userRepository,NotificationService notificationService,PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.passwordEncoder = passwordEncoder;
    }
    @Override
    public User creerCompte(String email,String phone, String mdp,UserRole role) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email Deja existant"+email);
        }
        String hash = passwordEncoder.encode(mdp);


        User user = switch(role){
            case PASSAGER -> new Passager(email, phone, hash);
            case CHAUFFEUR -> new Chauffeur(email, phone, hash);
            case ADMIN -> new Admin(email, phone, hash);
        };
        userRepository.save(user);
        notificationService.notfierUser(user,"Bienvenue sur CovoitApp","Votre compte a été créé avec succès.");
        return user;
    }
    @Override
    public User authentifier(String email, String mdp) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Email ou mot de passe incorrect"));
        if (user.getStatus() != UserStatus.ACTIF){
            throw new UtilisateurInactifException(email);
        }
        if(!passwordEncoder.matches(mdp, user.getPasswordHash())){
            user.incrementFailedAttempts();
            userRepository.save(user);
        
        if(user.getStatus() == UserStatus.BLOQUE){
            notificationService.notfierUser(user,"Compte bloqué ","Votre compte a été bloqué suite à plusieurs tentatives de connexion échouées.");
        }
        throw new IllegalArgumentException("Email ou mot de passe incorrect");
        }
        user.resetFailedAttempts();
        userRepository.save(user);
        return user;
}
@Override
public void deconnecter(String userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
    user.deconnecter();
}
@PreAuthorize("hasRole('ADMIN')")
@Override
public void suspendreCompte(String userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
    
    user.setStatus(UserStatus.SUSPENDU);
    userRepository.save(user);
    notificationService.notfierUser(user,"Compte suspendu","Le compte a été suspendu.");
}
@PreAuthorize("hasRole('ADMIN')")
@Override
public void bloquerCompte(String userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
    user.setStatus(UserStatus.BLOQUE);
    userRepository.save(user);
    notificationService.notfierUser(user,"Compte bloqué","Le compte a été bloqué.");
}



}

