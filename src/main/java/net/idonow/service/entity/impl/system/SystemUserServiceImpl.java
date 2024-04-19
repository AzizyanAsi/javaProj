package net.idonow.service.entity.impl.system;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import net.idonow.common.config.AppConfig;
import net.idonow.common.data.CountryCode;
import net.idonow.common.util.LocaleUtils;
import net.idonow.common.util.PhoneNumberUtils;
import net.idonow.controller.exception.common.ActionNotAllowedException;
import net.idonow.controller.exception.common.EntityNotFoundException;
import net.idonow.controller.exception.common.InvalidRequestDataException;
import net.idonow.controller.exception.common.InvalidVerificationTokenException;
import net.idonow.controller.exception.security.*;
import net.idonow.controller.mapping.ResponseMappers;
import net.idonow.entity.Role;
import net.idonow.entity.User;
import net.idonow.entity.VerificationToken;
import net.idonow.entity.enums.TokenType;
import net.idonow.entity.system.SystemUser;
import net.idonow.entity.system.SystemVerificationToken;
import net.idonow.repository.system.SystemUserRepository;
import net.idonow.security.enums.RoleType;
import net.idonow.security.service.common.JwtService;
import net.idonow.security.service.common.LoggedUserStorageService;
import net.idonow.service.common.MailSenderService;
import net.idonow.service.common.PhoneNumberVerificationService;
import net.idonow.service.entity.RoleService;
import net.idonow.service.entity.VerificationTokenService;
import net.idonow.service.entity.system.SystemUserService;
import net.idonow.service.entity.system.SystemVerificationService;
import net.idonow.transform.system.systemuser.SystemUserRequest;
import net.idonow.transform.system.systemuser.SystemUserResponse;
import net.idonow.transform.system.systemuser.converter.ISystemUserConverter;
import net.idonow.transform.system.systemuser.converter.SystemUserUpdateRequest;
import net.idonow.transform.user.restore.PasswordResetRequest;
import net.idonow.transform.user.restore.TokenVerificationRequest;
import net.idonow.transform.user.restore.TokenVerificationResponse;
import net.idonow.transform.user.update.PasswordChangeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static net.idonow.common.cache.EntityCacheNames.*;
import static net.idonow.common.cache.TemplateCacheNames.APP_USER_PASSWORD_RESET_TOKEN_PREFIX;
import static net.idonow.common.cache.TemplateCacheNames.SYSTEM_USER_PASSWORD_RESET_TOKEN_PREFIX;

@Slf4j
@Service
public class SystemUserServiceImpl implements SystemUserService {
    private final SystemUserRepository systemUserRepository;
    private final RoleService roleService;
    private PasswordEncoder passwordEncoder;
    private final ISystemUserConverter systemUserConverter;
    private PhoneNumberVerificationService phoneNumberVerificationService;
    private LoggedUserStorageService loggedSystemUserStorageService;

    private SystemVerificationService verificationTokenService;
    private RedisTemplate<String, String> redisTemplate;
    private TransactionTemplate transactionTemplate;
    private MailSenderService mailSenderService;
    private ResponseMappers responseMappers;
    private AppConfig appConfig;
    private JwtService jwtService;

    private LocaleUtils localeUtils;

    public SystemUserServiceImpl(SystemUserRepository systemUserRepository, RoleService roleService,
            ISystemUserConverter systemUserConverter) {
        this.systemUserRepository = systemUserRepository;
        this.roleService = roleService;
        this.systemUserConverter = systemUserConverter;
    }

    @Autowired
    public void setAppConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setResponseMappers(ResponseMappers responseMappers) {
        this.responseMappers = responseMappers;
    }

    @Autowired
    public void setPhoneNumberVerificationService(PhoneNumberVerificationService phoneNumberVerificationService) {
        this.phoneNumberVerificationService = phoneNumberVerificationService;
    }
    @Autowired
    public void setLoggedSystemUserStorageService(@Qualifier("loggedSystemUserStorageServiceImpl") LoggedUserStorageService loggedSystemUserStorageService) {
        this.loggedSystemUserStorageService = loggedSystemUserStorageService;
    }
    @Autowired
    public void setVerificationTokenService(SystemVerificationService verificationTokenService) {
        this.verificationTokenService = verificationTokenService;
    }
    @Autowired
    public void setRedisTemplate(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    @Autowired
    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
        this.transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.transactionTemplate.setTimeout(15);
    }

    @Autowired
    public void setMailSenderService(MailSenderService mailSenderService) {
        this.mailSenderService = mailSenderService;
    }

