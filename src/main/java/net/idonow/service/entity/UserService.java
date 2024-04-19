package net.idonow.service.entity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.idonow.entity.Role;
import net.idonow.entity.User;
import net.idonow.security.enums.RoleType;
import net.idonow.security.jwt.common.AuthenticationRequest;
import net.idonow.service.entity.templates.EntityReadService;
import net.idonow.transform.user.UserResponse;
import net.idonow.transform.user.registration.UserRequest;
import net.idonow.transform.user.restore.PasswordResetRequest;
import net.idonow.transform.user.restore.TokenVerificationRequest;
import net.idonow.transform.user.restore.TokenVerificationResponse;
import net.idonow.transform.user.update.EmailUpdateRequest;
import net.idonow.transform.user.update.PasswordChangeRequest;
import net.idonow.transform.user.update.UserInfoUpdateRequest;
import net.idonow.transform.user.verification.PhoneNumberVerificationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.Set;

public interface UserService extends EntityReadService<User> {

    User getUserByEmail(String email) throws UsernameNotFoundException;
    User getActiveUserByEmailCached(String email) throws AuthenticationException;
    User getActiveUserByEmail(String email) throws AuthenticationException;
    User getActiveUserByPhoneNumber(String phoneNumber) throws AuthenticationException;
    Set<SimpleGrantedAuthority> getAuthorities(Role role);
    String getHiddenPhoneNumber(String phoneNumber);

    void refreshTokens(HttpServletRequest request, HttpServletResponse response);
    UserResponse registerAndSendVerificationMessage(UserRequest userRequest);
    User startPhoneNumberVerification(AuthenticationRequest authenticationRequest);
    User confirmPhoneNumberVerification(PhoneNumberVerificationRequest phoneNumberVerificationRequest, HttpServletRequest request, HttpServletResponse response);
    boolean startEmailVerification(Principal principal);
    boolean sendPasswordResetMessageToUser(String username);
    TokenVerificationResponse verifyResetToken(TokenVerificationRequest tokenVerificationRequest);
    void resetPassword(PasswordResetRequest passwordResetRequest);
    void confirmEmailVerification(Principal principal, String token);

    User updateEmail(Principal principal, EmailUpdateRequest emailUpdateRequest, HttpServletRequest request, HttpServletResponse response);
    User updateInfo(UserInfoUpdateRequest userInfoUpdateRequest);
    void updateRole(User user, RoleType roleType, HttpServletRequest request, HttpServletResponse response);
    void changePassword(PasswordChangeRequest passwordChangeRequest, Principal principal);
    String uploadProfilePicture(MultipartFile profilePicture, Principal principal) throws IOException;
    String uploadCoverPicture(MultipartFile coverPicture, Principal principal) throws IOException;
    void deleteProfilePicture(Principal principal);
    void deleteCoverPicture(Principal principal);
    Page<User> getListOfUsers(String firstName, String email, Boolean active, Pageable pageable);

    void deleteUserById(Long id);

    User updateUserById(Long id, UserInfoUpdateRequest userInfoUpdateRequest);
}
