package Nhom08.Project.repository;

import Nhom08.Project.entity.CvJobMatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CvJobMatchRepository extends JpaRepository<CvJobMatch, Long> {

    List<CvJobMatch> findBySessionIdOrderByMatchScoreDesc(Long sessionId);

    boolean existsBySessionId(Long sessionId);

    void deleteBySessionId(Long sessionId);
}
