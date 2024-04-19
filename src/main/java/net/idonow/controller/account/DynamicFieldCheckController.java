package net.idonow.controller.account;

import jakarta.validation.Valid;
import net.idonow.transform.user.check.PasswordCheckRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api")
public class DynamicFieldCheckController {

    @PostMapping("password-check")
    public ResponseEntity<Void> passwordCheck(@Valid @RequestBody PasswordCheckRequest ignoredPasswordCheckRequest) {

        // Dummy check - request validation will intercept any constraint violation (see PasswordCheckRequest)
        return ResponseEntity.ok().build();
    }
}
