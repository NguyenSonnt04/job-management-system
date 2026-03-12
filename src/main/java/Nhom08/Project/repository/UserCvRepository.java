package Nhom08.Project.repository;

import Nhom08.Project.entity.UserCv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCvRepository extends JpaRepository<UserCv, Long> {
    List<UserCv> findByUserIdOrderByUpdatedAtDesc(Long userId);
    void deleteByIdAndUserId(Long id, Long userId);
}
