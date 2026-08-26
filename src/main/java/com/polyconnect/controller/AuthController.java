package com.polyconnect.controller;

import com.polyconnect.entity.Hod;
import com.polyconnect.entity.Role;
import com.polyconnect.entity.Student;
import com.polyconnect.entity.User;
import com.polyconnect.repository.UserRepository;
import com.polyconnect.security.TenantContext;
import com.polyconnect.security.UserPrincipal;
import com.polyconnect.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register/student")
    public ResponseEntity<Map<String, Object>> registerStudent(
        @RequestBody Student student,
        @RequestParam String collegeCode,
        @RequestParam String branchCode
    ) {
        Student created = authService.registerStudent(student, collegeCode, branchCode);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Registration submitted successfully. Pending approval from department HOD.");
        response.put("pin", created.getPin());
        response.put("status", created.getStatus());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/hod")
    public ResponseEntity<Map<String, Object>> registerHod(
        @RequestBody Hod hod,
        @RequestParam String collegeCode,
        @RequestParam String branchCode
    ) {
        Hod created = authService.registerHod(hod, collegeCode, branchCode);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "HOD Registration submitted successfully. Pending verification from system Admin.");
        response.put("email", created.getEmail());
        response.put("status", created.getStatus());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        String identifier = loginRequest.get("identifier");
        String password = loginRequest.get("password");

        User user = authService.login(identifier, password);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("token", user.getToken());
        response.put("user", user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        UserPrincipal principal = TenantContext.getCurrentUser();
        User user = userRepository.findById(principal.getId()).orElseThrow();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("user", user);
        return ResponseEntity.ok(response);
    }
}
