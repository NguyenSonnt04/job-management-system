package Nhom08.Project.repository;

import Nhom08.Project.entity.ChatSession;
import Nhom08.Project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    @Query("SELECT s FROM ChatSession s LEFT JOIN FETCH s.messages WHERE s.sessionId = :sessionId")
    Optional<ChatSession> findBySessionIdWithMessages(@Param("sessionId") String sessionId);

    @Query("SELECT s FROM ChatSession s WHERE s.user.id = :userId AND s.status = 'ACTIVE' ORDER BY s.updatedAt DESC")
    List<ChatSession> findActiveSessionsByUserIdOrderByUpdatedAtDesc(@Param("userId") Long userId);

    Optional<ChatSession> findBySessionId(String sessionId);

    List<ChatSession> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<ChatSession> findByUserIsNullAndStatusOrderByCreatedAtDesc(String status);

    void deleteBySessionId(String sessionId);
}
