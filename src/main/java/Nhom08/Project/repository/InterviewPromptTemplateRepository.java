package Nhom08.Project.repository;

import Nhom08.Project.entity.InterviewPromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewPromptTemplateRepository extends JpaRepository<InterviewPromptTemplate, Long> {

    Optional<InterviewPromptTemplate> findByPromptKeyAndActiveTrue(String promptKey);

    List<InterviewPromptTemplate> findByActiveTrueOrderByPromptKeyAsc();
}
