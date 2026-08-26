    package com.polyconnect.controller;

    import com.fasterxml.jackson.databind.JsonNode;
    import com.polyconnect.entity.SbtetExamMonthYear;
    import com.polyconnect.entity.SbtetExamType;
    import com.polyconnect.entity.SbtetScheme;
    import com.polyconnect.entity.SbtetSemester;
    import com.polyconnect.service.SbtetCircularService;
    import com.polyconnect.service.SbtetResultService;
    import com.polyconnect.service.SyncStatusService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    @RestController
    @RequestMapping("/api/sbtet")
    public class SbtetController {

        private final SbtetResultService resultService;
        private final SbtetCircularService circularService;

        @Autowired
        private SyncStatusService syncStatusService;
        public SbtetController(SbtetResultService resultService, SbtetCircularService circularService) {
            this.resultService = resultService;
            this.circularService= circularService;
        }


        //for sync//

        @GetMapping("/last-sync")
        public Map<String, Object> getLastSync() {
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", syncStatusService.getLastSyncTimestamp());
            return response;
        }
        /**
         * Stateless live proxy for semester results.
         */
        @GetMapping("/circulars")
        public ResponseEntity<JsonNode> getCirculars() {
            return ResponseEntity.ok(circularService.getActiveCirculars());
        }


        @GetMapping("/results/semester")
        public ResponseEntity<Map<String, Object>> getSemesterResults(
            @RequestParam int examMonthYearId,
            @RequestParam String pin,
            @RequestParam int schemeId,
            @RequestParam int semYearId,
            @RequestParam(defaultValue = "1") int studentTypeId
        ) {
            return ResponseEntity.ok(resultService.fetchSemesterResults(examMonthYearId, pin, schemeId, semYearId, studentTypeId));
        }



        @GetMapping("/consolidated-results")
        public ResponseEntity<Map<String, Object>> getConsolidatedResults(@RequestParam("pin") String pin) {
            Map<String, Object> result = resultService.fetchConsolidatedResults(pin);
            return ResponseEntity.ok(result);
        }



        /**
         * Stateless live proxy for mid results.
         */
        @GetMapping("/results/mid")
        public ResponseEntity<Map<String, Object>> getMidResults(
            @RequestParam int examTypeId,
            @RequestParam String pin,
            @RequestParam int schemeId,
            @RequestParam int semYearId
        ) {
            return ResponseEntity.ok(resultService.fetchMidResults(examTypeId, pin, schemeId, semYearId));
        }

        @GetMapping("/discovery/schemes")
        public ResponseEntity<List<SbtetScheme>> getSchemes() {
            return ResponseEntity.ok(resultService.getActiveSchemes());
        }

        @GetMapping("/discovery/exam-types")
        public ResponseEntity<List<SbtetExamType>> getExamTypes() {
            return ResponseEntity.ok(resultService.getActiveExamTypes());
        }

        @GetMapping("/discovery/semesters")
        public ResponseEntity<List<SbtetSemester>> getSemesters() {
            return ResponseEntity.ok(resultService.getActiveSemesters());
        }

        @GetMapping("/discovery/exam-month-years")
        public ResponseEntity<List<SbtetExamMonthYear>> getExamMonthYears() {
            return ResponseEntity.ok(resultService.getExamMonthYears());
        }

        @GetMapping("/discovery/live-schemes")
        public ResponseEntity<JsonNode> getLiveSchemes() {
            return ResponseEntity.ok(resultService.getLiveSchemeDiscovery());
        }
    }
