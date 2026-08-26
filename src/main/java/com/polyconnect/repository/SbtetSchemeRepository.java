package com.polyconnect.repository;

import com.polyconnect.entity.SbtetScheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SbtetSchemeRepository extends JpaRepository<SbtetScheme, Long> {
    Optional<SbtetScheme> findBySchemeCode(String schemeCode);
    Optional<SbtetScheme> findBySbtetSchemeId(Integer sbtetSchemeId);
    List<SbtetScheme> findByActiveTrue();
}