    @Autowired
    public void setJwtService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Autowired
    public void setLocaleUtils(LocaleUtils localeUtils) {
        this.localeUtils = localeUtils;
    }

    @Override
    public SystemUser getActiveUserByEmail(String email) throws AuthenticationException {
        Optional<SystemUser> optSystemUser = systemUserRepository.findByEmailIgnoreCase(email);
        if (optSystemUser.isEmpty()) {
            throw new EmailNotFoundException(String.format("SystemUser with email '%s' not found", email));
        }
        SystemUser systemUser = optSystemUser.get();
        this.verifySystemUser(systemUser);
        return systemUser;
    }

    @Override
    public SystemUser getActiveUserByPhoneNumber(String phoneNumber) throws AuthenticationException {
        CountryCode countryCode = CountryCode.valueOf(appConfig.getAppCountryCode());
        boolean isValid = PhoneNumberUtils.validate(phoneNumber, countryCode);
        if (isValid) {
            phoneNumber = PhoneNumberUtils.normalize(phoneNumber, countryCode);
        } else {
            throw new PhoneNumberNotFoundException("Invalid phone number");
        }
        Optional<SystemUser> loadedUser = systemUserRepository.findByPhoneNumber(phoneNumber);
        if (loadedUser.isEmpty()) {
            throw new PhoneNumberNotFoundException(String.format("SystemUser with phone number '%s' not found", phoneNumber));
        }
        SystemUser user = loadedUser.get();
        this.verifySystemUser(user);
        return user;
    }

