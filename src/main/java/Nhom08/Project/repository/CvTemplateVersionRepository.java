package Nhom08.Project.repository;

import Nhom08.Project.entity.CvTemplateVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CvTemplateVersionRepository extends JpaRepository<CvTemplateVersion, Long> {
    List<CvTemplateVersion> findByTemplateIdOrderByVersionNoDesc(Long templateId);
    Optional<CvTemplateVersion> findTopByTemplateIdOrderByVersionNoDesc(Long templateId);
    boolean existsByTemplateId(Long templateId);
    void deleteByTemplateId(Long templateId);
}
