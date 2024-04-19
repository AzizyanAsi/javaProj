package net.idonow.transform.user.update;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailUpdateRequest {

    @NotBlank(message = "{validation.empty}")
    @Email(message = "{validation.email}")
    private String email;

    @NotBlank(message = "{validation.empty}")
    private String password;

}
