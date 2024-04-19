package net.idonow.repository;

import net.idonow.entity.User;
import net.idonow.entity.VerificationToken;
import net.idonow.entity.enums.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByUserAndTokenType(User user, TokenType tokenType);

    boolean existsVerificationTokenByUserAndTokenType(User user, TokenType tokenType);

    @Modifying
    @Query("DELETE FROM VerificationToken t WHERE t.user.id=:userId")
    void deleteByUser(Long userId);

    void deleteByUserAndTokenType(User user, TokenType tokenType);

}
