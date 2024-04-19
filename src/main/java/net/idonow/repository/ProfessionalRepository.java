package net.idonow.repository;

import net.idonow.entity.Professional;
import net.idonow.transform.professional.ProfessionalView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface ProfessionalRepository extends JpaRepository<Professional, Long> {

    @EntityGraph(attributePaths = {"services", "workingSamples", "user"})
    Optional<Professional> findById(Long id);

    @EntityGraph(attributePaths = {"services", "workingSamples", "user"})
    Optional<Professional> findProfessionalByIdAndActiveIsTrue(Long id);

    @EntityGraph(attributePaths = {"services", "workingSamples", "user"})
    Optional<Professional> findProfessionalByUser_Email(String email);

    boolean existsProfessionalById(Long id);

    @Query(value = """
                    SELECT p.user_id as id, p.location as location, u.first_name as firstName, u.last_name as lastName, u.profile_picture_name as profilePictureName
                    FROM professional p
                    INNER JOIN "user" u on u.id = p.user_id
                    INNER JOIN service s on p.user_id = s.professional_id
                    WHERE s.profession_id = :professionId AND p.active AND ST_DWithin(p.location, CAST( ST_Point(:longitude, :latitude, 4326) AS geography), :distance)
                    LIMIT 20
                   """, nativeQuery = true)
    Set<ProfessionalView> findNearestProfessionals(@Param("professionId") Long professionId, @Param("distance") Double distance, @Param("latitude") Double latitude, @Param("longitude") Double longitude);


    @EntityGraph(attributePaths = {"services", "workingSamples", "user.role"})
    @Query(value = """
                    SELECT p
                    FROM Professional p
                    WHERE (:active IS NULL OR p.active = :active)
                    AND (LOWER(p.user.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')) OR :firstName = '')
                    AND (LOWER(p.user.email) LIKE LOWER(CONCAT('%', :email, '%')) OR :email = '')
            """   )
    Page<Professional> findProfessionalsWithFilters(
            @Param("active") Boolean active,
            @Param("firstName") String firstName,
            @Param("email") String email,
            Pageable pageable);

}
