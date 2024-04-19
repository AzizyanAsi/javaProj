package net.idonow.repository.system;

import net.idonow.entity.enums.TokenType;
import net.idonow.entity.system.SystemUser;
import net.idonow.entity.system.SystemVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SystemVerificationRepository extends JpaRepository<SystemVerificationToken, Long> {

        Optional<SystemVerificationToken> findBySystemUserAndTokenType(SystemUser user, TokenType tokenType);

        boolean existsVerificationTokenBySystemUserAndTokenType(SystemUser user, TokenType tokenType);

//    @Modifying
//    @Query("DELETE FROM VerificationToken t WHERE t.user.id=:userId")
//    void deleteByUser(Long userId);
        void deleteBySystemUserAndTokenType(SystemUser user, TokenType tokenType);

}
