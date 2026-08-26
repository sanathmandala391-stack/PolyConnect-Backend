package com.polyconnect.service;

import com.polyconnect.entity.*;
import com.polyconnect.exception.InvalidPinException;
import com.polyconnect.exception.ResourceNotFoundException;
import com.polyconnect.exception.UnauthorizedException;
import com.polyconnect.repository.*;
import com.polyconnect.security.JwtTokenProvider;
import com.polyconnect.security.UserPrincipal;
import com.polyconnect.validation.PinValidator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final HodRepository hodRepository;
    private final CollegeRepository collegeRepository;
    private final BranchRepository branchRepository;
    private final StudentApprovalRepository studentApprovalRepository;
    private final HodApprovalRepository hodApprovalRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final PinValidator pinValidator;

    public AuthService(
        UserRepository userRepository,
        StudentRepository studentRepository,
        HodRepository hodRepository,
        CollegeRepository collegeRepository,
        BranchRepository branchRepository,
        StudentApprovalRepository studentApprovalRepository,
        HodApprovalRepository hodApprovalRepository,
        PasswordEncoder passwordEncoder,
        JwtTokenProvider tokenProvider,
        PinValidator pinValidator
    ) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.hodRepository = hodRepository;
        this.collegeRepository = collegeRepository;
        this.branchRepository = branchRepository;
        this.studentApprovalRepository = studentApprovalRepository;
        this.hodApprovalRepository = hodApprovalRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.pinValidator = pinValidator;
    }

    @Transactional
    public Student registerStudent(Student studentInput, String collegeCode, String branchCode) {
        College college = collegeRepository.findByCode(collegeCode)
            .orElseThrow(() -> new ResourceNotFoundException("College with code '" + collegeCode + "' not found."));

        Branch branch = branchRepository.findByCode(branchCode)
            .orElseThrow(() -> new ResourceNotFoundException("Branch with code '" + branchCode + "' not found."));

        // Validate PIN format and match against selected college and branch
        pinValidator.validateMatchesRegistration(studentInput.getPin(), college.getCode(), branch.getCode());

        if (userRepository.existsByUsername(studentInput.getPin()) || studentRepository.existsByPin(studentInput.getPin())) {
            throw new InvalidPinException("Student with PIN '" + studentInput.getPin() + "' is already registered.");
        }

        if (userRepository.existsByEmail(studentInput.getEmail())) {
            throw new InvalidPinException("Email '" + studentInput.getEmail() + "' is already in use.");
        }

        Student student = new Student();
        student.setUsername(studentInput.getPin());
        student.setPin(studentInput.getPin());
        student.setEmail(studentInput.getEmail());
        student.setPassword(passwordEncoder.encode(studentInput.getRawPassword()));
        student.setFullName(studentInput.getFullName());
        student.setPhoneNumber(studentInput.getPhoneNumber());
        student.setSchemeCode(studentInput.getSchemeCode() != null ? studentInput.getSchemeCode() : "C24");
        student.setCurrentSemester(studentInput.getCurrentSemester() != null ? studentInput.getCurrentSemester() : "1SEM");
        student.setAdmissionYear(studentInput.getAdmissionYear() != null ? studentInput.getAdmissionYear() : 2024);
        student.setCollege(college);
        student.setBranch(branch);
        student.setStatus(UserStatus.PENDING); // Pending HOD approval

        Student savedStudent = studentRepository.save(student);

        // Create approval request for the department HOD
        StudentApproval approval = new StudentApproval(savedStudent, college, branch);
        studentApprovalRepository.save(approval);

        return savedStudent;
    }

    @Transactional
    public Hod registerHod(Hod hodInput, String collegeCode, String branchCode) {
        College college = collegeRepository.findByCode(collegeCode)
            .orElseThrow(() -> new ResourceNotFoundException("College with code '" + collegeCode + "' not found."));

        Branch branch = branchRepository.findByCode(branchCode)
            .orElseThrow(() -> new ResourceNotFoundException("Branch with code '" + branchCode + "' not found."));

        if (userRepository.existsByUsername(hodInput.getEmail()) || userRepository.existsByEmail(hodInput.getEmail())) {
            throw new InvalidPinException("HOD with email '" + hodInput.getEmail() + "' is already registered.");
        }

        Hod hod = new Hod();
        hod.setUsername(hodInput.getEmail());
        hod.setEmail(hodInput.getEmail());
        hod.setPassword(passwordEncoder.encode(hodInput.getRawPassword()));
        hod.setFullName(hodInput.getFullName());
        hod.setPhoneNumber(hodInput.getPhoneNumber());
        hod.setEmployeeId(hodInput.getEmployeeId());
        hod.setQualification(hodInput.getQualification());
        hod.setExperienceYears(hodInput.getExperienceYears());
        hod.setCollege(college);
        hod.setBranch(branch);
        hod.setStatus(UserStatus.PENDING); // Pending Admin approval

        Hod savedHod = hodRepository.save(hod);

        // Create approval request for Admin
        HodApproval approval = new HodApproval(savedHod, college, branch);
        hodApprovalRepository.save(approval);

        return savedHod;
    }

    public User login(String identifier, String rawPassword) {
        User user = userRepository.findByUsername(identifier)
            .or(() -> userRepository.findByEmail(identifier))
            .orElseThrow(() -> new UnauthorizedException("Invalid username/PIN or password."));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new UnauthorizedException("Invalid username/PIN or password.");
        }

        if (user.getStatus() == UserStatus.PENDING) {
            throw new UnauthorizedException("Your registration is currently PENDING approval from your institution.");
        }

        if (user.getStatus() == UserStatus.REJECTED) {
            throw new UnauthorizedException("Your registration was REJECTED. Please contact your college administrator.");
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new UnauthorizedException("Your account is SUSPENDED. Please contact your college administration.");
        }

        UserPrincipal principal = UserPrincipal.create(user);
        String token = tokenProvider.generateToken(principal);
        user.setToken(token);
        return user;
    }
}
