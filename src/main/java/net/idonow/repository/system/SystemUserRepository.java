package net.idonow.repository.system;

import net.idonow.entity.system.SystemUser;
import net.idonow.security.enums.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SystemUserRepository extends JpaRepository<SystemUser, Long> {
    @EntityGraph(attributePaths = { "role" })
    Optional<SystemUser> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = { "role" })
    Optional<SystemUser> findByPhoneNumber(String phoneNumber);

    Page<SystemUser> findAllByRoleRoleType(RoleType role, Pageable pageable);

    @Query("SELECT u FROM SystemUser u WHERE u.role.roleType = :roleType "
            + "AND (:name IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%'))) "
            + "AND (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) "
            + "AND (:active IS NULL OR u.active = :active)")
    Page<SystemUser> findAllByRoleRoleTypeAndNameAndEmailAndActive(@Param("roleType") RoleType roleType,
            @Param("name") String name, @Param("email") String email, @Param("active") Boolean active,
            Pageable pageable);

    SystemUser findBySocketSessionId(String socketSessionId);

    SystemUser findByEmailAndRoleRoleType(String email, RoleType userType);
}
