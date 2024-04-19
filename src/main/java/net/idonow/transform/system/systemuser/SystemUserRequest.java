package net.idonow.transform.system.systemuser;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.idonow.common.validation.constraints.ValidPhoneNumber;
import net.idonow.security.enums.RoleType;

import static net.idonow.common.data.RegexPatterns.REGX_PASSWORD;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SystemUserRequest {
    @NotBlank(message = "{validation.empty}")
    @Size(min = 2, message = "{validation.size.min}")
    private String firstName;

    @NotBlank(message = "{validation.empty}")
    @Size(min = 2, message = "{validation.size.min}")
    private String lastName;

    @NotBlank(message = "{validation.empty}")
    @Email(message = "{validation.email}")
    private String email;

    @NotBlank(message = "{validation.empty}")
    @Size(max = 25, message = "{validation.size.max}")
    @ValidPhoneNumber(message = "{validation.phone}")
    private String phoneNumber;

    @NotBlank(message = "{validation.empty}")
    @Pattern(
            regexp = REGX_PASSWORD,
            message = "{validation.password}"
    )
    private String password;

//    @NotBlank(message = "{validation.empty}")//todo check
    private RoleType userRole;
}
