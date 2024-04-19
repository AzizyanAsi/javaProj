package net.idonow.service.entity.impl;

import lombok.extern.slf4j.Slf4j;
import net.idonow.common.config.LimitsConfig;
import net.idonow.common.util.LocaleUtils;
import net.idonow.controller.exception.common.ActionNotAllowedException;
import net.idonow.controller.exception.common.InvalidVerificationTokenException;
import net.idonow.entity.User;
import net.idonow.entity.VerificationToken;
import net.idonow.entity.enums.TokenType;
import net.idonow.entity.system.SystemUser;
import net.idonow.repository.VerificationTokenRepository;
import net.idonow.service.entity.VerificationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;

@Slf4j
@Service
public class VerificationTokenServiceImpl implements VerificationTokenService {

    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final LimitsConfig limitsConfig;
    private LocaleUtils localeUtils;

    public VerificationTokenServiceImpl(VerificationTokenRepository verificationTokenRepository, PasswordEncoder passwordEncoder, LimitsConfig limitsConfig) {
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.limitsConfig = limitsConfig;
    }

    @Autowired
    public void setLocaleUtils(LocaleUtils localeUtils) {
        this.localeUtils = localeUtils;
    }

    @Override
    public VerificationToken createToken(User user, TokenType tokenType) {
        // Create token with length 6 and save
        String token = generateToken();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(passwordEncoder.encode(token));
        verificationToken.setUser(user);
        verificationToken.setTokenType(tokenType);
        verificationTokenRepository.save(verificationToken);
        // After saving token with encrypted code, reset the code with decrypted value for putting it in email message
        verificationToken.setToken(token);
        return verificationToken;
    }

    @Override
    public boolean verifyToken(User user, TokenType tokenType, String token) {
        Optional<VerificationToken> optToken = verificationTokenRepository.findByUserAndTokenType(user, tokenType);
        if (optToken.isPresent()) {
            VerificationToken verificationToken = optToken.get();
            // Check if expired
            if (tokenType.isExpirable() && tokenExpired(verificationToken.getCreated())) {
                this.deleteToken(verificationToken.getId());
                throw new InvalidVerificationTokenException("Token is expired");
            }
            // Increment check attempt number
            verificationToken.setAttemptNumber((short) (verificationToken.getAttemptNumber() + 1));
            if (checkingMaxAttemptReached(verificationToken)) {
                this.deleteToken(verificationToken.getId());
                throw new InvalidVerificationTokenException("Max check attempt reached");
            }
            boolean verified = passwordEncoder.matches(token, verificationToken.getToken());
            if (verified) {
                this.deleteToken(verificationToken.getId());
            } else {
                // Update check attempt number
                verificationTokenRepository.save(verificationToken);
            }
            return verified;
        } else {
            throw new ActionNotAllowedException(localeUtils.getLocalizedMessage("error.data-verification"));
        }
    }

    @Override
    public boolean sendingNewTokenNotAllowed(User user, TokenType tokenType) {
        Optional<VerificationToken> optToken = verificationTokenRepository.findByUserAndTokenType(user, tokenType);
        if (optToken.isEmpty()) {
            return false;
        }
        VerificationToken token = optToken.get();
        // Token can be expirable. Allow resending - if expired
        if (tokenType.isExpirable() && tokenExpired(token.getCreated())) {
            this.deleteToken(token.getId());
            return false;
        }
        // Allow resending - if limit is exceeded
        if (resendAllowed(token.getCreated())) {
            this.deleteToken(token.getId());
            return false;
        }
        return true;
    }

    @Override
    public boolean resendAllowed(LocalDateTime created) {
        LocalDateTime resendLimitDT = created.plus(limitsConfig.getResendTokenAfterSeconds(), SECONDS);
        return LocalDateTime.now().isAfter(resendLimitDT);
    }

    @Override
    public boolean tokenExists(User user, TokenType tokenType) {
        return verificationTokenRepository.existsVerificationTokenByUserAndTokenType(user, tokenType);
    }

    @Override
    public void deleteToken(Long tokenId) {
        verificationTokenRepository.deleteById(tokenId);
    }

    @Override
    public void deleteByUser(Long userId) {
        verificationTokenRepository.deleteByUser(userId);
    }

    @Override
    public void deleteByUserAndTokenType(User user, TokenType tokenType) {
        verificationTokenRepository.deleteByUserAndTokenType(user, tokenType);
    }

    /* PRIVATE METHODS */

    private String generateToken() {
        SecureRandom secureRandom = new SecureRandom();
        IntStream intStream = secureRandom.ints(1, 100000, 999999);
        return intStream.mapToObj(String::valueOf).collect(Collectors.joining());
    }

    private boolean checkingMaxAttemptReached(VerificationToken verificationToken) {
        return verificationToken.getAttemptNumber() > limitsConfig.getTokenCheckMaxAttempts();
    }

    private boolean tokenExpired(LocalDateTime created) {
        LocalDateTime expirationLimitDT = created.plus(limitsConfig.getTokenExpirationAfterMinutes(), MINUTES);
        return LocalDateTime.now().isAfter(expirationLimitDT);
    }

}
