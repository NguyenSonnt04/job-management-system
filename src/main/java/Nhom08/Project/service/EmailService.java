package Nhom08.Project.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Mã xác thực đăng nhập - CoHoiViecLam");

            String html = """
                <div style="font-family: Arial, sans-serif; max-width: 480px; margin: auto;
                            border: 1px solid #e5e7eb; border-radius: 12px; overflow: hidden;">
                  <div style="background: #1D4ED8; padding: 24px; text-align: center;">
                    <h1 style="color: #fff; margin: 0; font-size: 22px;">CoHoiViecLam</h1>
                  </div>
                  <div style="padding: 32px;">
                    <h2 style="color: #111827; margin-top: 0;">Xác thực đăng nhập</h2>
                    <p style="color: #6b7280;">Mã OTP của bạn là:</p>
                    <div style="background: #f3f4f6; border-radius: 8px; padding: 20px;
                                text-align: center; letter-spacing: 12px; font-size: 36px;
                                font-weight: bold; color: #1D4ED8; margin: 16px 0;">
                      %s
                    </div>
                    <p style="color: #6b7280; font-size: 14px;">
                      Mã có hiệu lực trong <strong>5 phút</strong>.<br>
                      Không chia sẻ mã này với bất kỳ ai.
                    </p>
                  </div>
                  <div style="background: #f9fafb; padding: 16px; text-align: center;
                              color: #9ca3af; font-size: 12px;">
                    Nếu bạn không thực hiện yêu cầu này, hãy bỏ qua email này.
                  </div>
                </div>
                """.formatted(otp);

            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email OTP: " + e.getMessage(), e);
        }
    }
}
