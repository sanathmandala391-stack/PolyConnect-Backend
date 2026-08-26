package com.polyconnect.service;



import com.fasterxml.jackson.databind.JsonNode;
import com.polyconnect.entity.CollegeCourse;
import com.polyconnect.entity.Course;
import com.polyconnect.integration.sbtet.SbtetClient;
import com.polyconnect.repository.CollegeCourseRepository;
import com.polyconnect.repository.CourseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class CourseService {

    private static final Logger log = LoggerFactory.getLogger(CourseService.class);

    private final SbtetClient sbtetClient;
    private final CourseRepository courseRepository;
    private final CollegeCourseRepository collegeCourseRepository;

    public CourseService(SbtetClient sbtetClient,
                         CourseRepository courseRepository,
                         CollegeCourseRepository collegeCourseRepository) {
        this.sbtetClient = sbtetClient;
        this.courseRepository = courseRepository;
        this.collegeCourseRepository = collegeCourseRepository;
    }

    // ---------------------------------------------------------
    // SYNC (call this from an admin-only endpoint or a scheduled job —
    // course/branch lists change rarely, so this does NOT need to run often)
    // ---------------------------------------------------------

    /**
     * Pulls the master branch/course list from SBTET and upserts it into MySQL.
     */
//    @Transactional
//    public int syncCourses() {
//        JsonNode table = sbtetClient.getAllCourses();
//        int count = 0;
//
//        if (table.isArray()) {
//            for (JsonNode node : table) {
//                int courseId = node.path("courseid").asInt();
//                Course course = courseRepository.findById(courseId).orElse(new Course());
//                course.setCourseId(courseId);
//                course.setBranchCode(node.path("BranchCode").asText(""));
//                course.setBranchName(node.path("BranchName").asText(""));
//                course.setCourseName(node.path("course_name").asText(""));
//                course.setActive(node.path("IsActive").asBoolean(true));
//                courseRepository.save(course);
//                count++;
//            }
//        }
//
//        log.info("Synced {} courses/branches from SBTET.", count);
//        return count;
//    }

    @Transactional
    public int syncCourses() {
        JsonNode response = sbtetClient.getAllCourses();

        if (response == null || response.isMissingNode()) {
            log.error("SBTET courses response was null or empty");
            return 0;
        }

        // Handle double-encoded JSON string if returned as text node
        if (response.isTextual()) {
            try {
                response = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response.asText());
            } catch (Exception e) {
                log.error("Failed to parse inner JSON string", e);
                return 0;
            }
        }

        // Extract the "Table" array from {"Table": [...]}
        JsonNode table = response.has("Table") ? response.path("Table") : response;
        int count = 0;

        if (table.isArray()) {
            for (JsonNode node : table) {
                int courseId = node.path("courseid").asInt(0);
                if (courseId == 0) continue; // Skip empty nodes

                Course course = courseRepository.findById(courseId).orElse(new Course());
                course.setCourseId(courseId);
                course.setBranchCode(node.path("BranchCode").asText(""));
                course.setBranchName(node.path("BranchName").asText(""));
                course.setCourseName(node.path("course_name").asText(""));
                course.setActive(node.path("IsActive").asBoolean(true));

                courseRepository.save(course);
                count++;
            }
        }

        log.info("Synced {} courses/branches from SBTET.", count);
        return count;
    }

    /**
     * For every branch in the courses table, pulls the college-wise offering list
     * from SBTET for the CURRENT academic year and replaces the stored rows for
     * that branch+year. Call syncCourses() first so the branch list is populated.
     */