    @Override
    public Set<SimpleGrantedAuthority> getAuthorities(Role role) {
        // ROLE_ prefix is used by hasRole method of ExpressionUrlAuthorizationConfigurer framework class
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleType().toString()));
        return authorities;
    }

    @Override
    public void refreshTokens(HttpServletRequest request, HttpServletResponse response) {
        String email = jwtService.decodeRefreshJwt(request);
        SystemUser systemUser = this.getActiveUserByEmail(email);
        jwtService.provideTokens(email, this.getAuthorities(systemUser.getRole()), request, response);
    }


    @Override
    public Page<SystemUser> getSupportAgents(String firstName, String email, Boolean active, Pageable pageable) {
        if ((firstName != null && !firstName.isEmpty()) || (email != null && !email.isEmpty()) || active != null) {

            return systemUserRepository.findAllByRoleRoleTypeAndNameAndEmailAndActive(RoleType.SUPPORT_AGENT, firstName, email,active,pageable);
        } else {
            return systemUserRepository.findAllByRoleRoleType(RoleType.SUPPORT_AGENT, pageable);
        }
    }
    @Override
    public Page<SystemUser> getAllSystemUsers(Pageable pageable) {
        return systemUserRepository.findAll(pageable);
    }

    @Override
    public SystemUser updateSystemUser(Long id, SystemUserUpdateRequest userRequest) {
        var converted = systemUserConverter.convertToEntityUpdateReq(userRequest);
        Optional<SystemUser> loadedUser = systemUserRepository.findById(id);
        SystemUser systemUser = loadedUser.orElseThrow(() -> new NoSuchElementException("User not found"));

        systemUser.copyFieldsFrom(converted);
        return systemUserRepository.save(systemUser);
    }


    /* -- PRIVATE METHODS -- */

    private void verifySystemUser(SystemUser systemUser) throws AuthenticationException {
        if (!systemUser.getActive().equals(true)) {
            String message = String.format("SystemUser account of '%s' is not active", systemUser.getEmail());
            throw new AccountNotActiveException(message);
        }
    }

    @Override
    public SystemUser registerSystemUser(SystemUserRequest userRequest) {
        // Normalize already validated phone number
        String normalizedPhoneNumber = PhoneNumberUtils.normalize(userRequest.getPhoneNumber(),
                CountryCode.valueOf(appConfig.getAppCountryCode()));
        Role userRole = roleService.getByRoleType(userRequest.getUserRole());
        SystemUser user = new SystemUser();
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setPhoneNumber(normalizedPhoneNumber);
        user.setActive(true);
        user.setRole(userRole);
       var savedUser= systemUserRepository.save(user);
        log.info("User successfully registered: {}", user);
        return savedUser;
    }

    @Override
    @Cacheable(value = ALL_SYSTEM_USERS)
    public List<SystemUser> getAllEntities() throws AuthenticationException {
        return systemUserRepository.findAll();
    }

    @Override
    @Cacheable(value = SYSTEM_USER, key = "#id", unless = "#result == null")
    public SystemUser getEntity(Long id) throws AuthenticationException {
        Optional<SystemUser> loadedUser = systemUserRepository.findById(id);
        if (loadedUser.isEmpty()) {
            throw new EntityNotFoundException(String.format("User with id {%d} not found", id));
        }
        return loadedUser.get();
    }

    @Override
    public SystemUser getSystemUserByEmail(String email) throws AuthenticationException {
        Optional<SystemUser> loadedUser = systemUserRepository.findByEmailIgnoreCase(email);
        if (loadedUser.isEmpty()) {
            throw new EntityNotFoundException(String.format("User with id {%d} not found", email));
        }
        return loadedUser.get();
    }

    @Override
    public boolean sendPasswordResetMessageToUser(String username) {
        try {
            if (sentEmailAsUsername(username)) {
                SystemUser user = this.getActiveUserByEmail(username);
                return sendResetMessageToEmail(user);
            } else {
                return sendResetMessageToPhoneNumber(this.getActiveUserByPhoneNumber(username));
            }
        } catch (AuthenticationException authenticationException) {
            log.warn("Password reset rejected for account '{}': {}", username, authenticationException.getMessage());
            return false;
        }
    }

    private boolean sentEmailAsUsername(String username) {
        return username.contains("@");
    }

    private boolean sendResetMessageToEmail(SystemUser user) {
        if (verificationTokenService.sendingNewTokenNotAllowedToSystemUser(user, TokenType.PASSWORD_RESET)) {
            log.warn("Password reset message is already sent to user email: '{}'", user.getEmail());
            return true;
        }
        // Create VerificationToken
        SystemVerificationToken verificationToken = verificationTokenService.createTokenForSystemUser(user, TokenType.PASSWORD_RESET);
        try {
            // Try to send an email
            mailSenderService.sendPasswordResetMessageToSystemUser(user, verificationToken.getToken());
            // Delete password reset token from cache if it has been provided
            deletePasswordResetTokenFromCache(user.getId());
            log.info("Password reset message is sent to user email: '{}'", user.getEmail());
            return true;
        } catch (MessagingException e) {
            verificationTokenService.deleteToken(verificationToken.getId());
            log.error("Failed to send password reset message to user  {}. Cause: {}", user.getEmail(), e.getMessage());
            return false;
        }
    }
    private boolean sendResetMessageToPhoneNumber(SystemUser user) {
        Boolean sent = transactionTemplate.execute(status -> {
            // If there is password reset token being sent user via email invalidate it
            verificationTokenService.deleteBySystemUserAndTokenType(user, TokenType.PASSWORD_RESET);
            // Try to send sms to user
            boolean success = phoneNumberVerificationService.startVerification(user.getPhoneNumber());
            if (!success) {
                status.setRollbackOnly();
                return false;
            } else {
                // Delete password reset token from cache if it has been provided
                deletePasswordResetTokenFromCache(user.getId());
                return true;
            }
        });
        if (sent != null && sent) {
            log.info("Password reset message is sent to user phone number: '{}'", user.getPhoneNumber());
            return true;
        } else {
            log.error("Failed to send password reset message to user phone number: '{}'", user.getPhoneNumber());
            return false;
        }
    }

    private void deletePasswordResetTokenFromCache(Long userId) {
        String key = getPasswordResetTokenCacheKey(userId);
        String value = redisTemplate.opsForValue().getAndDelete(key);
        if (value != null) {
            log.info("Password change token deleted: userId = {}", userId);
        }
    }

    private String getPasswordResetTokenCacheKey(Long userId) {
        return SYSTEM_USER_PASSWORD_RESET_TOKEN_PREFIX + userId;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = SYSTEM_USER, key = "#passwordResetRequest.userId")})
    public void resetPassword(PasswordResetRequest passwordResetRequest) {
        String token = this.getPasswordResetTokenFromCache(passwordResetRequest.getUserId());
        if (token == null || passwordResetRequest.getToken().compareTo(token) != 0) {
            throw new ActionNotAllowedException(localeUtils.getLocalizedMessage("error.data-verification"));
        }
        this.deletePasswordResetTokenFromCache(passwordResetRequest.getUserId());
        SystemUser user = this.getEntity(passwordResetRequest.getUserId());
        user.setPassword(passwordEncoder.encode(passwordResetRequest.getPassword()));
        systemUserRepository.save(user);
        // Interrupt active session
        loggedSystemUserStorageService.interruptUserSession(user.getEmail());
        log.info("Password has been successfully reset: " + user);
    }

    @Nullable
    private String getPasswordResetTokenFromCache(Long userId) {
        String key = getPasswordResetTokenCacheKey(userId);
        return this.redisTemplate.opsForValue().get(key);
    }


    @Override
    public TokenVerificationResponse verifyResetToken(TokenVerificationRequest tokenVerificationRequest) {
        try {
            if (sentEmailAsUsername(tokenVerificationRequest.getUsername())) {
                SystemUser user = this.getActiveUserByEmail(tokenVerificationRequest.getUsername());
                return verifyResetTokenByEmail(user, tokenVerificationRequest.getToken());
            } else {
                SystemUser user = this.getActiveUserByPhoneNumber(tokenVerificationRequest.getUsername());
                return verifyResetTokenByPhoneNumber(user, tokenVerificationRequest.getToken());
            }
        } catch (AuthenticationException exception) {
            throw new ActionNotAllowedException("Error while verifying token");
        }
    }

    /**
     * If the user first enters email to reset password and then without verifying token enters phone number to send reset token,
     * the token being sent to email automatically becomes invalid after phone number token is being successfully sent
     */
    private TokenVerificationResponse verifyResetTokenByEmail(SystemUser user, String token) {
        try {
            boolean verified = verificationTokenService.verifyTokenSystemUser(user, TokenType.PASSWORD_RESET, token);
            if (verified) {
                log.info("Password reset token being sent via email has been successfully verified: {}", user);
            } else {
                throw new InvalidRequestDataException("Invalid token", Map.of("token", localeUtils.getLocalizedMessage("error.invalid-token")));
            }
        } catch (InvalidVerificationTokenException ex) {
            throw new ActionNotAllowedException(ex.getMessage());
        }
        return new TokenVerificationResponse(user.getId(), createPasswordResetToken(user.getId()));
    }

    /**
     * If there is password reset token being sent user via email it means that after entering and receiving
     * otp to phone number, user entered email to send reset token => reject to check verification
     */
    private TokenVerificationResponse verifyResetTokenByPhoneNumber(SystemUser user, String token) {
        if (verificationTokenService.tokenExistsForSystemUser(user, TokenType.PASSWORD_RESET)) {
            throw new ActionNotAllowedException(localeUtils.getLocalizedMessage("error.data-verification"));
        }
        boolean verified = phoneNumberVerificationService.confirmVerification(user.getPhoneNumber(), token);
        if (verified) {
            log.info("Password reset token being sent via phone number has been successfully verified: {}", user);
            return new TokenVerificationResponse(user.getId(), createPasswordResetToken(user.getId()));
        } else {
            throw new InvalidRequestDataException("Invalid token", Map.of("token", localeUtils.getLocalizedMessage("error.invalid-token")));
        }
    }

    private String createPasswordResetToken(Long userId) {
        String token = UUID.randomUUID().toString();
        String key = getPasswordResetTokenCacheKey(userId);
        redisTemplate.opsForValue().set(key, token, 10L, TimeUnit.MINUTES);
        log.info("Password reset token provided: userId = {}", userId);
        return token;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = SYSTEM_USER, key = "#principal.name")
    })
    public void changePassword(PasswordChangeRequest passwordChangeRequest, Principal principal) {
        // Get user from db
        SystemUser systemUser = getSystemUserByEmail(principal.getName());
        // Check if current password sent by user is correct
        if (!passwordEncoder.matches(passwordChangeRequest.getOldPassword(), systemUser.getPassword())) {
            String message = "Sent invalid password";
            log.warn(message + ": " + systemUser.getEmail());
            throw new InvalidRequestDataException(message, Map.of("oldPassword", localeUtils.getLocalizedMessage("error.password.invalid-old-password")));
        }
        // Don't allow to set old password
        if (passwordEncoder.matches(passwordChangeRequest.getNewPassword(), systemUser.getPassword())) {
            String message = "Repetition of old password";
            log.warn(message + ": " + systemUser);
            throw new InvalidRequestDataException(message, Map.of("newPassword", localeUtils.getLocalizedMessage("error.password.old-pwd-forbidden")));
        }
        systemUser.setPassword(passwordEncoder.encode(passwordChangeRequest.getNewPassword()));
        systemUserRepository.save(systemUser);
        log.info("User password changed: " + systemUser);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = SYSTEM_USER, key = "#id"),
            @CacheEvict(value = ALL_SYSTEM_USERS, allEntries = true)
    })
    public void deleteSystemUserById(Long id) {
        this.getEntity(id);
        systemUserRepository.deleteById(id);
        log.info("SystemUser successfully deleted: {id = {}}", id);
    }

}
