package com.polyconnect.service;

import com.polyconnect.entity.Branch;
import com.polyconnect.entity.College;
import com.polyconnect.entity.CollegeBranch;
import com.polyconnect.entity.Community;
import com.polyconnect.exception.InvalidPinException;
import com.polyconnect.exception.ResourceNotFoundException;
import com.polyconnect.repository.BranchRepository;
import com.polyconnect.repository.CollegeBranchRepository;
import com.polyconnect.repository.CollegeRepository;
import com.polyconnect.repository.CommunityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CollegeService {

    private final CollegeRepository collegeRepository;
    private final BranchRepository branchRepository;
    private final CollegeBranchRepository collegeBranchRepository;
    private final CommunityRepository communityRepository;

    public CollegeService(
        CollegeRepository collegeRepository,
        BranchRepository branchRepository,
        CollegeBranchRepository collegeBranchRepository,
        CommunityRepository communityRepository
    ) {
        this.collegeRepository = collegeRepository;
        this.branchRepository = branchRepository;
        this.collegeBranchRepository = collegeBranchRepository;
        this.communityRepository = communityRepository;
    }

    public List<College> getAllActiveColleges() {
        List<College> colleges = collegeRepository.findByActiveTrue();
        for (College college : colleges) {
            List<CollegeBranch> cbs = collegeBranchRepository.findByCollegeIdAndActiveTrue(college.getId());
            college.setBranchCodes(cbs.stream().map(cb -> cb.getBranch().getCode()).toList());
        }
        return colleges;
    }

    public List<Branch> getAllActiveBranches() {
        return branchRepository.findByActiveTrue();
    }

    @Transactional
    public College createCollege(College collegeInput, List<String> offeredBranchCodes) {
        if (collegeRepository.existsByCode(collegeInput.getCode())) {
            throw new InvalidPinException("College with code '" + collegeInput.getCode() + "' already exists.");
        }

        College college = collegeRepository.save(collegeInput);

        // Associate offered branches
        if (offeredBranchCodes != null && !offeredBranchCodes.isEmpty()) {
            for (String branchCode : offeredBranchCodes) {
                branchRepository.findByCode(branchCode).ifPresent(branch -> {
                    CollegeBranch cb = new CollegeBranch(college, branch, 60);
                    collegeBranchRepository.save(cb);
                });
            }
        }

        // Auto-create college community board
        String slug = "college-" + college.getCode().toLowerCase();
        if (communityRepository.findBySlug(slug).isEmpty()) {
            Community community = new Community();
            community.setName(college.getName() + " Community");
            community.setSlug(slug);
            community.setDescription("Official student & staff forum for " + college.getName());
            community.setCommunityType("COLLEGE");
            community.setCollege(college);
            communityRepository.save(community);
        }

        college.setBranchCodes(offeredBranchCodes);
        return college;
    }
}
