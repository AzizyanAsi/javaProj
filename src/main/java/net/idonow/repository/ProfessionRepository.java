package net.idonow.repository;

import net.idonow.entity.Profession;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfessionRepository extends JpaRepository<Profession, Long> {

    @EntityGraph(attributePaths = {"professionCategory"}, type = EntityGraph.EntityGraphType.LOAD)
    Optional<Profession> findById(Long id);

    List<Profession> findByProfessionCategory_Id(Long professionCategoryId);
}
