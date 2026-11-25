package at.hakspittal.minishop.service;

import at.hakspittal.minishop.entity.User;
import at.hakspittal.minishop.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {   //implements UserDetailsService holt Nutzerdaten aus der DB
    
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    //UserDetails wird von der Spring-Security-Dependency bereitgestellt
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Benutzer nicht gefunden: " + username));
        
        //Rolle umwandeln, damit Spring-Security es versteht        
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

        //UserDetails-Objekt wird gebaut
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())   
                .password(user.getPassword())
                .authorities(List.of(authority))
                .disabled(!user.isEnabled())
                .build();
    }
}
