package Nhom08.Project.service;

import Nhom08.Project.dto.EmployerRegisterStep1DTO;
import Nhom08.Project.dto.EmployerRegisterStep2DTO;
import Nhom08.Project.dto.LoginDTO;
import Nhom08.Project.dto.UserRegisterDTO;
import Nhom08.Project.entity.Employer;
import Nhom08.Project.entity.Role;
import Nhom08.Project.entity.User;
import Nhom08.Project.repository.EmployerRepository;
import Nhom08.Project.repository.RoleRepository;
import Nhom08.Project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EmployerRepository employerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Register a new User (candidate/job seeker)
     */
    @Transactional
    public User registerUser(UserRegisterDTO dto) {
        // Check if email already exists
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        // Get CANDIDATE role
        Role candidateRole = roleRepository.findByName(Role.CANDIDATE)
                .orElseThrow(() -> new RuntimeException("Role CANDIDATE không tồn tại"));

        // Create user
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());
        user.setRole(candidateRole);
        user.setEnabled(false); // Chờ xác thực email

        return userRepository.save(user);
    }

    /**
     * Register a new Employer - Step 1: Create user with EMPLOYER role
     */
    @Transactional
    public User registerEmployerStep1(EmployerRegisterStep1DTO dto) {
        // Check if email already exists
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        // Get EMPLOYER role
        Role employerRole = roleRepository.findByName(Role.EMPLOYER)
                .orElseThrow(() -> new RuntimeException("Role EMPLOYER không tồn tại"));

        // Create user with employer role
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(employerRole);
        user.setEnabled(true);

        return userRepository.save(user);
    }

    /**
     * Register a new Employer - Step 2: Create employer profile
     */
    @Transactional
    public Employer registerEmployerStep2(Long userId, EmployerRegisterStep2DTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // Check if employer profile already exists
        if (employerRepository.findByUserId(userId).isPresent()) {
            throw new RuntimeException("Thông tin công ty đã tồn tại");
        }

        // Create employer profile
        Employer employer = new Employer();
        employer.setUser(user);
        employer.setCompanyName(dto.getCompanyName());
        employer.setBusinessType(dto.getBusinessType());
        employer.setEmployeeCount(dto.getEmployeeCount());
        employer.setCountry(dto.getCountry());
        employer.setProvince(dto.getProvince());
        employer.setAddress(dto.getAddress());
        employer.setDescription(dto.getDescription());
        employer.setContactName(dto.getContactName());
        employer.setContactPhone(dto.getContactPhone());
        employer.setTaxCode(dto.getTaxCode());

        return employerRepository.save(employer);
    }

    /**
     * Authenticate user
     */
    public Optional<User> authenticate(LoginDTO dto) {
        Optional<User> userOpt = userRepository.findByEmail(dto.getEmail());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getEnabled() && passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
                return Optional.of(user);
            }
        }
        
        return Optional.empty();
    }

    /**
     * Check if email exists
     */
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Find user by id
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
