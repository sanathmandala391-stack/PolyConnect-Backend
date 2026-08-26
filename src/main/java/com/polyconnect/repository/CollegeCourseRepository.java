package com.polyconnect.repository;



import com.polyconnect.entity.CollegeCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CollegeCourseRepository extends JpaRepository<CollegeCourse, Long> {

    List<CollegeCourse> findByBranchCodeAndAcademicYearOrderByCollegeNameAsc(String branchCode, String academicYear);

    List<CollegeCourse> findByCollegeCodeAndAcademicYearOrderByCourseNameAsc(String collegeCode, String academicYear);

    long countByBranchCodeAndAcademicYear(String branchCode, String academicYear);

    @Transactional
    void deleteByBranchCodeAndAcademicYear(String branchCode, String academicYear);
}