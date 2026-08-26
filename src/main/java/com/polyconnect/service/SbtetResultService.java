package com.polyconnect.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.polyconnect.entity.SbtetExamMonthYear;
import com.polyconnect.entity.SbtetExamType;
import com.polyconnect.entity.SbtetScheme;
import com.polyconnect.entity.SbtetSemester;
import com.polyconnect.integration.sbtet.SbtetClient;
import com.polyconnect.repository.SbtetExamMonthYearRepository;
import com.polyconnect.repository.SbtetExamTypeRepository;
import com.polyconnect.repository.SbtetSchemeRepository;
import com.polyconnect.repository.SbtetSemesterRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SbtetResultService {

    private final SbtetClient sbtetClient;
    private final SbtetSchemeRepository schemeRepository;
    private final SbtetExamTypeRepository examTypeRepository;
    private final SbtetSemesterRepository semesterRepository;
    private final SbtetExamMonthYearRepository examMonthYearRepository;

    public SbtetResultService(
        SbtetClient sbtetClient,
        SbtetSchemeRepository schemeRepository,
        SbtetExamTypeRepository examTypeRepository,
        SbtetSemesterRepository semesterRepository,
        SbtetExamMonthYearRepository examMonthYearRepository
    ) {
        this.sbtetClient = sbtetClient;
        this.schemeRepository = schemeRepository;
        this.examTypeRepository = examTypeRepository;
        this.semesterRepository = semesterRepository;
        this.examMonthYearRepository = examMonthYearRepository;
    }

    /**
     * Stateless proxy call for Semester Results.
     */
    public Map<String, Object> fetchSemesterResults(int examMonthYearId, String pin, int schemeId, int semYearId, int studentTypeId) {
        return sbtetClient.getSemesterResults(examMonthYearId, pin, schemeId, semYearId, studentTypeId);
    }


    /**
     * Stateless proxy call for Consolidated Results.
     */
    public Map<String, Object> fetchConsolidatedResults(String pin) {
        return sbtetClient.getConsolidatedResults(pin);
    }

    /**
     * Stateless proxy call for Mid Results.
     */
    public Map<String, Object> fetchMidResults(int examTypeId, String pin, int schemeId, int semYearId) {
        return sbtetClient.getMidResults(examTypeId, pin, schemeId, semYearId);
    }

    public List<SbtetScheme> getActiveSchemes() {
        return schemeRepository.findByActiveTrue();
    }

    public List<SbtetExamType> getActiveExamTypes() {
        return examTypeRepository.findByActiveTrue();
    }

    public List<SbtetSemester> getActiveSemesters() {
        return semesterRepository.findByActiveTrueOrderBySequenceIdAsc();
    }

    public List<SbtetExamMonthYear> getExamMonthYears() {
        return examMonthYearRepository.findByActiveTrueOrderBySbtetIdDesc();
    }

    public JsonNode getLiveSchemeDiscovery() {
        return sbtetClient.getSchemeDiscovery();
    }

    public JsonNode getLiveExamTypeDiscovery() {
        return sbtetClient.getExamTypeDiscovery();
    }
}
