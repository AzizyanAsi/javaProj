package net.idonow.entity.system;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.idonow.entity.User;
import net.idonow.entity.enums.TokenType;
import net.idonow.entity.system.SystemUser;
import net.idonow.entity.templates.AbstractPersistentObject;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
@DynamicInsert
@Table(
        name = "system_verification_token",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"system_token"}),
                @UniqueConstraint(columnNames = {"system_user_id", "token_type"})
        })
public class SystemVerificationToken extends AbstractPersistentObject {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "v_seq")
    @SequenceGenerator(name = "s_v_seq", sequenceName = "system_verification_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "system_token", nullable = false, updatable = false)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "system_user_id")
    private SystemUser systemUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false, length = 20)
    private TokenType tokenType;

    @Column(name = "attempt_number", nullable = false, columnDefinition = "smallint default 0")
    private Short attemptNumber;

    @CreationTimestamp
    @Column(name = "created", nullable = false)
    private LocalDateTime created;
}

