package net.idonow.transform.user.restore;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static net.idonow.common.data.RegexPatterns.REGX_PASSWORD;

@Getter
@Setter
@NoArgsConstructor
public class PasswordResetRequest {

    @Positive(message = "{validation.positive}")
    @NotNull(message = "{validation.empty}")
    private Long userId;

    @NotBlank(message = "{validation.empty}")
    private String token;

    @NotBlank(message = "{validation.empty}")
    @Pattern(regexp = REGX_PASSWORD, message = "{validation.password}")
    private String password;
}
