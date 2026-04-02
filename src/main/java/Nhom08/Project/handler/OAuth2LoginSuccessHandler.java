package Nhom08.Project.handler;

import Nhom08.Project.security.CustomOAuth2User;
import Nhom08.Project.service.EmailService;
import Nhom08.Project.service.OtpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();
        String email = oauth2User.getEmail();

        // Lưu Authentication vào session (chưa hoàn tất đăng nhập)
        HttpSession session = request.getSession(true);
        session.setAttribute("PENDING_OAUTH2_AUTH", authentication);

        // Xóa SecurityContext khỏi bộ nhớ VÀ khỏi session
        // (Spring Security đã lưu vào session trước khi gọi handler này)
        SecurityContextHolder.clearContext();
        session.removeAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);

        // Tạo và gửi OTP
        String otp = otpService.generateOtp(email);
        String redirectUrl;
        try {
            emailService.sendOtpEmail(email, otp);
            redirectUrl = "/otp-verify.html?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Nếu gửi email thất bại → redirect với thông báo lỗi
            redirectUrl = "/otp-verify.html?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8)
                        + "&mailError=true";
        }
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
