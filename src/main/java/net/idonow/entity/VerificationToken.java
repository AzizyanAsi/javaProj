package net.idonow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
@Table(name = "verification_token",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"token"}),
                @UniqueConstraint(columnNames = {"user_id", "token_type"})
        })
public class VerificationToken extends AbstractPersistentObject {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "v_seq")
    @SequenceGenerator(name = "v_seq", sequenceName = "verification_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "token", nullable = false, updatable = false)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false, length = 20)
    private TokenType tokenType;

    @Column(name = "attempt_number", nullable = false, columnDefinition = "smallint default 0")
    private Short attemptNumber;

    @CreationTimestamp
    @Column(name = "created", nullable = false)
    private LocalDateTime created;
}
