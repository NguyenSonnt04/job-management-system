package Nhom08.Project.repository;

import Nhom08.Project.entity.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {

    @Query("SELECT s FROM InterviewSession s WHERE s.user.id = :userId ORDER BY s.createdAt DESC")
    List<InterviewSession> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT s FROM InterviewSession s WHERE s.user.id = :userId AND s.status = :status ORDER BY s.createdAt DESC")
    List<InterviewSession> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    long countByUserId(Long userId);

    @Query("SELECT s FROM InterviewSession s WHERE s.user.id = :userId ORDER BY s.createdAt DESC")
    List<InterviewSession> findTop5ByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId,
            org.springframework.data.domain.Pageable pageable);
}
