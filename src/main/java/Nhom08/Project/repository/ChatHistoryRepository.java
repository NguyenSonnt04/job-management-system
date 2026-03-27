package Nhom08.Project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import Nhom08.Project.entity.ChatHistory;

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
        "SELECT * FROM chat_history WHERE session_id = ?1 ORDER BY created_at DESC LIMIT ?2",
        nativeQuery = true)
    List<ChatHistory> findRecentBySessionId(String sessionId, int limit);

    /**
     * Đếm số lượng chat trong session
     */
    long countBySessionId(String sessionId);
}
