package net.idonow.controller.system;

import jakarta.validation.constraints.Positive;
import net.idonow.common.api.ApiResponse;
import net.idonow.common.util.LocaleUtils;
import net.idonow.entity.Role;
import net.idonow.service.entity.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/system/roles")
public class RoleController {

    private final RoleService roleService;
    private LocaleUtils localeUtils;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @Autowired
    public void setLocaleUtils(LocaleUtils localeUtils) {
        this.localeUtils = localeUtils;
    }

    // Mapping to get all Roles
    @GetMapping
    public ApiResponse<List<Role>> getRoles() {
        List<Role> roleResponses = roleService.getAllEntities();
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.entities.found"), roleResponses);
    }

    // Mapping to get specific Role by id
    @GetMapping("{id}")
    public ApiResponse<Role> getRole(@PathVariable("id") @Positive(message = "{validation.positive}") Long id) {
        Role role = roleService.getEntity(id);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.entity.found"), role);
    }
}
