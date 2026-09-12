package com.polyconnect.service;

import com.polyconnect.entity.Doubt;
import com.polyconnect.entity.Reputation;
import com.polyconnect.entity.User;
import com.polyconnect.exception.ResourceNotFoundException;
import com.polyconnect.integration.ai.AiDoubtSolverClient;
import com.polyconnect.repository.DoubtRepository;
import com.polyconnect.repository.ReputationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

@Service
public class DoubtService {

    private final DoubtRepository doubtRepository;
    private final AiDoubtSolverClient aiDoubtSolverClient;
    private final ReputationRepository reputationRepository;

    public DoubtService(
        DoubtRepository doubtRepository,
        AiDoubtSolverClient aiDoubtSolverClient,
        ReputationRepository reputationRepository
    ) {
        this.doubtRepository = doubtRepository;
        this.aiDoubtSolverClient = aiDoubtSolverClient;
        this.reputationRepository = reputationRepository;
    }

    public List<Doubt> getStudentDoubts(Long studentId) {
        return doubtRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    @Transactional
    public void deleteDoubt(Long doubtId, Long studentId) {
        Doubt doubt = doubtRepository.findById(doubtId)
                .orElseThrow(() -> new ResourceNotFoundException("Doubt not found: " + doubtId));

        if (!doubt.getStudent().getId().equals(studentId)) {
            throw new AccessDeniedException("You do not have permission to delete this doubt.");
        }

        doubtRepository.delete(doubt);
    }

    @Transactional
    public Doubt askDoubt(Doubt doubtInput, User student) {
        Doubt doubt = new Doubt();
        doubt.setStudent(student);

        String subjectCode = (doubtInput.getSubjectCode() != null && !doubtInput.getSubjectCode().isBlank())
                ? doubtInput.getSubjectCode()
                : "DIPLOMA";
        String subjectName = (doubtInput.getSubjectName() != null && !doubtInput.getSubjectName().isBlank())
                ? doubtInput.getSubjectName()
                : "Polytechnic Subject";
        String topic = (doubtInput.getTopic() != null && !doubtInput.getTopic().isBlank())
                ? doubtInput.getTopic()
                : "Academic Doubt";

        doubt.setSubjectCode(subjectCode);
        doubt.setSubjectName(subjectName);
        doubt.setTopic(topic);
        doubt.setQuestionText(doubtInput.getQuestionText() != null ? doubtInput.getQuestionText() : "");

        // Handle image / attachment preview safely within varchar limits
        if (doubtInput.getImageUrl() != null && !doubtInput.getImageUrl().isBlank()) {
            String img = doubtInput.getImageUrl();
            if (img.startsWith("data:") && img.length() > 500) {
                doubt.setImageUrl("[Attachment - " + (img.length() / 1024) + " KB]");
            } else if (img.length() > 500) {
                doubt.setImageUrl(img.substring(0, 495) + "...");
            } else {
                doubt.setImageUrl(img);
            }
        }

        // Check if a valid AI solution is already provided
        String aiSolution = doubtInput.getAiSolution();
        if (aiSolution == null || aiSolution.isBlank() || aiSolution.startsWith("### SBTET AI Academic Mentor\n\nHere is guidance")) {
            // Invoke backend AI Doubt Solver with SBTET syllabus prompt
            aiSolution = aiDoubtSolverClient.solveDoubt(
                subjectCode,
                subjectName,
                topic,
                doubt.getQuestionText(),
                doubtInput.getImageUrl()
            );
        }

        doubt.setAiSolution(aiSolution);
        doubt.setAiStatus("RESOLVED");

        Doubt saved = doubtRepository.save(doubt);

        // Increase student reputation activity points safely
        try {
            reputationRepository.findById(student.getId()).ifPresent(rep -> {
                rep.setPoints(rep.getPoints() + 5);
                reputationRepository.save(rep);
            });
        } catch (Exception ignore) {}

        return saved;
    }
}
