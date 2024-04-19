package net.idonow.security.service;

import lombok.extern.slf4j.Slf4j;
import net.idonow.entity.User;
import net.idonow.service.entity.UserService;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public AppUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            User user;
            if (username.contains("@")) {
                user = userService.getActiveUserByEmail(username);
            } else {
                user = userService.getActiveUserByPhoneNumber(username);
            }
            Set<SimpleGrantedAuthority> authorities = userService.getAuthorities(user.getRole());
            return buildUserForAuthentication(user, authorities);
        } catch (AuthenticationException authenticationException) {
            log.warn("Authentication failed: {}", authenticationException.getMessage());
            throw authenticationException;
        }
    }

    private UserDetails buildUserForAuthentication(User user, Set<SimpleGrantedAuthority> authorities) {
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
    }

}
