package net.idonow.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import net.idonow.common.api.ApiResponse;
import net.idonow.common.util.LocaleUtils;
import net.idonow.common.validation.constraints.ValidMultipartFile;
import net.idonow.controller.mapping.ResponseMappers;
import net.idonow.entity.User;
import net.idonow.security.jwt.common.AuthenticationRequest;
import net.idonow.service.entity.UserService;
import net.idonow.transform.user.UserResponse;
import net.idonow.transform.user.UserSelfResponse;
import net.idonow.transform.user.registration.UserRequest;
import net.idonow.transform.user.restore.PasswordResetRequest;
import net.idonow.transform.user.restore.TokenVerificationRequest;
import net.idonow.transform.user.restore.TokenVerificationResponse;
import net.idonow.transform.user.update.EmailUpdateRequest;
import net.idonow.transform.user.update.PasswordChangeRequest;
import net.idonow.transform.user.update.UserInfoUpdateRequest;
import net.idonow.transform.user.verification.PhoneNumberVerificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@Validated
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private LocaleUtils localeUtils;
    private ResponseMappers responseMappers;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setLocaleUtils(LocaleUtils localeUtils) {
        this.localeUtils = localeUtils;
    }

    @Autowired
    public void setResponseMappers(ResponseMappers responseMappers) {
        this.responseMappers = responseMappers;
    }

    // Mapping to get self info
    @GetMapping("self-info")
    public ApiResponse<UserSelfResponse> getSelfInfo(Principal principal) {
        User commandOwner = userService.getActiveUserByEmailCached(principal.getName());
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.entity.found"), responseMappers.userToSelfResponse(commandOwner));
    }

    // Mapping for registration
    @PostMapping("registration")
    public ApiResponse<UserResponse> registerUser(@Valid @RequestBody UserRequest userRequest) {
        UserResponse userResponse = userService.registerAndSendVerificationMessage(userRequest);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.user.register"), userResponse);
    }
    // Mapping to start email verification
    @PostMapping("email/start-verification")
    public ApiResponse<String> verifyEmail(Principal principal) {
        boolean success = userService.startEmailVerification(principal);
        if (success) {
            return ApiResponse.ok(localeUtils.getLocalizedMessage("success.email.token-sent", new String[]{principal.getName()}));
        } else {
            return ApiResponse.error(localeUtils.getLocalizedMessage("error.request-failed"), HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    // Mapping to confirm User email
    @PostMapping("email/confirm-verification")
    public ApiResponse<String> confirmEmail(@RequestParam("token") @NotBlank(message = "{validation.empty}") @Pattern(regexp = "[0-9]{6}", message = "{validation.regexp.digits}") String token,
                                            Principal principal) {
        userService.confirmEmailVerification(principal, token);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.email.verified"));
    }

    @PostMapping("phone-number/start-verification")
    public ApiResponse<UserResponse> startUserPhoneNumberVerification(@Valid @RequestBody AuthenticationRequest authRequest) {
        User user = userService.startPhoneNumberVerification(authRequest);
        String hiddenPhoneNumber = userService.getHiddenPhoneNumber(user.getPhoneNumber());
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.phone-number.token-sent", new String[]{hiddenPhoneNumber}), responseMappers.userToResponse(user));
    }

    @PostMapping("phone-number/confirm-verification")
    public ApiResponse<UserSelfResponse> verifyUserPhoneNumber(@Valid @RequestBody PhoneNumberVerificationRequest verificationRequest, HttpServletRequest request, HttpServletResponse response) {
        User user = userService.confirmPhoneNumberVerification(verificationRequest, request, response);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.phone-number.verified"), responseMappers.userToSelfResponse(user));
    }

    // Public endpoint for sending a password reset message to the User
    @PostMapping("forgot-password")
    public ApiResponse<String> forgotPassword(@RequestParam @NotBlank(message = "{validation.empty}") String username) {
        boolean sent = userService.sendPasswordResetMessageToUser(username);
        if (sent) {
            return ApiResponse.ok(localeUtils.getLocalizedMessage("success.user.reset-message-sent"));
        }
        return ApiResponse.error(localeUtils.getLocalizedMessage("error.request-failed"), HttpStatus.REQUEST_TIMEOUT, null);
    }

    // Public endpoint to validate token sent by above endpoint and in case of valid token sent token in response to change password
    @PostMapping("forgot-password/verify")
    public ApiResponse<TokenVerificationResponse> verifyResetToken(@RequestBody @Valid TokenVerificationRequest tokenVerificationRequest) {
        TokenVerificationResponse tokenVerificationResponse = userService.verifyResetToken(tokenVerificationRequest);
        return ApiResponse.ok("Successfully verified", tokenVerificationResponse);
    }

    // Public endpoint to validate token sent by above endpoint and set new password
    @PostMapping("reset-password")
    public ApiResponse<String> resetPassword(@RequestBody @Valid PasswordResetRequest passwordResetRequest) {
        userService.resetPassword(passwordResetRequest);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.user.password-change"));
    }

    // Mapping to update email
    @PutMapping("email")
    public ApiResponse<UserSelfResponse> updateEmail(@RequestBody @Valid EmailUpdateRequest emailUpdateRequest, Principal principal,
                                                     HttpServletRequest request, HttpServletResponse response) {
        User user = userService.updateEmail(principal, emailUpdateRequest, request, response);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.data.update"), responseMappers.userToSelfResponse(user));
    }

    // Mapping to update self info
    @PutMapping("info")
    public ApiResponse<UserResponse> updateSelfInfo(@RequestBody @Valid UserInfoUpdateRequest userInfoUpdateRequest) {
        UserResponse userResponse = responseMappers.userToResponse(userService.updateInfo(userInfoUpdateRequest));
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.data.update"), userResponse);
    }

    // Mapping to change password
    @PutMapping("change-password")
    public ApiResponse<String> changePassword(@RequestBody @Valid PasswordChangeRequest passwordChangeRequest, Principal principal) {
        userService.changePassword(passwordChangeRequest, principal);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.user.password-change"));
    }

    // Mapping to upload profile picture
    @PostMapping("profile-picture")
    public ApiResponse<String> uploadProfilePicture(@RequestParam @ValidMultipartFile(message = "{validation.multipart-file}") MultipartFile profilePicture, Principal principal) throws IOException {
        String url = userService.uploadProfilePicture(profilePicture, principal);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.image.upload"), url);
    }

    // Mapping to upload profile picture
    @PostMapping("cover-picture")
    public ApiResponse<String> uploadCoverPicture(@RequestParam @ValidMultipartFile(message = "{validation.multipart-file}") MultipartFile coverPicture, Principal principal) throws IOException {
        String url = userService.uploadCoverPicture(coverPicture, principal);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.image.upload"), url);
    }

    // Mapping to delete profile picture
    @DeleteMapping("profile-picture")
    public ApiResponse<String> deleteProfilePicture(Principal principal) {
        userService.deleteProfilePicture(principal);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.image.delete"));
    }

    // Mapping to delete profile picture
    @DeleteMapping("cover-picture")
    public ApiResponse<String> deleteCoverPicture(Principal principal) {
        userService.deleteCoverPicture(principal);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.image.delete"));
    }

}
