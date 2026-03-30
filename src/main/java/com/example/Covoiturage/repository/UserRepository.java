package com.example.Covoiturage.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Covoiturage.model.User;
import com.example.Covoiturage.model.enums.UserStatus;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByStatus(UserStatus status);
}

// repository to3tabar ka layer bin les services w la base de données t3ml translate l code SQL eli deja set fel les entite (mapping @entity) exemple findby name = selet * from user where name = ?