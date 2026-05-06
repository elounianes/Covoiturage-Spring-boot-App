// this file and the UserDetailsServiceImpl class connects your database users to Spring Security.
//It tells Spring Security how to load a user (email, password, role, status) from the database for authentication.
// this file is AI generated
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
public class UserDetailsServiceImpl implements UserDetailsService { //implements UserDetailsService (required by Spring Security) This interface has one key method: loadUserByUsername

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String email)
{
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Aucun utilisateur trouvé avec l'email : " + email));

        String role = "ROLE_" + user.getRole().name(); //  getting the role

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
    }//This class adapts your database User into a Spring Security User so authentication and authorization can work properly.
    //It loads a user from the database and transforms it into a Spring Security-compatible object used for authentication.
}