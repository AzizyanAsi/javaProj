package net.idonow.service.entity.impl;

import lombok.extern.slf4j.Slf4j;
import net.idonow.controller.exception.common.EntityNotFoundException;
import net.idonow.entity.Role;
import net.idonow.repository.RoleRepository;
import net.idonow.security.enums.RoleType;
import net.idonow.service.entity.RoleService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static net.idonow.common.cache.EntityCacheNames.ALL_ROLES;
import static net.idonow.common.cache.EntityCacheNames.ROLE;

@Slf4j
@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Cacheable(value = ALL_ROLES)
    public List<Role> getAllEntities() {
        return roleRepository.findAll();
    }

    @Override
    @Cacheable(value = ROLE, key = "#id", unless = "#result == null")
    public Role getEntity(Long id) {
        Optional<Role> optRole = roleRepository.findById(id);
        if (optRole.isEmpty()) {
            throw new EntityNotFoundException(String.format("Role with id {%d} not found", id));
        }
        return optRole.get();
    }

    @Override
    public Role getByRoleType(RoleType roleType) {
        Optional<Role> optRole = roleRepository.findByRoleType(roleType);
        if (optRole.isEmpty()) {
            throw new EntityNotFoundException(String.format("Role with role type {%s} not found", roleType));
        }
        return optRole.get();
    }
}
