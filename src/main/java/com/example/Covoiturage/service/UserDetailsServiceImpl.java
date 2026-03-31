package com.example.Covoiturage.service;

import com.example.Covoiturage.model.User;
import com.example.Covoiturage.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String email)
{
        User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                new UsernameNotFoundException(
                    "Aucun utilisateur trouvé avec l'email : " + email));

        String role = "ROLE_" + user.getRole().name(); //  "ROLE_PASSAGER"

        // Spring Security uses this object — your model User is not involved
       
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPasswordHash(),
            user.isActif(),         
            true,                    
            true,                    
            user.getStatus().name()
                .equals("ACTIF"),    // accountNonLocked
            List.of(new SimpleGrantedAuthority(role))
        );
    }
}