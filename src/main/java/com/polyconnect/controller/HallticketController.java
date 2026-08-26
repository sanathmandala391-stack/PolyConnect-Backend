package com.polyconnect.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.polyconnect.service.HallticketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hallticket")
public class HallticketController {

    private final HallticketService hallticketService;

    public HallticketController(HallticketService hallticketService) {
        this.hallticketService = hallticketService;
    }

    @GetMapping("/exam-months")
    public ResponseEntity<JsonNode> getExamMonths(
            @RequestParam(defaultValue = "1") int studentTypeId) {
        return ResponseEntity.ok(hallticketService.getExamMonths(studentTypeId));
    }

    @GetMapping("/view")
    public ResponseEntity<JsonNode> viewHallticket(
            @RequestParam String pin,
            @RequestParam(defaultValue = "123") String dob, // SBTET seems to accept a placeholder here
            @RequestParam int emyr,
            @RequestParam(defaultValue = "1") int studentTypeId) {
        return ResponseEntity.ok(hallticketService.getHallticket(pin, dob, emyr, studentTypeId));
    }
}