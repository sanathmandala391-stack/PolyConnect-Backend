package com.polyconnect.repository;

import com.polyconnect.entity.SbtetExamType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SbtetExamTypeRepository extends JpaRepository<SbtetExamType, Long> {
    Optional<SbtetExamType> findBySbtetExamTypeId(Integer sbtetExamTypeId);
    List<SbtetExamType> findByActiveTrue();
}
