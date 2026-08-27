package com.polyconnect.integration.sbtet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.polyconnect.exception.SbtetServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SbtetClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;



    public SbtetClient(@Value("${polyconnect.sbtet.base-url:https://www.sbtet.telangana.gov.in/api/api}") String baseUrl) {
        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("User-Agent", "PolyConnect-Backend/1.0")
            .defaultHeader("Accept", "application/json")
            .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Fetches attendance report for PIN.
     * Endpoint returns double-encoded JSON with Table (summary), Table1 (calendar), Table2 (month lookup).
     */
    public Map<String, Object> getAttendanceReport(String pin) {
        try {
            String rawResponse = restClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/PreExamination/getAttendanceReport")
                    .queryParam("Pin", pin.trim())
                    .build())
                .retrieve()
                .body(String.class);

            if (rawResponse == null || rawResponse.isBlank()) {
                throw new SbtetServiceException("Empty response received from SBTET attendance service.");
            }

            JsonNode rootNode = parseDoubleEncodedJson(rawResponse);
            return processAttendanceJson(rootNode, pin);
        } catch (SbtetServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SbtetServiceException("SBTET attendance service is temporarily unavailable.");
        }
    }

    /**
     * Fetches Semester Results (stateless proxy).
     * Exact confirmed query params: ExamMonthYearId, ExamTypeId=5, Pin, SchemeId, SemYearId, StudentTypeId
     */
    public Map<String, Object> getSemesterResults(int examMonthYearId, String pin, int schemeId, int semYearId, int studentTypeId) {
        try {
            String rawResponse = restClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/Results/GetStudentWiseReport")
                    .queryParam("ExamMonthYearId", examMonthYearId)
                    .queryParam("ExamTypeId", 5)
                    .queryParam("Pin", pin.trim())
                    .queryParam("SchemeId", schemeId)
                    .queryParam("SemYearId", semYearId)
                    .queryParam("StudentTypeId", studentTypeId > 0 ? studentTypeId : 1)
                    .build())
                .retrieve()
                .body(String.class);

            if (rawResponse == null || rawResponse.isBlank()) {
                throw new SbtetServiceException("Empty response from SBTET semester results service.");
            }

//            JsonNode rootNode = parseDoubleEncodedJson(rawResponse);
//            return processSemesterResultsJson(rootNode);
            System.out.println("========== SBTET SEMESTER RAW RESPONSE ==========");
            System.out.println(rawResponse);
            System.out.println("=================================================");

            JsonNode rootNode = parseDoubleEncodedJson(rawResponse);

            System.out.println("========== SBTET PARSED RESPONSE ===============");
            System.out.println(rootNode.toPrettyString());
            System.out.println("=================================================");

            return processSemesterResultsJson(rootNode);
        } catch (SbtetServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SbtetServiceException("SBTET result service is temporarily unavailable.");
        }
    }

    /**
     * Fetches Mid Examination Results (stateless proxy).
     * Exact confirmed query params: ExamTypeId (1 or 2), Pin, SchemeId, SemYearId.
     * Note: NO ExamMonthYearId on this endpoint.
     */
    public Map<String, Object> getMidResults(int examTypeId, String pin, int schemeId, int semYearId) {
        try {
            String rawResponse = restClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/Results/GetC18MidStudentWiseReport")
                    .queryParam("ExamTypeId", examTypeId)
                    .queryParam("Pin", pin.trim())
                    .queryParam("SchemeId", schemeId)
                    .queryParam("SemYearId", semYearId)
                    .build())
                .retrieve()
                .body(String.class);

            if (rawResponse == null || rawResponse.isBlank()) {
                throw new SbtetServiceException("Empty response from SBTET mid results service.");
            }

            JsonNode rootNode = parseDoubleEncodedJson(rawResponse);
            return processMidResultsJson(rootNode);
        } catch (SbtetServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SbtetServiceException("SBTET mid result service is temporarily unavailable.");
        }
    }

    /**
     * Scheme discovery endpoint.
     */
    public JsonNode getSchemeDiscovery() {
        try {
            String rawResponse = restClient.get()
                .uri("/Results/GetSchemeDataForResults")
                .retrieve()
                .body(String.class);
            return parseDoubleEncodedJson(rawResponse);
        } catch (Exception ex) {
            throw new SbtetServiceException("SBTET discovery service is temporarily unavailable.");
        }
    }

    /**
     * Exam type discovery endpoint.
     */
    public JsonNode getExamTypeDiscovery() {
        try {
            String rawResponse = restClient.get()
                .uri("/Results/GetExamTypeForResults")
                .retrieve()
                .body(String.class);
            return parseDoubleEncodedJson(rawResponse);
        } catch (Exception ex) {
            throw new SbtetServiceException("SBTET discovery service is temporarily unavailable.");
        }
    }


    public JsonNode getActiveCirculars() {
        try {
            String rawResponse = restClient.get()
                    .uri("/AdminService/getCircularsActive")
                    .retrieve()
                    .body(String.class);
            if (rawResponse == null || rawResponse.isBlank()) {
                throw new SbtetServiceException("Empty response from SBTET circulars service.");
            }
            JsonNode rootNode = parseDoubleEncodedJson(rawResponse);
            return rootNode.path("Table");
        } catch (SbtetServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SbtetServiceException("SBTET circulars service is temporarily unavailable.");
        }
    }



    private JsonNode parseDoubleEncodedJson(String raw) throws Exception {
        JsonNode firstPass = objectMapper.readTree(raw);
        if (firstPass.isTextual()) {
            return objectMapper.readTree(firstPass.asText());
        }
        return firstPass;
    }

    private Map<String, Object> processAttendanceJson(JsonNode rootNode, String pin) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("pin", pin);

        JsonNode tableNode = rootNode.path("Table");
        JsonNode table1Node = rootNode.path("Table1"); // Daily status records
        JsonNode table2Node = rootNode.path("Table2"); // Month lookup

        System.out.println("========== Table1 (daily records) ==========");
        System.out.println(table1Node.toPrettyString());


        if (tableNode.isArray() && !tableNode.isEmpty()) {
            JsonNode summary = tableNode.get(0);
            int workingDays = summary.path("WorkingDays").asInt(0);
            int examsWorkingDays = summary.path("ExamsWorkingDays").asInt(workingDays);

            // Only these keys are confirmed from a real captured response: WorkingDays,
            // Percentage, TotalPercentage/ExamsPer, ExamsWorkingDays. A present/absent DAY
            // COUNT key was never captured — do not guess one (e.g. "PresentDays"/"AbsentDays"
            // are NOT confirmed). If you capture a real response and confirm the actual key,
            // add it here explicitly instead of falling back to a computed guess.
            double currentStandingPct = summary.path("Percentage").asDouble(0.0);
            double examEligibilityPct = summary.path("TotalPercentage").asDouble(
                summary.path("ExamsPer").asDouble(0.0)
            );

            BigDecimal currentStanding = BigDecimal.valueOf(currentStandingPct).setScale(2, RoundingMode.HALF_UP);
            BigDecimal examEligibility = BigDecimal.valueOf(examEligibilityPct).setScale(2, RoundingMode.HALF_UP);
            boolean detentionRisk = examEligibility.compareTo(new BigDecimal("75.00")) < 0;

            // presentDays/absentDays: SBTET's raw JSON key for a day COUNT (as opposed to the
            // confirmed percentage fields) was never captured in a real response, so it is
            // derived here from two fields that ARE confirmed (Percentage x WorkingDays) rather
            // than guessed from an invented key name. Treat this as an estimate for display
            // only — if you capture a real response and find the actual count field, read it
            // directly instead and remove this derivation.
            int presentDays = (int) Math.round(currentStandingPct / 100.0 * workingDays);
            int absentDays = Math.max(0, workingDays - presentDays);

            result.put("workingDays", workingDays);
            result.put("presentDays", presentDays);
            result.put("presentDaysIsEstimated", true);
            result.put("absentDays", absentDays);
            result.put("examsWorkingDays", examsWorkingDays);
            result.put("currentStandingPercentage", currentStanding);
            result.put("examEligibilityPercentage", examEligibility);
            result.put("isDetentionRisk", detentionRisk);
            result.put("summary", summary);
        }

        result.put("dailyRecords", table1Node);
        result.put("monthLookup", table2Node);
        return result;
    }
