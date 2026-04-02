package Nhom08.Project.controller;

import Nhom08.Project.dto.EmployerRegisterStep1DTO;
import Nhom08.Project.dto.EmployerRegisterStep2DTO;
import Nhom08.Project.dto.LoginDTO;
import Nhom08.Project.dto.UserRegisterDTO;
import Nhom08.Project.entity.Employer;
import Nhom08.Project.entity.User;
import Nhom08.Project.repository.UserRepository;
import Nhom08.Project.service.AuthService;
import Nhom08.Project.service.EmailService;
import Nhom08.Project.service.OtpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    /**
     * GET /api/auth/me — returns current logged-in user info
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(Authentication auth) {
        Map<String, Object> resp = new HashMap<>();
        if (auth == null || !auth.isAuthenticated()) {
            resp.put("loggedIn", false);
            return ResponseEntity.ok(resp);
        }
        return authService.findByEmail(auth.getName()).map(user -> {
            resp.put("loggedIn",    true);
            resp.put("id",          user.getId());
            resp.put("email",       user.getEmail());
            resp.put("fullName",    user.getFullName());
            resp.put("phone",       user.getPhone());
            resp.put("role",        user.getRole().getName());
            resp.put("displayName", user.getFullName() != null ? user.getFullName() : user.getEmail());
            return ResponseEntity.ok(resp);
        }).orElseGet(() -> {
            resp.put("loggedIn", false);
            return ResponseEntity.ok(resp);
        });
    }

    /**
     * Check if email exists
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        boolean exists = authService.isEmailExists(email);
        response.put("exists", exists);
        response.put("message", exists ? "Email đã được sử dụng" : "Email có thể sử dụng");
        return ResponseEntity.ok(response);
    }

    /**
     * Register new user (candidate/job seeker)
     */
    @PostMapping("/register/user")
    public ResponseEntity<Map<String, Object>> registerUser(@Valid @RequestBody UserRegisterDTO dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!dto.isPasswordMatch()) {
                response.put("success", false);
                response.put("message", "Mật khẩu xác nhận không khớp");
                return ResponseEntity.badRequest().body(response);
            }

            User user = authService.registerUser(dto);

            // Gửi OTP xác thực email
            String otp = otpService.generateOtp(user.getEmail());
            try {
                emailService.sendOtpEmail(user.getEmail(), otp);
                response.put("mailSent", true);
            } catch (Exception e) {
                response.put("mailSent", false);
            }

            response.put("success", true);
            response.put("requireEmailVerification", true);
            response.put("email", user.getEmail());
            response.put("message", "Đăng ký thành công! Vui lòng kiểm tra email để lấy mã OTP.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Employer registration - Step 1: Login info
     */
    @PostMapping("/register/employer/step1")
    public ResponseEntity<Map<String, Object>> registerEmployerStep1(
            @Valid @RequestBody EmployerRegisterStep1DTO dto,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!dto.isEmailMatch()) {
                response.put("success", false);
                response.put("message", "Email xác nhận không khớp");
                return ResponseEntity.badRequest().body(response);
            }

            if (!dto.isPasswordMatch()) {
                response.put("success", false);
                response.put("message", "Mật khẩu xác nhận không khớp");
                return ResponseEntity.badRequest().body(response);
            }

            User user = authService.registerEmployerStep1(dto);
            
            // Store user ID in session for step 2
            session.setAttribute("employerUserId", user.getId());
            
            response.put("success", true);
            response.put("message", "Bước 1 hoàn thành. Tiếp tục điền thông tin công ty.");
            response.put("userId", user.getId());
            response.put("redirectUrl", "/employer-register-step-2.html");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Employer registration - Step 2: Company info
     */
    @PostMapping("/register/employer/step2")
    public ResponseEntity<Map<String, Object>> registerEmployerStep2(
            @Valid @RequestBody EmployerRegisterStep2DTO dto,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = (Long) session.getAttribute("employerUserId");
            if (userId == null) {
                response.put("success", false);
                response.put("message", "Phiên đăng ký đã hết hạn. Vui lòng bắt đầu lại từ đầu.");
                response.put("redirectUrl", "/employer-register.html");
                return ResponseEntity.badRequest().body(response);
            }

            Employer employer = authService.registerEmployerStep2(userId, dto);
            
            // Clear session
            session.removeAttribute("employerUserId");
            
            response.put("success", true);
            response.put("message", "Đăng ký thành công! Vui lòng đăng nhập.");
            response.put("employerId", employer.getId());
            response.put("redirectUrl", "/employer-login.html");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Login endpoint - Creates Spring Security session
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginDTO dto,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Authenticate using Spring Security
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword());
            
            Authentication authentication = authenticationManager.authenticate(authToken);
            
            // Set authentication in SecurityContext
            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authentication);
            
            // Store in HTTP session
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
            
            // Get user details
            User user = authService.findByEmail(dto.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            response.put("success", true);
            response.put("message", "Đăng nhập thành công");
            response.put("role", user.getRole().getName());
            response.put("displayName", user.getFullName() != null ? user.getFullName() : user.getEmail());
            
            // Redirect based on role
            String redirectUrl = switch (user.getRole().getName()) {
                case "ROLE_ADMIN" -> "/admin/dashboard.html";
                case "ROLE_EMPLOYER" -> "/dashboard.html";
                default -> "/index.html";
            };
            response.put("redirectUrl", redirectUrl);
            
            return ResponseEntity.ok(response);
            
        } catch (AuthenticationException e) {
            response.put("success", false);
            response.put("message", "Email hoặc mật khẩu không chính xác");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Bước 1: Gửi OTP về email để reset mật khẩu
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> body) {
        Map<String, Object> response = new HashMap<>();
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            response.put("success", false);
            response.put("message", "Vui lòng nhập email");
            return ResponseEntity.badRequest().body(response);
        }
        User user = userRepository.findByEmail(email.trim().toLowerCase()).orElse(null);
        if (user == null) {
            response.put("success", false);
            response.put("message", "Email không tồn tại trong hệ thống");
            return ResponseEntity.badRequest().body(response);
        }
        String otp = otpService.generateOtp(email);
        try {
            emailService.sendOtpEmail(email, otp);
            response.put("success", true);
            response.put("mailSent", true);
        } catch (Exception e) {
            response.put("success", true);
            response.put("mailSent", false);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Bước 2: Xác thực OTP → cấp quyền đặt lại mật khẩu
     */
    @PostMapping("/verify-reset-otp")
    public ResponseEntity<Map<String, Object>> verifyResetOtp(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        String email = body.get("email");
        String otp   = body.get("otp");
        if (!otpService.verifyOtp(email, otp == null ? "" : otp.trim())) {
            response.put("success", false);
            response.put("message", "Mã OTP không đúng hoặc đã hết hạn");
            return ResponseEntity.badRequest().body(response);
        }
        // Lưu vào session: email này được phép đặt lại mật khẩu
        HttpSession session = request.getSession(true);
        session.setAttribute("RESET_PASSWORD_EMAIL", email);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    /**
     * Bước 3: Đặt lại mật khẩu mới
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("RESET_PASSWORD_EMAIL") == null) {
            response.put("success", false);
            response.put("message", "Phiên đã hết hạn, vui lòng thực hiện lại từ đầu");
            return ResponseEntity.badRequest().body(response);
        }
        String sessionEmail  = (String) session.getAttribute("RESET_PASSWORD_EMAIL");
        String newPassword   = body.get("newPassword");
        String confirmPassword = body.get("confirmPassword");

        if (newPassword == null || newPassword.length() < 6) {
            response.put("success", false);
            response.put("message", "Mật khẩu phải có ít nhất 6 ký tự");
            return ResponseEntity.badRequest().body(response);
        }
        if (!newPassword.equals(confirmPassword)) {
            response.put("success", false);
            response.put("message", "Mật khẩu xác nhận không khớp");
            return ResponseEntity.badRequest().body(response);
        }
        User user = userRepository.findByEmail(sessionEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));
        user.setPassword(authService.encodePassword(newPassword));
        userRepository.save(user);
        session.removeAttribute("RESET_PASSWORD_EMAIL");
        response.put("success", true);
        response.put("message", "Đặt lại mật khẩu thành công");
        return ResponseEntity.ok(response);
    }
}

