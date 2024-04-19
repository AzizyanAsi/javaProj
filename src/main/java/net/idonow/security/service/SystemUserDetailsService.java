package net.idonow.security.service;

import lombok.extern.slf4j.Slf4j;
import net.idonow.entity.system.SystemUser;
import net.idonow.service.entity.system.SystemUserService;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
public class SystemUserDetailsService implements UserDetailsService {

    private final SystemUserService systemUserService;

    public SystemUserDetailsService(SystemUserService systemUserService) {
        this.systemUserService = systemUserService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            SystemUser systemUser;
            if (username.contains("@")) {
                systemUser = systemUserService.getActiveUserByEmail(username);
            } else {
                systemUser = systemUserService.getActiveUserByPhoneNumber(username);
            }
            Set<SimpleGrantedAuthority> authorities = systemUserService.getAuthorities(systemUser.getRole());
            return buildUserForAuthentication(systemUser, authorities);
        } catch (AuthenticationException authenticationException) {
            log.warn("Authentication failed: {}", authenticationException.getMessage());
            throw authenticationException;
        }
    }

    private UserDetails buildUserForAuthentication(SystemUser systemUser, Set<SimpleGrantedAuthority> authorities) {
        return new org.springframework.security.core.userdetails.User(systemUser.getEmail(), systemUser.getPassword(), authorities);
    }

}
