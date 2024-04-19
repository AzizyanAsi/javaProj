package net.idonow.controller.account;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.idonow.common.api.ApiResponse;
import net.idonow.service.entity.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/token")
public class RefreshTokenController {

    private final UserService userService;

    public RefreshTokenController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("refresh")
    public ResponseEntity<Void> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        userService.refreshTokens(request, response);
        return ApiResponse.ok().build();
    }

}
