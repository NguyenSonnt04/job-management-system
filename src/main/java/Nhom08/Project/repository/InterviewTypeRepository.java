package Nhom08.Project.repository;

import Nhom08.Project.entity.InterviewType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewTypeRepository extends JpaRepository<InterviewType, Long> {

    List<InterviewType> findByActiveTrueOrderByDisplayOrderAsc();

    Optional<InterviewType> findByTypeKey(String typeKey);
}
