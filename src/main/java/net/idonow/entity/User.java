package net.idonow.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.idonow.entity.templates.AbstractPersistentObject;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "user",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"email"}),
                @UniqueConstraint(columnNames = {"phone_number"}),
                @UniqueConstraint(columnNames = {"profile_picture_name"}),
                @UniqueConstraint(columnNames = {"cover_picture_name"})
        })
public class User extends AbstractPersistentObject {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "u_seq")
    @SequenceGenerator(name = "u_seq", sequenceName = "user_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    @JsonIgnore
    private String password;

    @Column(name = "phone_number", nullable = false, length = 25)
    private String phoneNumber;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "phone_number_verified", nullable = false)
    private boolean phoneNumberVerified;

    @Column(name = "profile_picture_name")
    private String profilePictureName;

    @Column(name = "cover_picture_name")
    private String coverPictureName;

    // Account configs key value pairs
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "account_config")
    private Map<String, Object> accountConfig;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id")
    private Country country;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id")
    private Role role;

    @CreationTimestamp
    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @Column(name = "password_updated", nullable = false)
    private LocalDateTime passwordUpdated;

    @Column(name = "email_updated", nullable = false)
    private LocalDateTime emailUpdated;

    @UpdateTimestamp
    @Column(name = "updated", nullable = false)
    private LocalDateTime updated;

//    @OneToOne(fetch = FetchType.LAZY, optional = false)
//    @MapsId
//    @JoinColumn(name = "user_id")
//    private Professional professional;
    @OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE)
    private Professional professional;

    @Column(name = "socket_session_id")//todo add in db
    private String socketSessionId;

    @Column(name = "online")//todo add in db
    private boolean online;

    // CUSTOM METHODS

    @PreUpdate
    @PrePersist
    public void normalizeEmail() {
        this.email = this.email.toLowerCase();
    }
}
