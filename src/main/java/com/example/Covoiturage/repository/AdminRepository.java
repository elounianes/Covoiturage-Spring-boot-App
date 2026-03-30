package com.example.Covoiturage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Covoiturage.model.Admin;

public interface AdminRepository extends JpaRepository<Admin, String> {}