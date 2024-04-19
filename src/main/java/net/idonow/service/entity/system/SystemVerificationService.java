package net.idonow.service.entity.system;

import net.idonow.controller.exception.common.InvalidVerificationTokenException;
import net.idonow.entity.enums.TokenType;
import net.idonow.entity.system.SystemUser;
import net.idonow.entity.system.SystemVerificationToken;

import java.time.LocalDateTime;

public interface SystemVerificationService {
    SystemVerificationToken createTokenForSystemUser(SystemUser user, TokenType tokenType);

    boolean verifyTokenSystemUser(SystemUser user, TokenType tokenType, String token) throws
            InvalidVerificationTokenException;
    boolean sendingNewTokenNotAllowedToSystemUser(SystemUser user, TokenType tokenType);
    boolean tokenExistsForSystemUser(SystemUser user, TokenType tokenType);

    void deleteBySystemUserAndTokenType(SystemUser user, TokenType tokenType);

    void deleteToken(Long tokenId);
    boolean resendAllowed(LocalDateTime created);
}
