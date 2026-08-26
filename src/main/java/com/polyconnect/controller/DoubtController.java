//package com.polyconnect.controller;
//
//import com.polyconnect.entity.Doubt;
//import com.polyconnect.entity.User;
//import com.polyconnect.repository.UserRepository;
//import com.polyconnect.security.TenantContext;
//import com.polyconnect.service.DoubtService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/doubts")
//public class DoubtController {
//
//    private final DoubtService doubtService;
//    private final UserRepository userRepository;
//
//    public DoubtController(DoubtService doubtService, UserRepository userRepository) {
//        this.doubtService = doubtService;
//        this.userRepository = userRepository;
//    }
//
//    @PostMapping("/ask")
//    public ResponseEntity<Doubt> askDoubt(@RequestBody Doubt doubt) {
//        User student = userRepository.findById(TenantContext.getCurrentUser().getId()).orElseThrow();
//        return ResponseEntity.ok(doubtService.askDoubt(doubt, student));
//    }
//
//    @GetMapping("/my")
//    public ResponseEntity<List<Doubt>> getMyDoubts() {
//        Long studentId = TenantContext.getCurrentUser().getId();
//        return ResponseEntity.ok(doubtService.getStudentDoubts(studentId));
//    }
//}































































package com.polyconnect.controller;

import com.polyconnect.entity.Doubt;
import com.polyconnect.entity.User;
import com.polyconnect.repository.UserRepository;
import com.polyconnect.security.TenantContext;
import com.polyconnect.service.DoubtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doubts")
public class DoubtController {

    private final DoubtService doubtService;
    private final UserRepository userRepository;

    public DoubtController(DoubtService doubtService, UserRepository userRepository) {
        this.doubtService = doubtService;
        this.userRepository = userRepository;
    }

    @PostMapping("/ask")
    public ResponseEntity<Doubt> askDoubt(@RequestBody Doubt doubt) {
        User student = userRepository.findById(TenantContext.getCurrentUser().getId()).orElseThrow();
        return ResponseEntity.ok(doubtService.askDoubt(doubt, student));
    }

    @GetMapping("/my")
    public ResponseEntity<List<Doubt>> getMyDoubts() {
        Long studentId = TenantContext.getCurrentUser().getId();
        return ResponseEntity.ok(doubtService.getStudentDoubts(studentId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoubt(@PathVariable Long id) {
        Long studentId = TenantContext.getCurrentUser().getId();
        doubtService.deleteDoubt(id, studentId);
        return ResponseEntity.noContent().build();
    }
}