//
//    private Map<String, Object> processSemesterResultsJson(JsonNode rootNode) {
//        Map<String, Object> result = new HashMap<>();
//        result.put("success", true);
//
//        // StudentInfo[0]
//        JsonNode studentInfo = rootNode.path("StudentInfo");
//        if (studentInfo.isArray() && !studentInfo.isEmpty()) {
//            result.put("studentInfo", studentInfo.get(0));
//        }
//
//        // StudentWiseReport[]
//        result.put("studentWiseReport", rootNode.path("StudentWiseReport"));
//
//        // StudentSGPACGPAInfo[0]
//        JsonNode sgpaCgpa = rootNode.path("StudentSGPACGPAInfo");
//        if (sgpaCgpa.isArray() && !sgpaCgpa.isEmpty()) {
//            result.put("studentSGPACGPAInfo", sgpaCgpa.get(0));
//        }
//
//        // StudentSubjectTotal[0]
//        JsonNode subjectTotal = rootNode.path("StudentSubjectTotal");
//        if (subjectTotal.isArray() && !subjectTotal.isEmpty()) {
//            result.put("studentSubjectTotal", subjectTotal.get(0));
//        }
//
//        return result;
//    }
private Map<String, Object> processSemesterResultsJson(JsonNode rootNode) {

    Map<String, Object> result = new HashMap<>();
    result.put("success", true);

    // SBTET returns:
    // [
    //   {
    //      "studentWiseReport": [...],
    //      "studentInfo": [...],
    //      ...
    //   }
    // ]

    JsonNode targetNode;

    // Unwrap outer array
    if (rootNode.isArray() && !rootNode.isEmpty()) {
        targetNode = rootNode.get(0);
    } else {
        targetNode = rootNode;
    }

    // -----------------------------
    // Student Info
    // -----------------------------
    JsonNode studentInfo = targetNode.path("studentInfo");

    if (studentInfo.isArray() && !studentInfo.isEmpty()) {
        result.put("studentInfo", studentInfo.get(0));
    } else {
        result.put("studentInfo", studentInfo);
    }

    // -----------------------------
    // Subject-wise Result
    // -----------------------------
    JsonNode studentWiseReport =
            targetNode.path("studentWiseReport");

    result.put("studentWiseReport", studentWiseReport);

    // -----------------------------
    // SGPA / CGPA
    // -----------------------------
    JsonNode sgpaCgpa =
            targetNode.path("studentSGPACGPAInfo");

    if (sgpaCgpa.isArray() && !sgpaCgpa.isEmpty()) {
        result.put("studentSGPACGPAInfo", sgpaCgpa.get(0));
    } else {
        result.put("studentSGPACGPAInfo", sgpaCgpa);
    }

    // -----------------------------
    // Subject Total
    // -----------------------------
    JsonNode subjectTotal =
            targetNode.path("studentSubjectTotal");

    if (subjectTotal.isArray() && !subjectTotal.isEmpty()) {
        result.put("studentSubjectTotal", subjectTotal.get(0));
    } else {
        result.put("studentSubjectTotal", subjectTotal);
    }

    // -----------------------------
    // Activities
    // -----------------------------
    JsonNode activities =
            targetNode.path("studentActvities");

    result.put("studentActvities", activities);

    // -----------------------------
    // Cumulative Grade
    // -----------------------------
    JsonNode cumulativeGrade =
            targetNode.path("CumulativeGradeInfo");

    result.put("CumulativeGradeInfo", cumulativeGrade);

    // -----------------------------
    // Branch Subject Grade Info
    // -----------------------------
    JsonNode branchSubjectGradeInfo =
            targetNode.path("branchSubjectGradeInfo");

    result.put("branchSubjectGradeInfo", branchSubjectGradeInfo);

    // -----------------------------
    // C18 Supplementary
    // -----------------------------
    result.put(
            "C18suppleGradeInfo",
            targetNode.path("C18suppleGradeInfo")
    );

    // -----------------------------
    // C18 Semester Info
    // -----------------------------
    result.put(
            "C18SemInfo",
            targetNode.path("C18SemInfo")
    );

    return result;
}
//
//    private Map<String, Object> processMidResultsJson(JsonNode rootNode) {
//        Map<String, Object> result = new HashMap<>();
//        result.put("success", true);
//        result.put("studentInfo", rootNode.path("StudentInfo"));
//        result.put("midReport", rootNode.path("StudentWiseReport").isMissingNode() ? rootNode.path("Table") : rootNode.path("StudentWiseReport"));
//        return result;
//    }
private Map<String, Object> processMidResultsJson(JsonNode rootNode) {
    Map<String, Object> result = new HashMap<>();
    result.put("success", true);

    // If SBTET wraps the response in a root JSON array [...], unwrap element 0
    JsonNode targetNode = rootNode.isArray() && !rootNode.isEmpty()
            ? rootNode.get(0)
            : rootNode;

    // Extract studentInfo (array of 1 element in SBTET's response)
    JsonNode studentInfoNode = targetNode.path("studentInfo");
    if (studentInfoNode.isArray() && !studentInfoNode.isEmpty()) {
        result.put("studentInfo", studentInfoNode.get(0));
    } else {
        result.put("studentInfo", studentInfoNode);
    }

    // Extract studentWiseReport (array of marks)
    result.put("studentWiseReport", targetNode.path("studentWiseReport"));

    return result;
}


    /**
     * Fetches Consolidated Results (stateless proxy).
     * Endpoint: /Results/GetConsolidatedResults?Pin={pin}
     */
