package net.idonow.transform.system.systemuser.converter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import net.idonow.common.validation.constraints.ValidPhoneNumber;

@Getter
@Setter
public class SystemUserUpdateRequest {
    @Size(min = 2, message = "{validation.size.min}")
    private String firstName;
    @Size(min = 2, message = "{validation.size.min}")
    private String lastName;
    @Email(message = "{validation.email}")
    private String email;
    @Size(max = 25, message = "{validation.size.max}")
    @ValidPhoneNumber(message = "{validation.phone}")
    private String phoneNumber;
    //    private RoleType userRole;

    private Boolean active;
}
