package net.idonow.transform.user.restore;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenVerificationRequest {

    @NotBlank(message = "{validation.empty}")
    private String username;

    @NotBlank(message = "{validation.empty}")
    @Pattern(regexp = "[0-9]{6}", message = "{validation.regexp.digits}")
    private String token;
}
