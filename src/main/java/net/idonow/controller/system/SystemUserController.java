package net.idonow.controller.system;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import net.idonow.common.api.ApiResponse;
import net.idonow.common.util.LocaleUtils;
import net.idonow.controller.mapping.ResponseMappers;
import net.idonow.entity.Professional;
import net.idonow.entity.User;
import net.idonow.entity.system.SystemUser;
import net.idonow.service.entity.ProfessionalService;
import net.idonow.service.entity.UserService;
import net.idonow.service.entity.system.SystemUserService;
import net.idonow.transform.professional.ProfessionalSelfResponse;
import net.idonow.transform.professional.ProfessionalUpdateRequest;
import net.idonow.transform.system.systemuser.SystemUserRequest;
import net.idonow.transform.system.systemuser.SystemUserResponse;
import net.idonow.transform.system.systemuser.converter.ISystemUserConverter;
import net.idonow.transform.system.systemuser.converter.SystemUserUpdateRequest;
import net.idonow.transform.user.UserResponse;
import net.idonow.transform.user.UserSelfResponse;
import net.idonow.transform.user.restore.PasswordResetRequest;
import net.idonow.transform.user.restore.TokenVerificationRequest;
import net.idonow.transform.user.restore.TokenVerificationResponse;
import net.idonow.transform.user.update.PasswordChangeRequest;
import net.idonow.transform.user.update.UserInfoUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Validated
@RestController
@RequestMapping("system/systemUser")
public class SystemUserController {
    private final SystemUserService systemUserService;
    private final UserService userService;
    private final ProfessionalService professionalService;
    private ISystemUserConverter systemUserConverter;
    private ResponseMappers responseMappers;
    private final LocaleUtils localeUtils;

    public SystemUserController(SystemUserService systemUserService, UserService userService,
            ProfessionalService professionalService, ISystemUserConverter systemUserConverter,
            LocaleUtils localeUtils) {
        this.systemUserService = systemUserService;
        this.userService = userService;
        this.professionalService = professionalService;
        this.systemUserConverter = systemUserConverter;
        this.localeUtils = localeUtils;
    }

    @Autowired
    public void setResponseMappers(ResponseMappers responseMappers) {
        this.responseMappers = responseMappers;
    }

    @PostMapping("registration")
    public ApiResponse<SystemUserResponse> registerSystemUser(@Valid @RequestBody SystemUserRequest userRequest) {
        SystemUser systemUser = systemUserService.registerSystemUser(userRequest);
        var responseUser = systemUserConverter.systemUserToResponse(systemUser);
        return ApiResponse.ok("success", responseUser);
    }

