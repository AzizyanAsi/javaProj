package net.idonow.transform.user.restore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TokenVerificationResponse {
    private Long userId;
    private String token;
}
