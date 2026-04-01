package Nhom08.Project.repository;

import Nhom08.Project.entity.InterviewResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InterviewResultRepository extends JpaRepository<InterviewResult, Long> {

    Optional<InterviewResult> findBySessionId(Long sessionId);

    @Query("SELECT AVG(r.overallScore) FROM InterviewResult r WHERE r.session.user.id = :userId")
    Double findAvgScoreByUserId(@Param("userId") Long userId);
}
