package Nhom08.Project.repository;

import Nhom08.Project.entity.InterviewMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewMessageRepository extends JpaRepository<InterviewMessage, Long> {

    @Query("SELECT m FROM InterviewMessage m WHERE m.session.id = :sessionId ORDER BY m.messageOrder ASC")
    List<InterviewMessage> findBySessionIdOrderByMessageOrderAsc(@Param("sessionId") Long sessionId);

    long countBySessionId(Long sessionId);
}
