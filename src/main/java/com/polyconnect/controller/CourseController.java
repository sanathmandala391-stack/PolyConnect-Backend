package com.polyconnect.controller;



import com.polyconnect.entity.CollegeCourse;
import com.polyconnect.entity.Course;
import com.polyconnect.service.CourseService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /** Public: full branch/course list, e.g. for the "Diploma Courses" tab. */
    @GetMapping
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }

    /** Public: current academic year, for the "Data Shown for Current Academic Year" badge. */
    @GetMapping("/academic-year")
    public Map<String, String> getCurrentAcademicYear() {
        return Map.of("academicYear", courseService.getCurrentAcademicYearLive());
    }

    /**
     * Public: colleges offering a given branch this academic year (drill-down when a
     * student clicks a branch row, e.g. "AA - ARCHITECTURAL ASSISTANTSHIP").
     * academicYear defaults to the live SBTET value if not passed, matching how
     * getCurrentAcademicYear() returns "2026-27" while stored rows use the same string.
     */
    @GetMapping("/{branchCode}/colleges")
    public List<CollegeCourse> getCollegesForBranch(
            @PathVariable String branchCode,
            @RequestParam(required = false) String academicYear) {
        String year = (academicYear != null && !academicYear.isBlank())
                ? academicYear
                : courseService.getCurrentAcademicYearLive();
        return courseService.getCollegesForBranch(branchCode.toUpperCase(), year);
    }

    /** Public: reverse lookup — all branches a given college offers (for the "Colleges" tab). */
    @GetMapping("/colleges/{collegeCode}")
    public List<CollegeCourse> getCoursesForCollege(
            @PathVariable String collegeCode,
            @RequestParam(required = false) String academicYear) {
        String year = (academicYear != null && !academicYear.isBlank())
                ? academicYear
                : courseService.getCurrentAcademicYearLive();
        return courseService.getCoursesForCollege(collegeCode, year);
    }

    /**
     * Admin-only: trigger a full re-sync from SBTET (branch list + every branch's
     * college offerings). Call this manually, or wire it to a scheduled n8n/cron job
     * later — SBTET's branch/college data changes rarely, so it doesn't need polling.
     */
    @PostMapping("/sync")
//    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> syncFromSbtet() {
        int coursesCount = courseService.syncCourses();
        int collegeRowsCount = courseService.syncCollegeWiseCourses();
        return Map.of(
                "coursesSynced", coursesCount,
                "collegeCourseRowsSynced", collegeRowsCount
        );
    }
    @GetMapping("/college-summary")
    public List<Map<String, Object>> getCollegeSummary() {
        return courseService.getCollegeSummary();
    }
}