//    @Transactional
//    public int syncCollegeWiseCourses() {
//        String academicYear = sbtetClient.getCurrentAcademicYear(); // e.g. "2026-27"
//        List<Course> allCourses = courseRepository.findAll();
//        int totalRows = 0;
//
//        for (Course course : allCourses) {
//            String branchCode = course.getBranchCode();
//            try {
//                JsonNode table = sbtetClient.getCollegeWiseCourses(branchCode);
//
//                // Replace this branch+year's rows cleanly (SBTET occasionally returns
//                // duplicate rows for the same college — dedupe happens naturally since
//                // we wipe and re-insert every sync rather than trying to diff).
//                collegeCourseRepository.deleteByBranchCodeAndAcademicYear(branchCode, academicYear);
//
//                if (table.isArray()) {
//                    java.util.Set<String> seenCollegeCodes = new java.util.HashSet<>();
//                    for (JsonNode node : table) {
//                        String collegeCode = node.path("CollegeCode").asText("");
//                        // Skip exact duplicate (CollegeCode, BranchCode) pairs — SBTET's
//                        // own API sometimes returns the same college twice for a branch.
//                        if (!seenCollegeCodes.add(collegeCode)) continue;
//
//                        CollegeCourse cc = new CollegeCourse();
//                        cc.setCollegeCode(collegeCode);
//                        cc.setCollegeName(node.path("CollegeName").asText(""));
//                        cc.setBranchCode(node.path("BranchCode").asText(branchCode));
//                        cc.setCourseName(node.path("CourseName").asText(""));
//                        cc.setIntake(node.path("Intake").asInt(0));
//                        cc.setAcademicYear(academicYear);
//                        collegeCourseRepository.save(cc);
//                        totalRows++;
//                    }
//                }
//            } catch (Exception ex) {
//                // Don't let one branch's failure abort the whole sync — SBTET's API
//                // can be flaky per-endpoint; log and continue with the rest.
//                log.warn("Failed to sync college-wise courses for branch {}: {}", branchCode, ex.getMessage());
//            }
//        }
//
//        log.info("Synced {} college-course rows across {} branches for AY {}.", totalRows, allCourses.size(), academicYear);
//        return totalRows;
//    }



    @Transactional
    public int syncCollegeWiseCourses() {
        String rawAcademicYear = getCurrentAcademicYearLive(); // e.g., "2026-27"
        List<Course> allCourses = courseRepository.findAll();
        int totalRows = 0;

        for (Course course : allCourses) {
            String branchCode = course.getBranchCode();
            try {
                JsonNode response = sbtetClient.getCollegeWiseCourses(branchCode);
                JsonNode table = response.path("Table"); // Extract "Table" array

                // Wipe existing rows for both string representations to prevent stale data
                collegeCourseRepository.deleteByBranchCodeAndAcademicYear(branchCode, rawAcademicYear);

                if (table.isArray()) {
                    java.util.Set<String> seenCollegeCodes = new java.util.HashSet<>();
                    for (JsonNode node : table) {
                        String collegeCode = node.path("CollegeCode").asText("");
                        if (!seenCollegeCodes.add(collegeCode)) continue;

                        CollegeCourse cc = new CollegeCourse();
                        cc.setCollegeCode(collegeCode);
                        cc.setCollegeName(node.path("CollegeName").asText(""));
                        cc.setBranchCode(node.path("BranchCode").asText(branchCode));
                        cc.setCourseName(node.path("CourseName").asText(""));
                        cc.setIntake(node.path("Intake").asInt(0));

                        // Normalize saved academic year to match the live API year ("2026-27")
                        cc.setAcademicYear(rawAcademicYear);

                        collegeCourseRepository.save(cc);
                        totalRows++;
                    }
                }
            } catch (Exception ex) {
                log.warn("Failed to sync college-wise courses for branch {}: {}", branchCode, ex.getMessage());
            }
        }

        log.info("Synced {} college-course rows across {} branches for AY {}.", totalRows, allCourses.size(), rawAcademicYear);
        return totalRows;
    }
    // ---------------------------------------------------------
    // READS (what the frontend actually calls)
    // ---------------------------------------------------------

    public List<Course> getAllCourses() {
        return courseRepository.findAllByOrderByBranchNameAsc();
    }

    public List<CollegeCourse> getCollegesForBranch(String branchCode, String academicYear) {
        return collegeCourseRepository.findByBranchCodeAndAcademicYearOrderByCollegeNameAsc(branchCode, academicYear);
    }

    public List<CollegeCourse> getCoursesForCollege(String collegeCode, String academicYear) {
        return collegeCourseRepository.findByCollegeCodeAndAcademicYearOrderByCourseNameAsc(collegeCode, academicYear);
    }

    public String getCurrentAcademicYearLive() {
        return sbtetClient.getCurrentAcademicYear();
    }

    public List<Map<String, Object>> getCollegeSummary() {
        JsonNode table = sbtetClient.getCollegeSummary();
        List<Map<String, Object>> summary = new java.util.ArrayList<>();

        int totalColleges = 0;
        int totalIntake = 0;

        if (table.isArray()) {
            for (JsonNode node : table) {
                String typeName = node.path("TypeName").asText("");
                int collegeCount = node.path("CollegeCount").asInt(0);
                int intake = node.path("TotalIntake").asInt(0);

                Map<String, Object> row = new java.util.LinkedHashMap<>();
                row.put("typeName", typeName);
                row.put("collegeCount", collegeCount);
                row.put("totalIntake", intake);
                summary.add(row);

                totalColleges += collegeCount;
                totalIntake += intake;
            }
        }

        // Add the computed Total row (SBTET's raw API doesn't send one — the real
        // site's UI computes it client-side from the three type rows, same as here).
        Map<String, Object> totalRow = new java.util.LinkedHashMap<>();
        totalRow.put("typeName", "Total");
        totalRow.put("collegeCount", totalColleges);
        totalRow.put("totalIntake", totalIntake);
        summary.add(totalRow);

        return summary;
    }
}