package net.idonow.transform.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.idonow.transform.role.RoleResponse;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSelfResponse extends UserResponse {
    private String email;
    private String phoneNumber;
    private boolean active;
    private boolean emailVerified;
    private boolean phoneNumberVerified;
    private String profilePictureUrl;
    private String coverPictureUrl;
    private Map<String, Object> accountConfig;
    private RoleResponse role;
    private LocalDateTime created;
    private LocalDateTime passwordUpdated;
    private LocalDateTime emailUpdated;
    private LocalDateTime updated;
}
