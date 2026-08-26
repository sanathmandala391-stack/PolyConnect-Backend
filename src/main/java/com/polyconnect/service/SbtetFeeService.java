package com.polyconnect.service;



import com.fasterxml.jackson.databind.JsonNode;
import com.polyconnect.integration.sbtet.SbtetClient;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SbtetFeeService {

    private final SbtetClient sbtetClient;

    public SbtetFeeService(SbtetClient sbtetClient) {
        this.sbtetClient = sbtetClient;
    }

    public JsonNode getExamMonthYear(String feeType) {
        // Regular = 1, Backlog = 2
        int studentTypeId = "Backlog".equalsIgnoreCase(feeType) ? 2 : 1;
        return sbtetClient.getExamMonthYear(studentTypeId);
    }

    public JsonNode getChallanNumbers(int examMonthYearId, String pin) {
        return sbtetClient.getChallanNumbers(examMonthYearId, pin);
    }

    public Map<String, Object> getChallanDetails(String chalanaNo) {
        return sbtetClient.findChallanDetails(chalanaNo);
    }
}
