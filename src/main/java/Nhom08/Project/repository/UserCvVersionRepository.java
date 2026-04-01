package Nhom08.Project.repository;

import Nhom08.Project.entity.UserCvVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCvVersionRepository extends JpaRepository<UserCvVersion, Long> {
    List<UserCvVersion> findByUserCvIdOrderByVersionNoDesc(Long userCvId);
    Optional<UserCvVersion> findTopByUserCvIdOrderByVersionNoDesc(Long userCvId);
    boolean existsByUserCvId(Long userCvId);
    void deleteByUserCvId(Long userCvId);
}
