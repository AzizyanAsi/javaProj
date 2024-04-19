package net.idonow.repository;

import net.idonow.entity.User;
import net.idonow.security.enums.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"role"})
    Optional<User> findById(Long id);

    @EntityGraph(attributePaths = {"role"})
    Optional<User> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = {"role"})
    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsUserByPhoneNumber(String phoneNumber);

    boolean existsUserByEmailIgnoreCase(String email);


    @Query("SELECT u FROM User u " +
            "WHERE (:firstName IS NULL OR LOWER(u.firstName) LIKE CONCAT('%', LOWER(:firstName), '%')) " +
            "AND (:email IS NULL OR LOWER(u.email) LIKE CONCAT('%', LOWER(:email), '%')) " +
            "AND (:active IS NULL OR u.active = :active)")
    Page<User> findUsersWithFilters(
            @Param("firstName") String firstName,
            @Param("email") String email,
            @Param("active") Boolean active,
            Pageable pageable);
    @EntityGraph(attributePaths = {"role"})
    User findByEmailAndRoleRoleType(String email, RoleType role);

    User findBySocketSessionId(String socketSessionId);
}

