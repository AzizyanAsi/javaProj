package net.idonow.service.entity.impl;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.idonow.common.config.AppConfig;
import net.idonow.common.config.LimitsConfig;
import net.idonow.common.config.StorageConfig;
import net.idonow.common.data.CountryCode;
import net.idonow.common.util.LocaleUtils;
import net.idonow.common.util.PhoneNumberUtils;
import net.idonow.controller.exception.common.ActionNotAllowedException;
import net.idonow.controller.exception.common.EntityNotFoundException;
import net.idonow.controller.exception.common.InvalidRequestDataException;
import net.idonow.controller.exception.common.InvalidVerificationTokenException;
import net.idonow.controller.exception.security.*;
import net.idonow.controller.mapping.ResponseMappers;
import net.idonow.entity.Country;
import net.idonow.entity.Role;
import net.idonow.entity.User;
import net.idonow.entity.VerificationToken;
import net.idonow.entity.enums.TokenType;
import net.idonow.repository.UserRepository;
import net.idonow.security.enums.RoleType;
import net.idonow.security.jwt.common.AuthenticationRequest;
import net.idonow.security.service.common.JwtService;
import net.idonow.security.service.common.LoggedUserStorageService;
import net.idonow.service.common.MailSenderService;
import net.idonow.service.common.PhoneNumberVerificationService;
import net.idonow.service.common.StorageService;
import net.idonow.service.entity.CountryService;
import net.idonow.service.entity.RoleService;
import net.idonow.service.entity.UserService;
import net.idonow.service.entity.VerificationTokenService;
import net.idonow.transform.user.UserResponse;
import net.idonow.transform.user.registration.UserRequest;
import net.idonow.transform.user.restore.PasswordResetRequest;
import net.idonow.transform.user.restore.TokenVerificationRequest;
import net.idonow.transform.user.restore.TokenVerificationResponse;
import net.idonow.transform.user.update.EmailUpdateRequest;
import net.idonow.transform.user.update.PasswordChangeRequest;
import net.idonow.transform.user.update.UserInfoUpdateRequest;
import net.idonow.transform.user.verification.PhoneNumberVerificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static net.idonow.common.cache.EntityCacheNames.*;
import static net.idonow.common.cache.TemplateCacheNames.APP_USER_PASSWORD_RESET_TOKEN_PREFIX;
import static net.idonow.common.util.LogUtils.Action.CREATE;
import static net.idonow.common.util.LogUtils.Action.UPDATE;
import static net.idonow.common.util.LogUtils.auditLog;
import static net.idonow.common.util.LogUtils.buildJSONMessage;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private PhoneNumberVerificationService phoneNumberVerificationService;
    private VerificationTokenService verificationTokenService;
    private MailSenderService mailSenderService;
    private CountryService countryService;
    private RoleService roleService;
    private StorageService storageService;
    private JwtService jwtService;
    private LoggedUserStorageService loggedAppUserStorageService;
    private PasswordEncoder passwordEncoder;
    private TransactionTemplate transactionTemplate;
    private RedisTemplate<String, String> redisTemplate;
    private AppConfig appConfig;
    private LimitsConfig limitsConfig;
    private StorageConfig storageConfig;
    private LocaleUtils localeUtils;
    private ResponseMappers responseMappers;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setPhoneNumberVerificationService(PhoneNumberVerificationService phoneNumberVerificationService) {
        this.phoneNumberVerificationService = phoneNumberVerificationService;
    }

    @Autowired
    public void setVerificationTokenService(VerificationTokenService verificationTokenService) {
        this.verificationTokenService = verificationTokenService;
    }

    @Autowired
    public void setMailSenderService(MailSenderService mailSenderService) {
        this.mailSenderService = mailSenderService;
    }

    @Autowired
    public void setCountryService(CountryService countryService) {
        this.countryService = countryService;
    }

    @Autowired
    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }

    @Autowired
    public void setStorageService(StorageService storageService) {
        this.storageService = storageService;
    }

    @Autowired
    public void setJwtService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Autowired
    public void setLoggedAppUserStorageService(@Qualifier("loggedAppUserStorageServiceImpl") LoggedUserStorageService loggedAppUserStorageService) {
        this.loggedAppUserStorageService = loggedAppUserStorageService;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
        this.transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.transactionTemplate.setTimeout(15);
    }

    @Autowired
    public void setRedisTemplate(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Autowired
    public void setAppConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @Autowired
    public void setLimitsConfig(LimitsConfig limitsConfig) {
        this.limitsConfig = limitsConfig;
    }

    @Autowired
    public void setStorageConfig(StorageConfig storageConfig) {
        this.storageConfig = storageConfig;
    }

    @Autowired
    public void setLocaleUtils(LocaleUtils localeUtils) {
        this.localeUtils = localeUtils;
    }

    @Autowired
    public void setResponseMappers(ResponseMappers responseMappers) {
        this.responseMappers = responseMappers;
    }

    @Override
    public List<User> getAllEntities() {
        return userRepository.findAll(); // TODO use pager
    }

    @Override
    public User getEntity(Long id) {
        Optional<User> optUser = userRepository.findById(id);
        if (optUser.isEmpty()) {
            throw new EntityNotFoundException(String.format("User with id {%s} not found", id));
        }
        return optUser.get();
    }

    @Override
    public User getUserByEmail(String email) throws UsernameNotFoundException {
        Optional<User> optUser = userRepository.findByEmailIgnoreCase(email);
        if (optUser.isEmpty()) {
            throw new EmailNotFoundException(String.format("User with email '%s' not found", email));
        }
        return optUser.get();
    }

    @Override
    @Cacheable(value = USER, key = "#email.toLowerCase()", unless = "#result == null")
    public User getActiveUserByEmailCached(String email) throws AuthenticationException {
        return this.getActiveUserByEmail(email);
    }

    @Override
    @CachePut(value = USER, key = "#email.toLowerCase()", unless = "#result == null")
    public User getActiveUserByEmail(String email) throws AuthenticationException {
        Optional<User> optUser = userRepository.findByEmailIgnoreCase(email);
        if (optUser.isEmpty()) {
            throw new EmailNotFoundException(String.format("User with email '%s' not found", email));
        }
        User user = optUser.get();
        this.verifyUser(user);
        return user;
    }

    @Override
    public User getActiveUserByPhoneNumber(String phoneNumber) throws AuthenticationException {
        // Get app country code and try to validate phone number
        CountryCode countryCode = CountryCode.valueOf(appConfig.getAppCountryCode());
        boolean isValid = PhoneNumberUtils.validate(phoneNumber, countryCode);
        if (isValid) {
            phoneNumber = PhoneNumberUtils.normalize(phoneNumber, countryCode);
        } else {
            throw new PhoneNumberNotFoundException("Invalid phone number");
        }
        Optional<User> optUser = userRepository.findByPhoneNumber(phoneNumber);
        if (optUser.isEmpty()) {
            throw new PhoneNumberNotFoundException(String.format("User with phone number '%s' not found", phoneNumber));
        }
        User user = optUser.get();
        this.verifyUser(user);
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
    public String getHiddenPhoneNumber(String phoneNumber) {
        return phoneNumber.replaceAll(".(?=\\d{3})", "*");
    }

    @Override
    public void refreshTokens(final HttpServletRequest request, HttpServletResponse response) {
        String email = jwtService.decodeRefreshJwt(request);
        User user = this.getActiveUserByEmail(email);
        jwtService.provideTokens(email, this.getAuthorities(user.getRole()), request, response);
    }

    @Override
    public UserResponse registerAndSendVerificationMessage(UserRequest userRequest) {
        // Normalize already validated phone number
        String normalizedPhoneNumber = PhoneNumberUtils.normalize(userRequest.getPhoneNumber(), CountryCode.valueOf(appConfig.getAppCountryCode()));

        if (emailExists(userRequest.getEmail())) {
            throw new InvalidRequestDataException("Email already exists", Map.of("email", localeUtils.getLocalizedMessage("error.entity.already-exists")));
        }
        if (phoneNumberExists(normalizedPhoneNumber)) {
            throw new InvalidRequestDataException("Phone number already exists", Map.of("phoneNumber", localeUtils.getLocalizedMessage("error.entity.already-exists")));
        }
        Country country = countryService.getByCode(appConfig.getAppCountryCode());
        Role userRole = roleService.getByRoleType(RoleType.CLIENT);
        User user = new User();
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setPhoneNumber(normalizedPhoneNumber);
        user.setActive(true);
        user.setEmailVerified(false);
        user.setPhoneNumberVerified(false);
        user.setCountry(country);
        user.setRole(userRole);
        user.setPasswordUpdated(LocalDateTime.now());
        user.setEmailUpdated(LocalDateTime.now());
        userRepository.save(user);
        // Sent verification message
        phoneNumberVerificationService.startVerification(user.getPhoneNumber());
        auditLog.info(buildJSONMessage(CREATE, responseMappers.userToSelfResponse(user)));
        log.info("User successfully registered: {}", user);
        return responseMappers.userToResponse(user);
    }

    @Override
    public User startPhoneNumberVerification(AuthenticationRequest authenticationRequest) {
        User user = checkCredentialsAndGet(authenticationRequest);
        if (user.isPhoneNumberVerified()) {
            throw new ActionNotAllowedException("Phone number is already verified");
        }
        boolean sent = phoneNumberVerificationService.startVerification(user.getPhoneNumber());
        if (sent) {
            return user;
        } else {
            throw new ActionNotAllowedException(localeUtils.getLocalizedMessage("error.request-failed"), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = USER, key = "#result.email"),
            @CacheEvict(value = PROFESSIONAL, key = "#result.email")
    })
    public User confirmPhoneNumberVerification(PhoneNumberVerificationRequest phoneNumberVerificationRequest, HttpServletRequest request, HttpServletResponse response) {
        User user = this.getEntity(phoneNumberVerificationRequest.getUserId());
        if (user.isPhoneNumberVerified()) {
            throw new ActionNotAllowedException("Phone number is already verified");
        }
        boolean verified = phoneNumberVerificationService.confirmVerification(user.getPhoneNumber(), phoneNumberVerificationRequest.getToken());
        if (verified) {
            user.setPhoneNumberVerified(true);
            userRepository.save(user);
            log.info("User phone number successfully verified: {}", user);
            jwtService.provideTokens(user.getEmail(), this.getAuthorities(user.getRole()), request, response);
            return user;
        } else {
            throw new InvalidRequestDataException("Invalid token", Map.of("code", localeUtils.getLocalizedMessage("error.invalid-token")));
        }
    }

    @Override
    public boolean startEmailVerification(Principal principal) {
        User commandOwner = getUserByEmail(principal.getName());
        if (commandOwner.isEmailVerified()) {
            throw new ActionNotAllowedException("Email is already verified");
        }
        if (verificationTokenService.sendingNewTokenNotAllowed(commandOwner, TokenType.EMAIL_CONFIRM)) {
            throwResendTimeNotExceededException();
        }
        // Create VerificationToken
        VerificationToken verificationToken = verificationTokenService.createToken(commandOwner, TokenType.EMAIL_CONFIRM);
        try {
            // Try to send an email
            mailSenderService.sendEmailVerificationMessage(commandOwner, verificationToken.getToken());
            log.info("Email verification message send to user email: {}", commandOwner);
            return true;
        } catch (MessagingException e) {
            log.warn("Failed to send email verification message to user {}. Cause: {}", commandOwner, e.getMessage());
            verificationTokenService.deleteToken(verificationToken.getId());
            return false;
        }
    }

    @Override
    public boolean sendPasswordResetMessageToUser(String username) {
        try {
            if (sentEmailAsUsername(username)) {
                User user = this.getActiveUserByEmail(username);
                if (!user.isEmailVerified()) {
                    throw new EmailNotVerifiedException("Email is not verified");
                }
                return sendResetMessageToEmail(user);
            } else {
                return sendResetMessageToPhoneNumber(this.getActiveUserByPhoneNumber(username));
            }
        } catch (AuthenticationException authenticationException) {
            // TODO - email the reason if necessary
            log.warn("Password reset rejected for account '{}': {}", username, authenticationException.getMessage());
            return false;
        }
    }

    @Override
    public TokenVerificationResponse verifyResetToken(TokenVerificationRequest tokenVerificationRequest) {
        try {
            if (sentEmailAsUsername(tokenVerificationRequest.getUsername())) {
                User user = this.getActiveUserByEmail(tokenVerificationRequest.getUsername());
                return verifyResetTokenByEmail(user, tokenVerificationRequest.getToken());
            } else {
                User user = this.getActiveUserByPhoneNumber(tokenVerificationRequest.getUsername());
                return verifyResetTokenByPhoneNumber(user, tokenVerificationRequest.getToken());
            }
        } catch (AuthenticationException exception) {
            throw new ActionNotAllowedException("Error while verifying token");
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = USER, key = "#passwordResetRequest.userId"),
            @CacheEvict(value = PROFESSIONAL, key = "#passwordResetRequest.userId")
    })
    public void resetPassword(PasswordResetRequest passwordResetRequest) {
        String token = this.getPasswordResetTokenFromCache(passwordResetRequest.getUserId());
        if (token == null || passwordResetRequest.getToken().compareTo(token) != 0) {
            throw new ActionNotAllowedException(localeUtils.getLocalizedMessage("error.data-verification"));
        }
        this.deletePasswordResetTokenFromCache(passwordResetRequest.getUserId());
        User user = this.getEntity(passwordResetRequest.getUserId());
        user.setPassword(passwordEncoder.encode(passwordResetRequest.getPassword()));
        user.setPasswordUpdated(LocalDateTime.now());
        userRepository.save(user);
        // Interrupt active session
        loggedAppUserStorageService.interruptUserSession(user.getEmail());
        log.info("Password has been successfully reset: " + user);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = USER, key = "#principal.name"),
            @CacheEvict(value = PROFESSIONAL, key = "#principal.name")
    })
    public void confirmEmailVerification(Principal principal, String token) {
        User user = getUserByEmail(principal.getName());
        if (user.isEmailVerified()) {
            throw new ActionNotAllowedException("Email is already verified");
        }
        try {
            boolean verified = verificationTokenService.verifyToken(user, TokenType.EMAIL_CONFIRM, token);
            if (verified) {
                user.setEmailVerified(true);
                userRepository.save(user);
                log.info("Account email verification successfully completed: " + user);
            } else {
                throw new InvalidRequestDataException(
                        "Invalid token",
                        Map.of("token", localeUtils.getLocalizedMessage("error.invalid-token")));
            }
        } catch (InvalidVerificationTokenException ex) {
            throw new ActionNotAllowedException(ex.getMessage());
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = USER, key = "#principal.name"),
            @CacheEvict(value = PROFESSIONAL, key = "#principal.name")
    })
    public User updateEmail(Principal principal, EmailUpdateRequest emailUpdateRequest, HttpServletRequest request, HttpServletResponse response) {
        String oldEmail = principal.getName();
        User commandOwner = this.checkCredentialsAndGet(new AuthenticationRequest(oldEmail, emailUpdateRequest.getPassword()));

        if (commandOwner.getEmail().compareTo(emailUpdateRequest.getEmail().toLowerCase()) == 0) {
            throw new ActionNotAllowedException("The new address must be different from the current one");
        }
        // Check if email already exists in db
        if (emailExists(emailUpdateRequest.getEmail())) {
            throw new InvalidRequestDataException("Unique constraint violation", Map.of("email", localeUtils.getLocalizedMessage("error.entity.already-exists")));
        }
        // After updating new email should be verified
        commandOwner.setEmail(emailUpdateRequest.getEmail());
        commandOwner.setEmailUpdated(LocalDateTime.now());
        commandOwner.setEmailVerified(false);
        transactionTemplate.executeWithoutResult(status -> {
            // Invalid all verification tokens for user old email
            verificationTokenService.deleteByUser(commandOwner.getId());
            userRepository.save(commandOwner);
        });
        // After updating email give user new tokens pair and remove old tokens
        loggedAppUserStorageService.interruptUserSession(oldEmail);
        jwtService.provideTokens(commandOwner.getEmail(), this.getAuthorities(commandOwner.getRole()), request, response);

        // Sent email verification message
        boolean sent = startEmailVerification(commandOwner::getEmail);
        if (!sent) {
            log.warn("Failed to sent verification message to user email: " + commandOwner.getEmail());
        }
        // Send notification to old email
        try {
            mailSenderService.sendEmailUpdateNotification(commandOwner.getFirstName(), oldEmail);
            log.info("Email update notification sent to user old email: " + oldEmail);
        } catch (MessagingException e) {
            log.error("Failed to send an notification to old email: '{}' [Exception message: {}]", commandOwner, e.getMessage());
        }

        auditLog.info(buildJSONMessage(UPDATE, responseMappers.userToSelfResponse(commandOwner)));
        return commandOwner;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = USER, key = "#result.email"),
            @CacheEvict(value = PROFESSIONAL, key = "#result.email")
    })
    public User updateInfo(UserInfoUpdateRequest userInfoUpdateRequest) {
        // Get user from db and set info
        User commandOwner = getUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        commandOwner.setFirstName(userInfoUpdateRequest.getFirstName());
        commandOwner.setLastName(userInfoUpdateRequest.getLastName());
        userRepository.save(commandOwner);
        log.info("User info updated: {}", commandOwner);
        return commandOwner;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = USER, key = "#result.email"),
            @CacheEvict(value = PROFESSIONAL, key = "#result.email")
    })
    public User updateUserById(Long id,UserInfoUpdateRequest userInfoUpdateRequest) {
        // Get user from db and set info
        User loadedUser = this.getEntity(id);
        loadedUser.setFirstName(userInfoUpdateRequest.getFirstName());
        loadedUser.setLastName(userInfoUpdateRequest.getLastName());
        userRepository.save(loadedUser);
        log.info("User info updated: {}", loadedUser);
        return loadedUser;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.MANDATORY)
    @Caching(evict = {
            @CacheEvict(value = USER, key = "#user.email"),
            @CacheEvict(value = PROFESSIONAL, key = "#user.email")
    })
    public void updateRole(User user, RoleType roleType, HttpServletRequest request, HttpServletResponse response) {
        Role role = roleService.getByRoleType(roleType);
        user.setRole(role);
        loggedAppUserStorageService.interruptUserSession(user.getEmail());
        jwtService.provideTokens(user.getEmail(), this.getAuthorities(user.getRole()), request, response);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = USER, key = "#principal.name"),
            @CacheEvict(value = PROFESSIONAL, key = "#principal.name")
    })
    public void changePassword(PasswordChangeRequest passwordChangeRequest, Principal principal) {
        // Get user from db
        User commandOwner = getUserByEmail(principal.getName());
        // Check if current password sent by user is correct
        if (!passwordEncoder.matches(passwordChangeRequest.getOldPassword(), commandOwner.getPassword())) {
            String message = "Sent invalid password";
            log.warn(message + ": " + commandOwner.getEmail());
            throw new InvalidRequestDataException(message, Map.of("oldPassword", localeUtils.getLocalizedMessage("error.password.invalid-old-password")));
        }
        // Don't allow to set old password
        if (passwordEncoder.matches(passwordChangeRequest.getNewPassword(), commandOwner.getPassword())) {
            String message = "Repetition of old password";
            log.warn(message + ": " + commandOwner);
            throw new InvalidRequestDataException(message, Map.of("newPassword", localeUtils.getLocalizedMessage("error.password.old-pwd-forbidden")));
        }
        commandOwner.setPassword(passwordEncoder.encode(passwordChangeRequest.getNewPassword()));
        commandOwner.setPasswordUpdated(LocalDateTime.now());
        userRepository.save(commandOwner);
        log.info("User password changed: " + commandOwner);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    @Caching(evict = {
            @CacheEvict(value = USER, key = "#principal.name"),
            @CacheEvict(value = PROFESSIONAL, key = "#principal.name")
    })
    public String uploadProfilePicture(MultipartFile profilePicture, Principal principal) throws IOException {
        // Get user from db
        User commandOwner = getUserByEmail(principal.getName());
        if (commandOwner.getProfilePictureName() != null) {
            throw new ActionNotAllowedException("Profile picture already exists");
        }
        String profilePictureName = storageService.createFileName(profilePicture);
        commandOwner.setProfilePictureName(profilePictureName);
        userRepository.flush();
        return storageService.uploadImage(profilePicture, storageConfig.getProfilePictureDirectory(), profilePictureName, true);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    @Caching(evict = {
            @CacheEvict(value = USER, key = "#principal.name"),
            @CacheEvict(value = PROFESSIONAL, key = "#principal.name")
    })
    public String uploadCoverPicture(MultipartFile coverPicture, Principal principal) throws IOException {
        // Get user from db
        User commandOwner = getUserByEmail(principal.getName());
        if (commandOwner.getCoverPictureName() != null) {
            throw new ActionNotAllowedException("Cover picture already exists");
        }
        String coverPictureName = storageService.createFileName(coverPicture);
        commandOwner.setCoverPictureName(coverPictureName);
        userRepository.flush();
        return storageService.uploadImage(coverPicture, storageConfig.getProfilePictureDirectory(), coverPictureName, true);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    @Caching(evict = {
            @CacheEvict(value = USER, key = "#principal.name"),
            @CacheEvict(value = PROFESSIONAL, key = "#principal.name")
    })
    public void deleteProfilePicture(Principal principal) {
        // Get user from db
        User commandOwner = getUserByEmail(principal.getName());
        if (commandOwner.getProfilePictureName() == null) {
            throw new ActionNotAllowedException("Profile picture is not found");
        }
        String fileToDelete = commandOwner.getProfilePictureName();
        commandOwner.setProfilePictureName(null);
        userRepository.flush();
        storageService.deleteObject(storageConfig.getProfilePictureDirectory(), fileToDelete);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    @Caching(evict = {
            @CacheEvict(value = USER, key = "#principal.name"),
            @CacheEvict(value = PROFESSIONAL, key = "#principal.name")
    })
    public void deleteCoverPicture(Principal principal) {
        // Get user from db
        User commandOwner = getUserByEmail(principal.getName());
        if (commandOwner.getCoverPictureName() == null) {
            throw new ActionNotAllowedException("Cover picture is not found");
        }
        String fileToDelete = commandOwner.getCoverPictureName();
        commandOwner.setCoverPictureName(null);
        userRepository.flush();
        storageService.deleteObject(storageConfig.getProfilePictureDirectory(), fileToDelete);
    }

    @Override
    public Page<User> getListOfUsers(String firstName, String email, Boolean active, Pageable pageable) {
        String sanitizedFirstName = (firstName != null) ? firstName : "";
        String sanitizedEmail = (email != null) ? email : "";
        Boolean sanitizedActive = (active != null) ? active : true;
        return userRepository.findUsersWithFilters(sanitizedFirstName, sanitizedEmail, sanitizedActive, pageable);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = USER, key = "#id"),
            @CacheEvict(value = ALL_USERS, allEntries = true)
    })
    public void deleteUserById(Long id) {
        this.getEntity(id);
        userRepository.deleteById(id);
        log.info("User successfully deleted: {id = {}}", id);

    }




    /* -- PRIVATE METHODS -- */

    private boolean phoneNumberExists(String phoneNumber) {
        return userRepository.existsUserByPhoneNumber(phoneNumber);
    }

    private boolean emailExists(String email) {
        return userRepository.existsUserByEmailIgnoreCase(email);
    }

    private void verifyUser(User user) throws AuthenticationException {
        if (!user.isActive()) {
            String message = String.format("User account of '%s' is not active", user.getEmail());
            throw new AccountNotActiveException(message);
        }
        if (!user.isPhoneNumberVerified()) {
            String message = String.format("User account of '%s' is not phone number verified", user.getEmail());
            throw new PhoneNumberNotVerifiedException(message);
        }
    }

    private User checkCredentialsAndGet(AuthenticationRequest authenticationRequest) {
        Optional<User> optUser = userRepository.findByEmailIgnoreCase(authenticationRequest.getUsername());
        if (optUser.isEmpty() || !passwordEncoder.matches(authenticationRequest.getPassword(), optUser.get().getPassword())) {
            throw new ActionNotAllowedException(localeUtils.getLocalizedMessage("error.invalid-user-pwd"));
        }
        return optUser.get();
    }

    private void throwResendTimeNotExceededException() {
        throw new InvalidRequestDataException(
                "The resend time has not been exceeded",
                Map.of("email", localeUtils.getLocalizedMessage("error.token-already-sent", new Integer[]{limitsConfig.getResendTokenAfterSeconds()})),
                HttpStatus.CONFLICT);
    }

    private boolean sentEmailAsUsername(String username) {
        return username.contains("@");
    }

    private boolean sendResetMessageToEmail(User user) {
        if (verificationTokenService.sendingNewTokenNotAllowed(user, TokenType.PASSWORD_RESET)) {
            log.warn("Password reset message is already sent to user email: '{}'", user.getEmail());
            return true;
        }
        // Create VerificationToken
        VerificationToken verificationToken = verificationTokenService.createToken(user, TokenType.PASSWORD_RESET);
        try {
            // Try to send an email
            mailSenderService.sendPasswordResetMessage(user, verificationToken.getToken());
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

    private boolean sendResetMessageToPhoneNumber(User user) {
        Boolean sent = transactionTemplate.execute(status -> {
            // If there is password reset token being sent user via email invalidate it
            verificationTokenService.deleteByUserAndTokenType(user, TokenType.PASSWORD_RESET);
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

    /**
     * If the user first enters email to reset password and then without verifying token enters phone number to send reset token,
     * the token being sent to email automatically becomes invalid after phone number token is being successfully sent
     */
    private TokenVerificationResponse verifyResetTokenByEmail(User user, String token) {
        try {
            boolean verified = verificationTokenService.verifyToken(user, TokenType.PASSWORD_RESET, token);
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
    private TokenVerificationResponse verifyResetTokenByPhoneNumber(User user, String token) {
        if (verificationTokenService.tokenExists(user, TokenType.PASSWORD_RESET)) {
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

    private String getPasswordResetTokenCacheKey(Long userId) {
        return APP_USER_PASSWORD_RESET_TOKEN_PREFIX + userId;
    }

    private String createPasswordResetToken(Long userId) {
        String token = UUID.randomUUID().toString();
        String key = getPasswordResetTokenCacheKey(userId);
        redisTemplate.opsForValue().set(key, token, 10L, TimeUnit.MINUTES);
        log.info("Password reset token provided: userId = {}", userId);
        return token;
    }

    @Nullable
    private String getPasswordResetTokenFromCache(Long userId) {
        String key = getPasswordResetTokenCacheKey(userId);
        return this.redisTemplate.opsForValue().get(key);
    }

    private void deletePasswordResetTokenFromCache(Long userId) {
        String key = getPasswordResetTokenCacheKey(userId);
        String value = redisTemplate.opsForValue().getAndDelete(key);
        if (value != null) {
            log.info("Password change token deleted: userId = {}", userId);
        }
    }
}
