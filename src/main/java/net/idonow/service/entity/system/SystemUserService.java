package net.idonow.service.entity.system;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.idonow.entity.Role;
import net.idonow.entity.system.SystemUser;
import net.idonow.service.entity.templates.EntityReadService;
import net.idonow.transform.system.systemuser.SystemUserRequest;
import net.idonow.transform.system.systemuser.converter.SystemUserUpdateRequest;
import net.idonow.transform.user.restore.PasswordResetRequest;
import net.idonow.transform.user.restore.TokenVerificationRequest;
import net.idonow.transform.user.restore.TokenVerificationResponse;
import net.idonow.transform.user.update.PasswordChangeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.Principal;
import java.util.Set;

public interface SystemUserService extends EntityReadService<SystemUser> {
    SystemUser registerSystemUser(SystemUserRequest userRequest);
    SystemUser getActiveUserByEmail(String email) throws AuthenticationException;
    SystemUser getSystemUserByEmail(String email) throws AuthenticationException;
    SystemUser getActiveUserByPhoneNumber(String phoneNumber) throws AuthenticationException;
    Set<SimpleGrantedAuthority> getAuthorities(Role role);
    void refreshTokens(HttpServletRequest request, HttpServletResponse response);

    Page<SystemUser> getSupportAgents(String firstName, String email, Boolean active, Pageable pageable);

    Page<SystemUser> getAllSystemUsers(Pageable pageable);

    SystemUser updateSystemUser(Long id, SystemUserUpdateRequest userRequest);
    boolean sendPasswordResetMessageToUser(String username);
    void resetPassword(PasswordResetRequest passwordResetRequest);

    void changePassword(PasswordChangeRequest passwordChangeRequest, Principal principal);

    void deleteSystemUserById(Long id);

    TokenVerificationResponse verifyResetToken(TokenVerificationRequest tokenVerificationRequest);
}
