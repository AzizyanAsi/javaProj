package net.idonow.repository;

import net.idonow.entity.Country;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Long> {

    @EntityGraph(attributePaths = {"currency"})
    Optional<Country> findByCountryCode(String code);
}
