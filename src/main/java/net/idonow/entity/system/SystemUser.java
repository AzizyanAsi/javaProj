package net.idonow.entity.system;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.idonow.entity.Role;
import net.idonow.entity.templates.AbstractPersistentObject;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "system_user",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"contact_email"}),
                @UniqueConstraint(columnNames = {"contact_phone_number"})
})
public class SystemUser extends AbstractPersistentObject {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "su_seq")
    @SequenceGenerator(name = "su_seq", sequenceName = "system_user_seq")
    @Column(name = "id")
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "contact_email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    @JsonIgnore
    private String password;

    @Column(name = "contact_phone_number", nullable = false, length = 25)
    private String phoneNumber;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id")
    private Role role;

    @CreationTimestamp
    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @UpdateTimestamp
    @Column(name = "updated", nullable = false)
    private LocalDateTime updated;

    @Column(name = "socket_session_id")
    private String socketSessionId;

    @Column(name = "online")
    private boolean online;

    public void copyFieldsFrom(SystemUser systemUser) {
        if (systemUser.getFirstName() != null) {
            this.firstName = systemUser.getFirstName();
        }
        if (systemUser.getLastName() != null) {
            this.lastName = systemUser.getLastName();
        }
        if (systemUser.getEmail() != null) {
            this.email = systemUser.getEmail();
        }
        if (systemUser.getPhoneNumber() != null) {
            this.phoneNumber = systemUser.getPhoneNumber();
        }
        if (systemUser.getActive() != null) {
            this.active = systemUser.getActive();
        }
    }

}
