package com.polyconnect.repository;

import com.polyconnect.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Integer> {
    List<Course> findAllByOrderByBranchNameAsc();
    Optional<Course> findByBranchCode(String branchCode);
    boolean existsByBranchCode(String branchCode);
}
