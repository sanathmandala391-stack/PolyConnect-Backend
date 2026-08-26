package com.polyconnect.repository;

import com.polyconnect.entity.Doubt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoubtRepository extends JpaRepository<Doubt, Long> {
    List<Doubt> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    List<Doubt> findBySubjectCodeOrderByCreatedAtDesc(String subjectCode);
}
