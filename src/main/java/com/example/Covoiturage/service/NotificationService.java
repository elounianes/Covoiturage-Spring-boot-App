package com.example.Covoiturage.service;
import com.example.Covoiturage.model.User;

public interface  NotificationService {
    boolean notifierEmail(String email , String sujet, String contenu);
    boolean notifierSMS(String phone, String contenu);
    void notfierUser(User user,String sujet, String message);
}
