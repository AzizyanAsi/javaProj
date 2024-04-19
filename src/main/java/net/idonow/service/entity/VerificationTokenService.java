package net.idonow.service.entity;

import net.idonow.controller.exception.common.InvalidVerificationTokenException;
import net.idonow.entity.User;
import net.idonow.entity.VerificationToken;
import net.idonow.entity.enums.TokenType;
import net.idonow.entity.system.SystemUser;

import java.time.LocalDateTime;

public interface VerificationTokenService {

    VerificationToken createToken(User user, TokenType tokenType);
//    VerificationToken createTokenForSystemUser(SystemUser user, TokenType tokenType);

    /**
     * Verification token is being deleted once it's approved, expired or max attempts to check have been reached
     */
    boolean verifyToken(User user, TokenType tokenType, String token) throws InvalidVerificationTokenException;

    boolean sendingNewTokenNotAllowed(User user, TokenType tokenType);

    boolean resendAllowed(LocalDateTime created);

    boolean tokenExists(User user, TokenType tokenType);

    void deleteToken(Long tokenId);

    void deleteByUser(Long userId);

    void deleteByUserAndTokenType(User user, TokenType tokenType);
}
