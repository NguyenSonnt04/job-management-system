package Nhom08.Project.repository;

import Nhom08.Project.entity.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository cho Chat History
 */
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Integer> {

    /**
     * Lấy lịch sử chat theo session, mới nhất trước
     */
    List<ChatHistory> findBySessionIdOrderByCreatedAtDesc(String sessionId);

    /**
     * Lấy lịch sử chat gần đây của một session (giới hạn số lượng)
     */
    @org.springframework.data.jpa.repository.Query(value =
        "SELECT * FROM chat_history WHERE session_id = :sessionId ORDER BY created_at DESC LIMIT :limit",
        nativeQuery = true)
    List<ChatHistory> findRecentBySessionId(String sessionId, int limit);

    /**
     * Đếm số lượng chat trong session
     */
    long countBySessionId(String sessionId);
}
