package Nhom08.Project.controller;

import Nhom08.Project.entity.Employer;
import Nhom08.Project.entity.User;
import Nhom08.Project.repository.EmployerRepository;
import Nhom08.Project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/employer")
public class EmployerProfileController {

    @Autowired private EmployerRepository employerRepo;
    @Autowired private UserRepository userRepo;

    /**
     * GET /api/employer/profile — thông tin tài khoản employer hiện tại
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        }

        Optional<User> userOpt = userRepo.findByEmail(auth.getName());
        if (userOpt.isEmpty()) return ResponseEntity.status(401).body(Map.of("message", "User not found"));

        User user = userOpt.get();
        Optional<Employer> empOpt = employerRepo.findByUserId(user.getId());
        if (empOpt.isEmpty()) {
            return ResponseEntity.status(403).body(Map.of("message", "Không phải nhà tuyển dụng"));
        }

        Employer emp = empOpt.get();
        Map<String, Object> result = new LinkedHashMap<>();
        // User info
        result.put("userId", user.getId());
        result.put("email", user.getEmail());
        result.put("fullName", user.getFullName());
        result.put("phone", user.getPhone());
        result.put("role", user.getRole() != null ? user.getRole().getName() : "EMPLOYER");
        result.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);

        // Employer info
        result.put("employerId", emp.getId());
        result.put("companyName", emp.getCompanyName());
        result.put("businessType", emp.getBusinessType());
        result.put("employeeCount", emp.getEmployeeCount());
        result.put("country", emp.getCountry());
        result.put("province", emp.getProvince());
        result.put("address", emp.getAddress());
        result.put("description", emp.getDescription());
        result.put("contactName", emp.getContactName());
        result.put("contactPhone", emp.getContactPhone());
        result.put("taxCode", emp.getTaxCode());
        result.put("logoUrl", emp.getLogoUrl());
        result.put("website", emp.getWebsite());

        return ResponseEntity.ok(result);
    }

    /**
     * PUT /api/employer/profile — cập nhật thông tin employer
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> body, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa đăng nhập"));
        }

        Optional<User> userOpt = userRepo.findByEmail(auth.getName());
        if (userOpt.isEmpty()) return ResponseEntity.status(401).body(Map.of("message", "User not found"));

        User user = userOpt.get();
        Optional<Employer> empOpt = employerRepo.findByUserId(user.getId());
        if (empOpt.isEmpty()) {
            return ResponseEntity.status(403).body(Map.of("message", "Không phải nhà tuyển dụng"));
        }

        Employer emp = empOpt.get();

        // Update user info
        if (body.containsKey("fullName")) user.setFullName((String) body.get("fullName"));
        if (body.containsKey("phone")) user.setPhone((String) body.get("phone"));
        userRepo.save(user);

        // Update employer info
        if (body.containsKey("companyName")) emp.setCompanyName((String) body.get("companyName"));
        if (body.containsKey("businessType")) emp.setBusinessType((String) body.get("businessType"));
        if (body.containsKey("employeeCount")) emp.setEmployeeCount((String) body.get("employeeCount"));
        if (body.containsKey("province")) emp.setProvince((String) body.get("province"));
        if (body.containsKey("address")) emp.setAddress((String) body.get("address"));
        if (body.containsKey("description")) emp.setDescription((String) body.get("description"));
        if (body.containsKey("contactName")) emp.setContactName((String) body.get("contactName"));
        if (body.containsKey("contactPhone")) emp.setContactPhone((String) body.get("contactPhone"));
        if (body.containsKey("taxCode")) emp.setTaxCode((String) body.get("taxCode"));
        if (body.containsKey("website")) emp.setWebsite((String) body.get("website"));
        employerRepo.save(emp);

        return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật thành công"));
    }
}
