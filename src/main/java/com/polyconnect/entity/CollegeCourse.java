package com.polyconnect.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "college_courses",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_college_branch_year",
                columnNames = {"college_code", "branch_code", "academic_year"}
        )
)
public class CollegeCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "college_code", nullable = false, length = 10)
    private String collegeCode;

    @Column(name = "college_name", nullable = false, length = 200)
    private String collegeName;

    @Column(name = "branch_code", nullable = false, length = 10)
    private String branchCode;

    @Column(name = "course_name", nullable = false, length = 200)
    private String courseName;

    @Column(name = "intake", nullable = false)
    private int intake;

    @Column(name = "academic_year", nullable = false, length = 20)
    private String academicYear;

    public CollegeCourse() {}

    public Long getId() { return id; }

    public String getCollegeCode() { return collegeCode; }
    public void setCollegeCode(String collegeCode) { this.collegeCode = collegeCode; }

    public String getCollegeName() { return collegeName; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }

    public String getBranchCode() { return branchCode; }
    public void setBranchCode(String branchCode) { this.branchCode = branchCode; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public int getIntake() { return intake; }
    public void setIntake(int intake) { this.intake = intake; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
}