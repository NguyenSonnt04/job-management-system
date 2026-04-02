package Nhom08.Project.controller;

import Nhom08.Project.entity.User;
import Nhom08.Project.repository.UserRepository;
import Nhom08.Project.security.CustomOAuth2User;
import Nhom08.Project.service.CustomUserDetailsService;
import Nhom08.Project.service.EmailService;
import Nhom08.Project.service.OtpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/2fa")
public class TwoFactorController {

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * Xác thực OTP cho đăng nhập Google (2FA)
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyOtp(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {

        String email = body.get("email");
        String inputOtp = body.get("otp");

        if (email == null || inputOtp == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thiếu thông tin"));
        }

        if (!otpService.verifyOtp(email, inputOtp.trim())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Mã OTP không đúng hoặc đã hết hạn"));
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Phiên đã hết hạn, vui lòng đăng nhập lại"));
        }

        Authentication pendingAuth = (Authentication) session.getAttribute("PENDING_OAUTH2_AUTH");
        if (pendingAuth == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không tìm thấy phiên đăng nhập, vui lòng thử lại"));
        }

        session.removeAttribute("PENDING_OAUTH2_AUTH");

        // Hoàn tất đăng nhập OAuth2
        SecurityContextHolderStrategy strategy = SecurityContextHolder.getContextHolderStrategy();
        SecurityContext securityContext = strategy.createEmptyContext();
        securityContext.setAuthentication(pendingAuth);
        strategy.setContext(securityContext);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

        CustomOAuth2User oauth2User = (CustomOAuth2User) pendingAuth.getPrincipal();
        User user = oauth2User.getUser();

        String redirectUrl = user.isAdmin() ? "/admin/dashboard"
                           : user.isEmployer() ? "/dashboard.html"
                           : "/index.html";

        return ResponseEntity.ok(Map.of("success", true, "redirectUrl", redirectUrl));
    }

    /**
     * Xác thực OTP cho đăng ký email → kích hoạt tài khoản + tự đăng nhập
     */
    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, Object>> verifyEmail(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {

        String email = body.get("email");
        String inputOtp = body.get("otp");

        if (email == null || inputOtp == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thiếu thông tin"));
        }

        if (!otpService.verifyOtp(email, inputOtp.trim())) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Mã OTP không đúng hoặc đã hết hạn"));
        }

        // Kích hoạt tài khoản
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));
        user.setEnabled(true);
        userRepository.save(user);

        // Tự động đăng nhập sau khi xác thực email
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolderStrategy strategy = SecurityContextHolder.getContextHolderStrategy();
        SecurityContext securityContext = strategy.createEmptyContext();
        securityContext.setAuthentication(auth);
        strategy.setContext(securityContext);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

        return ResponseEntity.ok(Map.of("success", true, "redirectUrl", "/index.html"));
    }

    /**
     * Gửi lại OTP (dùng cho cả đăng nhập Google và đăng ký email)
     */
    @PostMapping("/resend")
    public ResponseEntity<Map<String, Object>> resendOtp(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {

        String email = body.get("email");
        String type = body.get("type"); // "register" hoặc null (OAuth2)

        if (email == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thiếu email"));
        }

        // Với OAuth2: bắt buộc có session pending
        if (!"register".equals(type)) {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("PENDING_OAUTH2_AUTH") == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Phiên đã hết hạn, vui lòng đăng nhập lại"));
            }
        } else {
            // Với register: kiểm tra user tồn tại và chưa được kích hoạt
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null || user.getEnabled()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Tài khoản không hợp lệ"));
            }
        }

        String otp = otpService.generateOtp(email);
        try {
            emailService.sendOtpEmail(email, otp);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã gửi lại mã OTP"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không thể gửi email, vui lòng thử lại"));
        }
    }
}
