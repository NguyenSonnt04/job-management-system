package Nhom08.Project.controller;

import Nhom08.Project.dto.UserProfileUpdateDTO;
import Nhom08.Project.entity.Employer;
import Nhom08.Project.entity.User;
import Nhom08.Project.repository.EmployerRepository;
import Nhom08.Project.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployerRepository employerRepository;

    /**
     * Get current logged-in user info
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        Map<String, Object> response = new HashMap<>();

        Optional<User> userOpt = getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            response.put("authenticated", false);
            return ResponseEntity.ok(response);
        }

        User user = userOpt.get();
        response.put("authenticated", true);
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("fullName", user.getFullName());
        response.put("phone", user.getPhone());
        response.put("contactEmail", user.getContactEmail());
        response.put("dob", user.getDob());
        response.put("occupation", user.getOccupation());
        response.put("skills", user.getSkills());
        response.put("education", user.getEducation());
        response.put("experience", user.getExperience());
        response.put("projects", user.getProjects());
        response.put("role", user.getRole().getName());

        // Build displayName — always set regardless of employer record
        String displayName = user.getFullName();

        // If employer, include company info
        if (user.isEmployer()) {
            Optional<Employer> employerOpt = employerRepository.findByUserId(user.getId());
            if (employerOpt.isPresent()) {
                Employer employer = employerOpt.get();
                Map<String, Object> employerInfo = new HashMap<>();
                employerInfo.put("id", employer.getId());
                employerInfo.put("companyName", employer.getCompanyName());
                employerInfo.put("contactName", employer.getContactName());
                employerInfo.put("contactPhone", employer.getContactPhone());
                employerInfo.put("province", employer.getProvince());
                response.put("employer", employerInfo);

                // Prefer contactName if fullName is blank
                if (displayName == null || displayName.isBlank()) {
                    displayName = employer.getContactName();
                }
            }
        }

        // Final fallback to email
        if (displayName == null || displayName.isBlank()) {
            displayName = user.getEmail();
        }
        response.put("displayName", displayName);


        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@Valid @RequestBody UserProfileUpdateDTO dto) {
        Map<String, Object> response = new HashMap<>();

        Optional<User> userOpt = getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Ban can dang nhap de cap nhat ho so");
            response.put("authenticated", false);
            return ResponseEntity.status(401).body(response);
        }

        User user = userOpt.get();
        user.setFullName(normalize(dto.getFullName()));
        user.setPhone(normalize(dto.getPhone()));
        user.setContactEmail(normalize(dto.getContactEmail()));
        user.setDob(normalize(dto.getDob()));
        user.setOccupation(normalize(dto.getOccupation()));
        user.setSkills(normalize(dto.getSkills()));
        user.setEducation(normalize(dto.getEducation()));
        user.setExperience(normalize(dto.getExperience()));
        user.setProjects(normalize(dto.getProjects()));
        userRepository.save(user);

        response.put("success", true);
        response.put("message", "Cap nhat thong tin thanh cong");
        response.put("authenticated", true);
        response.put("fullName", user.getFullName());
        response.put("phone", user.getPhone());
        response.put("email", user.getEmail());
        response.put("contactEmail", user.getContactEmail());
        response.put("dob", user.getDob());
        response.put("occupation", user.getOccupation());
        response.put("skills", user.getSkills());
        response.put("education", user.getEducation());
        response.put("experience", user.getExperience());
        response.put("projects", user.getProjects());
        return ResponseEntity.ok(response);
    }

    private Optional<User> getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }

        return userRepository.findByEmail(auth.getName());
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
