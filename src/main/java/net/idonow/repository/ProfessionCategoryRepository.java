package net.idonow.repository;

import net.idonow.entity.ProfessionCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessionCategoryRepository extends JpaRepository<ProfessionCategory, Long> {
}
