package com.example.Covoiturage.service.impl;
import org.springframework.stereotype.Service;
import com.example.Covoiturage.repository.NotificationRepository;
import com.example.Covoiturage.service.NotificationService;
import com.example.Covoiturage.model.Notification;
import com.example.Covoiturage.model.User;
@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepos;
    public NotificationServiceImpl(NotificationRepository notificationRepos) {
        this.notificationRepos = notificationRepos;
    }
    @Override
    public boolean notifierEmail(String email, String sujet, String contenu) {
            System.out.println("[EMAIL] To: " + email);
            System.out.println("  Sujet : " + sujet);
            System.out.println("  Contenu : " + contenu);
            return true;    
    }
    @Override
    public boolean notifierSMS(String phone, String contenu) {
        System.out.println("[SMS] To: " + phone);
        System.out.println("  Contenu : " + contenu);
        return true;
    }
    @Override
    public void notfierUser(User user, String sujet, String message) {
        Notification notification = new Notification(user, "EMAIL",sujet+"|"+ message);
        notificationRepos.save(notification);
        notifierEmail(user.getEmail(), sujet, message);
        notifierSMS(user.getPhone(), message);
    }
}
