package com.polyconnect.controller;



import com.fasterxml.jackson.databind.JsonNode;
import com.polyconnect.service.SbtetFeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sbtet/fee")
@CrossOrigin(origins = "*")
public class SbtetFeeController {

    private final SbtetFeeService feeService;

    public SbtetFeeController(SbtetFeeService feeService) {
        this.feeService = feeService;
    }

    @GetMapping("/exam-month-year")
    public ResponseEntity<JsonNode> getExamMonthYear(@RequestParam(defaultValue = "Regular") String feeType) {
        return ResponseEntity.ok(feeService.getExamMonthYear(feeType));
    }

    @GetMapping("/challan-numbers")
    public ResponseEntity<JsonNode> getChallanNumbers(
            @RequestParam int examMonthYearId,
            @RequestParam String pin) {
        return ResponseEntity.ok(feeService.getChallanNumbers(examMonthYearId, pin));
    }

    @GetMapping("/challan-details")
    public ResponseEntity<Map<String, Object>> getChallanDetails(@RequestParam String chalanaNo) {
        return ResponseEntity.ok(feeService.getChallanDetails(chalanaNo));
    }
}