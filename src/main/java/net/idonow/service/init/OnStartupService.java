package net.idonow.service.init;

import net.idonow.entity.Role;
import net.idonow.security.enums.RoleType;
import net.idonow.service.entity.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OnStartupService {

    private final RoleService roleService;

    public OnStartupService(RoleService roleService) {
        this.roleService = roleService;
    }


    public void validateACL() {
        List<Role> roles = roleService.getAllEntities();

        // -- CHECK Roles

        Set<RoleType> rolesDS = roles.stream()
                .map(Role::getRoleType)
                .collect(Collectors.toSet());
        Set<RoleType> rolesEnum = Set.of(RoleType.values());
        if (rolesDS.size() != rolesEnum.size() || !rolesDS.containsAll(rolesEnum)) {
            throw new Error("Roles do not match those of the datasource!");
        }
    }
}
