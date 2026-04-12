package com.portfolio.stockpricefeed.security;

import com.portfolio.stockpricefeed.entities.User;
import com.portfolio.stockpricefeed.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

/**
 * US2 - Loads user from PostgreSQL for Spring Security authentication.
 * Supports login by email OR username.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailServiceImple implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String emailOrUsername) throws UsernameNotFoundException {
        // Try email first, then username
        User user = userRepository.findByEmail(emailOrUsername)
                .or(() -> userRepository.findByUsername(emailOrUsername))
                .orElseThrow();

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }
}