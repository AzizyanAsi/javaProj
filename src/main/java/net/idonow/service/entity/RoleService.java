package net.idonow.service.entity;

import net.idonow.entity.Role;
import net.idonow.security.enums.RoleType;
import net.idonow.service.entity.templates.EntityReadService;

public interface RoleService extends EntityReadService<Role> {
    Role getByRoleType(RoleType roleType);
}
