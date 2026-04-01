package Nhom08.Project.config;

import Nhom08.Project.service.ContactChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler tự động cleanup chat sessions cũ
 * Chạy mỗi giờ để xóa sessions không hoạt động quá 24h
 */
@Component
public class ChatCleanupScheduler {

    @Autowired
    private ContactChatbotService chatbotService;

    /**
     * Cleanup sessions cũ mỗi giờ
     * Chạy vào phút thứ 15 của mỗi giờ
     */
    @Scheduled(cron = "0 15 * * * ?")
    public void cleanupOldChatSessions() {
        System.out.println("⏰ [Scheduler] Starting automatic chat session cleanup...");
        try {
            int deletedCount = chatbotService.cleanupOldSessions();
            System.out.println("✅ [Scheduler] Cleanup completed. Deleted " + deletedCount + " sessions");
        } catch (Exception e) {
            System.err.println("❌ [Scheduler] Cleanup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cleanup khi khởi động ứng dụng
     */
    @Scheduled(initialDelay = 30000, fixedDelay = Long.MAX_VALUE)
    public void cleanupOnStartup() {
        System.out.println("🚀 [Startup] Running initial chat session cleanup...");
        try {
            int deletedCount = chatbotService.cleanupOldSessions();
            System.out.println("✅ [Startup] Initial cleanup completed. Deleted " + deletedCount + " sessions");
        } catch (Exception e) {
            System.err.println("❌ [Startup] Initial cleanup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
