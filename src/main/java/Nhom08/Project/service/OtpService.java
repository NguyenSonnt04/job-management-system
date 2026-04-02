package Nhom08.Project.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final int OTP_EXPIRY_SECONDS = 300; // 5 phút
    private static final int MAX_ATTEMPTS = 5;

    private record OtpEntry(String otp, Instant expiresAt, int attempts) {}

    private final ConcurrentHashMap<String, OtpEntry> store = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    /** Tạo OTP 6 chữ số và lưu vào bộ nhớ, trả về OTP đó */
    public String generateOtp(String email) {
        String otp = String.format("%06d", random.nextInt(1_000_000));
        store.put(email.toLowerCase(), new OtpEntry(otp, Instant.now().plusSeconds(OTP_EXPIRY_SECONDS), 0));
        return otp;
    }

    /** Kiểm tra OTP: true nếu đúng và còn hạn */
    public boolean verifyOtp(String email, String inputOtp) {
        OtpEntry entry = store.get(email.toLowerCase());
        if (entry == null) return false;
        if (Instant.now().isAfter(entry.expiresAt())) {
            store.remove(email.toLowerCase());
            return false;
        }
        if (entry.attempts() >= MAX_ATTEMPTS) {
            store.remove(email.toLowerCase());
            return false;
        }
        if (!entry.otp().equals(inputOtp)) {
            store.put(email.toLowerCase(), new OtpEntry(entry.otp(), entry.expiresAt(), entry.attempts() + 1));
            return false;
        }
        store.remove(email.toLowerCase());
        return true;
    }

    public boolean hasValidOtp(String email) {
        OtpEntry entry = store.get(email.toLowerCase());
        return entry != null && Instant.now().isBefore(entry.expiresAt());
    }
}
