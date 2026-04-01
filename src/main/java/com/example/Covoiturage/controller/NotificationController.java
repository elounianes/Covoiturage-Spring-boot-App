package com.example.Covoiturage.controller;

import com.example.Covoiturage.dto.ApiResponse;
import com.example.Covoiturage.exception.ResourceNotFoundException;
import com.example.Covoiturage.model.Notification;
import com.example.Covoiturage.model.User;
import com.example.Covoiturage.repository.NotificationRepository;
import com.example.Covoiturage.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepo;
    private final UserRepository userRepo;

    public NotificationController(NotificationRepository notificationRepo,
                                   UserRepository userRepo) {
        this.notificationRepo = notificationRepo;
        this.userRepo = userRepo;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>>
            getMesNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepo.findByEmail(userDetails.getUsername())
            .orElseThrow(() ->
                new ResourceNotFoundException("User",
                    userDetails.getUsername()));

        List<Notification> notifications = notificationRepo
            .findByDestinataireIdOrderByDateEnvoiDesc(user.getId());

        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

   
    @PutMapping("/{id}/lire")
    public ResponseEntity<ApiResponse<Void>> marquerCommeLue(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Notification notification = notificationRepo.findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("Notification", id));

        if (!notification.getDestinataire().getEmail()
                .equals(userDetails.getUsername())) {
            return ResponseEntity
                .status(403)
                .body(ApiResponse.error(
                    "Cette notification ne vous appartient pas"));
        }

        notification.setLu(true);
        notificationRepo.save(notification);

        return ResponseEntity.ok(
            ApiResponse.success("Notification marquée comme lue"));
    }

    @GetMapping("/non-lues")
    public ResponseEntity<ApiResponse<List<Notification>>>
            getNotificationsNonLues(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepo.findByEmail(userDetails.getUsername())
            .orElseThrow(() ->
                new ResourceNotFoundException("User",
                    userDetails.getUsername()));

        List<Notification> nonLues = notificationRepo
            .findByDestinataireIdAndLu(user.getId(), false);

        return ResponseEntity.ok(ApiResponse.success(nonLues));
    }
}