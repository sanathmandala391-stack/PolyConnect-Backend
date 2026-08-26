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
        doubt.setSubjectCode(doubtInput.getSubjectCode());
        doubt.setSubjectName(doubtInput.getSubjectName());
        doubt.setTopic(doubtInput.getTopic());
        doubt.setQuestionText(doubtInput.getQuestionText());
        doubt.setImageUrl(doubtInput.getImageUrl());

        // Invoke AI Doubt Solver with SBTET syllabus prompt
        String aiSolution = aiDoubtSolverClient.solveDoubt(
            doubtInput.getSubjectCode(),
            doubtInput.getSubjectName(),
            doubtInput.getTopic(),
            doubtInput.getQuestionText(),
            null
        );

        doubt.setAiSolution(aiSolution);
        doubt.setAiStatus("RESOLVED");

        Doubt saved = doubtRepository.save(doubt);

        // Increase student reputation activity points
        reputationRepository.findById(student.getId()).ifPresent(rep -> {
            rep.setPoints(rep.getPoints() + 5);
            reputationRepository.save(rep);
        });

        return saved;
    }
}
