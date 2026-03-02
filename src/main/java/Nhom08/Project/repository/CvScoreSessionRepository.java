package Nhom08.Project.repository;

import Nhom08.Project.entity.CvScoreSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CvScoreSessionRepository extends JpaRepository<CvScoreSession, Long> {

    @Query("SELECT s FROM CvScoreSession s WHERE s.user.id = :userId ORDER BY s.createdAt DESC")
    List<CvScoreSession> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
}
