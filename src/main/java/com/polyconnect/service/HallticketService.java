//package com.polyconnect.service;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//@Service
//public class HallticketService {
//
//    private final RestTemplate restTemplate;
//
//    private static final String EXAM_MONTHS_URL =
//            "https://www.sbtet.telangana.gov.in/api/api/PreExamination/GetExamMonthYearForHallticketandFeepayment?DataTypeId=2&StudentTypeId={studentTypeId}";
//
//    private static final String HALLTICKET_URL =
//            "https://www.sbtet.telangana.gov.in/api/api/PreExamination/GetRegularHallticket?DateOfBirth={dob}&EMYR={emyr}&Pin={pin}&StudentTypeId={studentTypeId}";
//
//    public HallticketService(RestTemplate restTemplate) {
//        this.restTemplate = restTemplate;
//    }
//
//    public JsonNode getExamMonths(int studentTypeId) {
//        return restTemplate.getForObject(EXAM_MONTHS_URL, JsonNode.class, studentTypeId);
//    }
//
//    public JsonNode getHallticket(String pin, String dob, int emyr, int studentTypeId) {
//        return restTemplate.getForObject(
//                HALLTICKET_URL, JsonNode.class, dob, emyr, pin, studentTypeId
//        );
//    }
//}


















package com.polyconnect.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HallticketService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String EXAM_MONTHS_URL =
            "https://www.sbtet.telangana.gov.in/api/api/PreExamination/GetExamMonthYearForHallticketandFeepayment?DataTypeId=2&StudentTypeId={studentTypeId}";

    private static final String HALLTICKET_URL =
            "https://www.sbtet.telangana.gov.in/api/api/PreExamination/GetRegularHallticket?DateOfBirth={dob}&EMYR={emyr}&Pin={pin}&StudentTypeId={studentTypeId}";

    public HallticketService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public JsonNode getExamMonths(int studentTypeId) {
        String rawResponse = restTemplate.getForObject(EXAM_MONTHS_URL, String.class, studentTypeId);
        return unwrapDoubleEncodedJson(rawResponse);
    }

    public JsonNode getHallticket(String pin, String dob, int emyr, int studentTypeId) {
        String rawResponse = restTemplate.getForObject(
                HALLTICKET_URL, String.class, dob, emyr, pin, studentTypeId
        );
        return unwrapDoubleEncodedJson(rawResponse);
    }

    /**
     * SBTET's API sometimes returns the real JSON wrapped as a JSON string literal
     * (i.e. double-encoded). This parses it once to unwrap the outer layer, and
     * again if needed to get the actual JSON object.
     */
    private JsonNode unwrapDoubleEncodedJson(String rawResponse) {
        try {
            JsonNode firstPass = objectMapper.readTree(rawResponse);

            if (firstPass.isTextual()) {
                // Double-encoded: parse the inner string to get the real object
                return objectMapper.readTree(firstPass.asText());
            }

            // Already a proper object — return as-is
            return firstPass;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse SBTET response", e);
        }
    }
}