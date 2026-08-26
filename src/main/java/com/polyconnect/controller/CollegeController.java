package com.polyconnect.controller;

import com.polyconnect.entity.Branch;
import com.polyconnect.entity.College;
import com.polyconnect.service.CollegeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CollegeController {

    private final CollegeService collegeService;

    public CollegeController(CollegeService collegeService) {
        this.collegeService = collegeService;
    }

    @GetMapping("/colleges/public")
    public ResponseEntity<List<College>> getPublicColleges() {
        return ResponseEntity.ok(collegeService.getAllActiveColleges());
    }

    @GetMapping("/branches/public")
    public ResponseEntity<List<Branch>> getPublicBranches() {
        return ResponseEntity.ok(collegeService.getAllActiveBranches());
    }

    @PostMapping("/admin/colleges")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<College> createCollege(
        @RequestBody College college,
        @RequestParam(required = false) List<String> branchCodes
    ) {
        return ResponseEntity.ok(collegeService.createCollege(college, branchCodes));
    }
}
