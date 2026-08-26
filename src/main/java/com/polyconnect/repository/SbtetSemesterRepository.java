package com.polyconnect.repository;

import com.polyconnect.entity.SbtetSemester;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SbtetSemesterRepository extends JpaRepository<SbtetSemester, Long> {
    Optional<SbtetSemester> findBySemId(String semId);
    List<SbtetSemester> findByActiveTrueOrderBySequenceIdAsc();
}