    @GetMapping("list")
    public ApiResponse<List<SystemUserResponse>> getListOfSystemUsers(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "created") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {
        Sort.Direction direction = sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(direction, sortBy));
        Page<SystemUser> usersList = systemUserService.getAllSystemUsers(pageable);
        List<SystemUserResponse> responseList = usersList.stream().map(systemUserConverter::systemUserToResponse)
                .toList();
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.entities.found"), responseList);
    }

    @GetMapping("list/supportAgents")
    public Page<SystemUserResponse> getListOfSupportAgents(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "created") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder, @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String email, @RequestParam(required = false) Boolean active) {
        Sort.Direction direction = sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(direction, sortBy));
        return systemUserService.getSupportAgents(firstName, email, active, pageable)
                .map(systemUserConverter::systemUserToResponse);
    }

    @GetMapping("/list/apUsers")
    public Page<UserSelfResponse> getListOfApUsers(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "created") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder, @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String email, @RequestParam(required = false) Boolean active) {
        Sort.Direction direction = sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(direction, sortBy));
        Page<User> usersPage = userService.getListOfUsers(firstName, email, active, pageable);
        return usersPage.map(responseMappers::userToSelfResponse);
    }

    @GetMapping("list/serviceProviders")
    public Page<ProfessionalSelfResponse> getListOfServiceProviders(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "created") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder, @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String email, @RequestParam(required = false) Boolean active) {
        Sort.Direction direction = sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(direction, sortBy));
        return professionalService.getListOfProfessionals(firstName, email, active, pageable)
                .map(responseMappers::professionalToSelfResponse);
    }

    @GetMapping("{id}")
    public ApiResponse<SystemUser> getSystemUser(
            @PathVariable("id") @Positive(message = "{validation.positive}") Long id) {
        SystemUser systemUser = systemUserService.getEntity(id);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.entity.found"), systemUser);
    }

    @PutMapping("update/{id}")
    public ApiResponse<SystemUserResponse> updateSystemUser(
            @PathVariable("id") @Positive(message = "{validation.positive}") Long id,
            @Valid @RequestBody SystemUserUpdateRequest userRequest) {
        var systemUserResp = systemUserConverter.systemUserToResponse(
                systemUserService.updateSystemUser(id, userRequest));
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.entity.found"), systemUserResp);
    }

    @PutMapping("update/apUser/{id}")
    public ApiResponse<UserResponse> updateApUser(
            @PathVariable("id") @Positive(message = "{validation.positive}") Long id,
            @RequestBody @Valid UserInfoUpdateRequest userInfoUpdateRequest) {
        UserResponse userResponse = responseMappers.userToResponse(
                userService.updateUserById(id, userInfoUpdateRequest));
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.data.update"), userResponse);
    }

    @PutMapping("update/serviceProvider/{id}")
    public ApiResponse<ProfessionalSelfResponse> updateProfessional(
            @PathVariable("id") @Positive(message = "{validation.positive}") Long id,
            @RequestBody @Valid ProfessionalUpdateRequest professionalUpdateRequest) {
        Professional professional = professionalService.updateProfessionalById(id, professionalUpdateRequest);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.data.update"),
                responseMappers.professionalToSelfResponse(professional));
    }

    @GetMapping("apUser/{id}")
    public ApiResponse<UserSelfResponse> getApUserById(
            @PathVariable("id") @Positive(message = "{validation.positive}") Long id) {
        User user = userService.getEntity(id);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.entity.found"),
                responseMappers.userToSelfResponse(user));
    }

    @GetMapping("serviceProvider/{id}")
    public ApiResponse<ProfessionalSelfResponse> getServiceProviderById(
            @PathVariable("id") @Positive(message = "{validation.positive}") Long id) {
        Professional prof = professionalService.getEntity(id);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.entity.found"), responseMappers.professionalToSelfResponse(prof));
    }

    @GetMapping(params = { "email" })
    public ApiResponse<SystemUser> getSystemUserByEmail(
            @RequestParam("email") @Email(message = "email is not valid") String email) {
        SystemUser systemUser = systemUserService.getActiveUserByEmail(email);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.entity.found"), systemUser);
    }

    //Public endpoint for sending a password reset message to the SystemUser
    @PostMapping("forgot-password")
    public ApiResponse<String> forgotPassword(
            @RequestParam @NotBlank(message = "{validation.empty}") String username) {
        boolean sent = systemUserService.sendPasswordResetMessageToUser(username);
        if (sent) {
            return ApiResponse.ok(localeUtils.getLocalizedMessage("success.user.reset-message-sent"));
        }
        return ApiResponse.error(localeUtils.getLocalizedMessage("error.request-failed"), HttpStatus.REQUEST_TIMEOUT,
                null);
    }

    // Public endpoint to validate token sent by above endpoint and in case of valid token sent token in response to change password
    @PostMapping("forgot-password/verify")
    public ApiResponse<TokenVerificationResponse> verifyResetToken(
            @RequestBody @Valid TokenVerificationRequest tokenVerificationRequest) {
        TokenVerificationResponse tokenVerificationResponse = systemUserService.verifyResetToken(
                tokenVerificationRequest);
        return ApiResponse.ok("Successfully verified", tokenVerificationResponse);
    }

    //   endpoint to validate token sent by above endpoint and set new password
    @PostMapping("reset-password")
    public ApiResponse<String> resetPassword(@RequestBody @Valid PasswordResetRequest passwordResetRequest) {
        systemUserService.resetPassword(passwordResetRequest);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.user.password-change"));
    }

    // Mapping to change password
    @PutMapping("change-password")
    public ApiResponse<String> changePassword(@RequestBody @Valid PasswordChangeRequest passwordChangeRequest,
            Principal principal) {
        systemUserService.changePassword(passwordChangeRequest, principal);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.user.password-change"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deleteSystemUser(@PathVariable Long id) {
        systemUserService.deleteSystemUserById(id);
        String successMessage = "User with ID " + id + " has been deleted successfully.";
        return ApiResponse.ok(successMessage);
    }

    @DeleteMapping("delete-apUser/{id}")
    public ApiResponse<String> deleteApUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        String successMessage = "User with ID " + id + " has been deleted successfully.";
        return ApiResponse.ok((successMessage));
    }

    @GetMapping("self-info")
    public ApiResponse<SystemUserResponse> getSelfInfo(Principal principal) {
        SystemUser loadedUser = systemUserService.getActiveUserByEmail(principal.getName());
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.entity.found"),
                systemUserConverter.systemUserToResponse(loadedUser));
    }
}
