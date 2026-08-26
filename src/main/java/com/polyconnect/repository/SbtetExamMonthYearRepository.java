package com.polyconnect.repository;

import com.polyconnect.entity.SbtetExamMonthYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SbtetExamMonthYearRepository extends JpaRepository<SbtetExamMonthYear, Long> {
    Optional<SbtetExamMonthYear> findBySbtetId(Integer sbtetId);
    List<SbtetExamMonthYear> findByActiveTrueOrderBySbtetIdDesc();
}
