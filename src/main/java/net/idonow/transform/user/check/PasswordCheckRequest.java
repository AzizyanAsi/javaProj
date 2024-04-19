package net.idonow.transform.user.check;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static net.idonow.common.data.RegexPatterns.REGX_PASSWORD;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordCheckRequest {

    @NotBlank(message = "{validation.empty}")
    @Pattern(regexp = REGX_PASSWORD, message = "{validation.password}")
    private String password;
}