//    public Map<String, Object> getConsolidatedResults(String pin) {
//        try {
//            String rawResponse = restClient.get()
//                    .uri(uriBuilder -> uriBuilder
//                            .path("/Results/GetConsolidatedResults")
//                            .queryParam("Pin", pin.trim())
//                            .build())
//                    .retrieve()
//                    .body(String.class);
//
//            if (rawResponse == null || rawResponse.isBlank()) {
//                throw new SbtetServiceException("Empty response from SBTET consolidated results service.");
//            }
//
//            JsonNode rootNode = parseDoubleEncodedJson(rawResponse);
//            return processConsolidatedResultsJson(rootNode);
//        } catch (SbtetServiceException ex) {
//            throw ex;
//        } catch (Exception ex) {
//            throw new SbtetServiceException("SBTET consolidated result service is temporarily unavailable.");
//        }
//    }
    public Map<String, Object> getConsolidatedResults(String pin) {
        try {
            String rawResponse = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            // Combines with base-url to form:
                            // https://www.sbtet.telangana.gov.in/api/api/Results/GetConsolidatedResults?Pin=...
                            .path("/Results/GetConsolidatedResults")
                            .queryParam("Pin", pin.trim())
                            .build())
                    .retrieve()
                    .body(String.class);

            System.out.println("========== SBTET RAW RESPONSE ==========");
            System.out.println(rawResponse);
            System.out.println("=========================================");

            if (rawResponse == null || rawResponse.isBlank()) {
                throw new SbtetServiceException("Empty response received from SBTET.");
            }

            JsonNode rootNode = parseDoubleEncodedJson(rawResponse);
            return processConsolidatedResultsJson(rootNode);

        } catch (SbtetServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new SbtetServiceException("SBTET service error: " + ex.getMessage());
        }
    }
    public Map<String, Object> processConsolidatedResultsJson(JsonNode rootNode) {
        Map<String, Object> result = new HashMap<>();

        // 1. Extract Student Info from Table
        JsonNode table = rootNode.path("Table");
        if (table.isArray() && table.size() > 0) {
            JsonNode student = table.get(0);
            Map<String, String> studentInfo = new HashMap<>();
            studentInfo.put("name", student.path("StudentName").asText(""));
            studentInfo.put("pin", student.path("Pin").asText(""));
            studentInfo.put("branch", student.path("BranchCode").asText(""));
            studentInfo.put("scheme", student.path("Scheme").asText(""));
            studentInfo.put("centerCode", student.path("CenterCode").asText(""));
            studentInfo.put("centerName", student.path("CenterName").asText(""));
            result.put("studentInfo", studentInfo);
        }

        // 2. Extract CGPA Info from Table1
        JsonNode table1 = rootNode.path("Table1");
        if (table1.isArray() && table1.size() > 0) {
            JsonNode cgpaNode = table1.get(0);
            Map<String, Object> cgpaInfo = new HashMap<>();
            cgpaInfo.put("cgpa", cgpaNode.path("CGPA").asDouble(0.0));
            cgpaInfo.put("totalCredits", cgpaNode.path("CgpaTotalCredits").asDouble(0.0));
            cgpaInfo.put("creditsGained", cgpaNode.path("CreditsGained").asDouble(0.0));
            cgpaInfo.put("totalGainedPoints", cgpaNode.path("CgpaTotalGained").asDouble(0.0));
            result.put("cgpaInfo", cgpaInfo);
        }

        // 3. Extract Subject Marks List from Table2
        // 3. Extract Subject Marks List from Table2
        JsonNode table2 = rootNode.path("Table2");
        List<Map<String, Object>> subjectList = new ArrayList<>();
        if (table2.isArray()) {
            for (JsonNode subject : table2) {
                Map<String, Object> subMap = new HashMap<>();
                subMap.put("subjectCode", subject.path("Subject_Code").asText(""));
                subMap.put("subjectName", subject.path("SubjectName").asText(""));
                subMap.put("semester", subject.path("Semester").asText(""));
                subMap.put("semId", subject.path("SemId").asInt(0));
                subMap.put("grade", subject.path("HybridGrade").asText(""));
                subMap.put("gradePoint", subject.path("GradePoint").asInt(0));
                subMap.put("subjectTotal", subject.path("SubjectTotal").asInt(0));
                subMap.put("result", subject.path("ExamStatus").asText(""));
                subMap.put("creditsGained", subject.path("CreditsGained").asDouble(0.0));
                subMap.put("examMonthYear", subject.path("ExamMonthYear").asText(""));

                // Previously missing — these were dropped even though SBTET sends them:
                subMap.put("maxCredits", subject.path("MaxCredits").asDouble(0.0));
                subMap.put("mid1Marks", subject.path("Mid1Marks").asText("0"));
                subMap.put("mid2Marks", subject.path("Mid2Marks").asText("0"));
                subMap.put("internalMarks", subject.path("InternalMarks").asText("0"));
                subMap.put("endExamMarks", subject.path("EndExamMarks").asText("0"));
                subMap.put("totalGradePoints", subject.path("TotalGradePoints").asDouble(0.0));
                subMap.put("wholeOrSupply", subject.path("WholeOrSupply").asText("R"));
                subMap.put("subjectOrder", subject.path("SubjectOrder").asInt(0));

                subjectList.add(subMap);
            }
        }
        result.put("reportList", subjectList);
        // 4. Extract Semester Breakdown from Table3
        JsonNode table3 = rootNode.path("Table3");
        List<Map<String, Object>> semList = new ArrayList<>();
        if (table3.isArray()) {
            for (JsonNode sem : table3) {
                Map<String, Object> semMap = new HashMap<>();
                semMap.put("semId", sem.path("SemId").asInt(0));
                semMap.put("semester", sem.path("Semester").asText(""));
                semMap.put("credits", sem.path("Credits").asDouble(0.0));
                semMap.put("totalGradePoints", sem.path("TotalGradePoints").asDouble(0.0));
                semMap.put("sgpa", sem.path("SGPA").isNull() ? "N/A" : sem.path("SGPA").asText());
                semList.add(semMap);
            }
        }
        result.put("semesterBreakdown", semList);

        result.put("success", true);
        return result;
    }

    private String getFirstValidString(JsonNode node, String... keys) {
        if (node == null || node.isMissingNode()) return "-";
        for (String key : keys) {
            if (node.hasNonNull(key) && !node.get(key).asText().isBlank()) {
                return node.get(key).asText();
            }
        }
        return "-";
    }


    /**
     * Fetches Exam Month Year for Hallticket / Fee Payment.
     * @param studentTypeId 1 for Regular, 2 for Backlog
     */
    public JsonNode getExamMonthYear(int studentTypeId) {
        try {
            String rawResponse = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/PreExamination/GetExamMonthYearForHallticketandFeepayment")
                            .queryParam("DataTypeId", 1)
                            .queryParam("StudentTypeId", studentTypeId > 0 ? studentTypeId : 1)
                            .build())
                    .retrieve()
                    .body(String.class);

            if (rawResponse == null || rawResponse.isBlank()) {
                throw new SbtetServiceException("Empty response received from SBTET exam month/year service.");
            }

            return parseDoubleEncodedJson(rawResponse);
        } catch (SbtetServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new SbtetServiceException("SBTET exam month/year service is temporarily unavailable: " + ex.getMessage());
        }
    }

    /**
     * Fetches Challan Numbers for given ExamMonthYearID and PIN.
     */
    public JsonNode getChallanNumbers(int examMonthYearId, String pin) {
        try {
            String rawResponse = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/PreExamination/GetChallanNumbers")
                            .queryParam("ExamMonthYearID", examMonthYearId)
                            .queryParam("PIN", pin.trim())
                            .queryParam("PaymentSubTypeID", 1)
                            .queryParam("PaymentTypeID", 1)
                            .build())
                    .retrieve()
                    .body(String.class);

            if (rawResponse == null || rawResponse.isBlank()) {
                throw new SbtetServiceException("Empty response received from SBTET challan number service.");
            }

            return parseDoubleEncodedJson(rawResponse);
        } catch (SbtetServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SbtetServiceException("SBTET challan number service is temporarily unavailable.");
        }
    }

    /**
     * Finds full transaction and receipt details by Challan Number.
     */
    public Map<String, Object> findChallanDetails(String chalanaNo) {
        try {
            String rawResponse = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/PreExamination/FindchalanaNo")
                            .queryParam("chalanaNo", chalanaNo.trim())
                            .build())
                    .retrieve()
                    .body(String.class);

            if (rawResponse == null || rawResponse.isBlank()) {
                throw new SbtetServiceException("Empty response received from SBTET challan detail service.");
            }

            JsonNode rootNode = parseDoubleEncodedJson(rawResponse);
            return processChallanDetailsJson(rootNode);
        } catch (SbtetServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SbtetServiceException("SBTET challan detail service is temporarily unavailable.");
        }
    }

    private Map<String, Object> processChallanDetailsJson(JsonNode rootNode) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);

        JsonNode tableNode = rootNode.path("Table");
        JsonNode table1Node = rootNode.path("Table1");

        if (tableNode.isArray() && !tableNode.isEmpty()) {
            JsonNode txn = tableNode.get(0);
            Map<String, Object> txnDetails = new HashMap<>();

            txnDetails.put("referenceNumber", txn.path("subscriberid").asText(""));
            txnDetails.put("bankTxnNumber", txn.path("bankrefno").asText("") + "-" + txn.path("addtninfo6").asText(""));
            txnDetails.put("paymentStatus", txn.path("errordesc").asText(""));
            txnDetails.put("pin", txn.path("addtninfo3").asText(""));
            txnDetails.put("feeType", txn.path("addtninfo5").asText(""));
            txnDetails.put("feeAmount", txn.path("txnamt").asText("0.00"));
            txnDetails.put("date", txn.path("txndate").asText(""));
            txnDetails.put("txnRefNo", txn.path("txnrefno").asText(""));

            result.put("receiptDetails", txnDetails);
        }

        if (table1Node.isArray() && !table1Node.isEmpty()) {
            result.put("pinDetails", table1Node.get(0));
        }

        return result;
    }

