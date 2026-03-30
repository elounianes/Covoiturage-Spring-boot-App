package com.example.Covoiturage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Covoiturage.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findByDestinataireIdOrderByDateEnvoiDesc(String userId);
    List<Notification> findByDestinataireIdAndLu(String userId, boolean lu);
}