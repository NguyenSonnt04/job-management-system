package Nhom08.Project.repository;

import Nhom08.Project.entity.ChatMessage;
import Nhom08.Project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.session.sessionId = :sessionId ORDER BY cm.createdAt ASC")
    List<ChatMessage> findMessagesBySessionId(@Param("sessionId") String sessionId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.user.id = :userId ORDER BY cm.createdAt DESC")
    List<ChatMessage> findUserMessages(@Param("userId") Long userId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.session.sessionId = :sessionId AND cm.role = :role ORDER BY cm.createdAt ASC")
    List<ChatMessage> findBySessionSessionIdAndRole(@Param("sessionId") String sessionId, @Param("role") String role);

    @Modifying
    @Transactional
    void deleteBySessionId(Long sessionId);

    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.session.id = :sessionId")
    long countBySessionId(@Param("sessionId") Long sessionId);
}