// ============================================================
// ADD THESE THREE METHODS INTO YOUR EXISTING SbtetClient CLASS
// (same class that already has getExamMonthYear, getChallanNumbers, etc.)
// ============================================================
public JsonNode getAllCourses() {
    try {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://www.sbtet.telangana.gov.in/api/api/AdminService/GetAllCourses";
        String rawResponse = restTemplate.getForObject(url, String.class);
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new SbtetServiceException("Empty response from SBTET course service.");
        }
        return parseDoubleEncodedJson(rawResponse);
    } catch (SbtetServiceException ex) {
        throw ex;
    } catch (Exception ex) {
        throw new SbtetServiceException("SBTET course service is temporarily unavailable.");
    }
}

    public JsonNode getCollegeWiseCourses(String branchCode) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://www.sbtet.telangana.gov.in/api/api/AdminService/GetCollegeWiseCourses?BranchCode=" + branchCode.trim();
            String rawResponse = restTemplate.getForObject(url, String.class);
            if (rawResponse == null || rawResponse.isBlank()) {
                throw new SbtetServiceException("Empty response from SBTET college-wise course service.");
            }
            return parseDoubleEncodedJson(rawResponse);
        } catch (SbtetServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SbtetServiceException("SBTET college-wise course service is temporarily unavailable.");
        }
    }

    public String getCurrentAcademicYear() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://www.sbtet.telangana.gov.in/api/api/AdminService/GetCurrentAcademicYear";
            String rawResponse = restTemplate.getForObject(url, String.class);
            if (rawResponse != null && !rawResponse.isBlank()) {
                JsonNode rootNode = parseDoubleEncodedJson(rawResponse);
                JsonNode tableNode = rootNode.path("Table");
                if (tableNode.isArray() && !tableNode.isEmpty()) {
                    return tableNode.get(0).path("AcademicYear").asText("2026-27");
                }
            }
        } catch (Exception ex) {
            // Fallback default if live API fails
        }
        return "2026-27";
    }
    // ============================================================
// ADD THIS METHOD INTO YOUR EXISTING SbtetClient CLASS
// ============================================================

    /**
     * Fetches the college summary breakdown by type (Government / Private Aided / Private Unaided).
     * Endpoint: GET /AdminService/GetCollegeSummary
     * Returns: {"Table":[{"TypeName":"Government","CollegeCount":59,"TotalIntake":17130}, ...]}
     */
    public JsonNode getCollegeSummary() {
        try {
            String rawResponse = restClient.get()
                    .uri("/AdminService/GetCollegeSummary")
                    .retrieve()
                    .body(String.class);

            if (rawResponse == null || rawResponse.isBlank()) {
                throw new SbtetServiceException("Empty response from SBTET GetCollegeSummary service.");
            }

            JsonNode rootNode = parseDoubleEncodedJson(rawResponse);
            return rootNode.path("Table");
        } catch (SbtetServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SbtetServiceException("SBTET GetCollegeSummary service is temporarily unavailable.");
        }
    }

}
