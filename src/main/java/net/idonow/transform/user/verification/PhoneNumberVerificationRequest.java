package net.idonow.transform.user.verification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhoneNumberVerificationRequest {

    @Positive(message = "{validation.positive}")
    @NotNull(message = "{validation.empty}")
    private Long userId;

    @NotBlank(message = "{validation.empty}")
    @Pattern(regexp = "[0-9]{6}", message = "{validation.regexp.digits}")
    private String token;
}
