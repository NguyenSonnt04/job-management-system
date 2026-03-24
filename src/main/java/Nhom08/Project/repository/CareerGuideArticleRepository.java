package Nhom08.Project.repository;

import Nhom08.Project.entity.CareerGuideArticle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CareerGuideArticleRepository extends JpaRepository<CareerGuideArticle, Long> {

    Optional<CareerGuideArticle> findBySlug(String slug);

    Optional<CareerGuideArticle> findBySlugAndStatus(String slug, CareerGuideArticle.ArticleStatus status);

    List<CareerGuideArticle> findByStatusOrderByPinnedDescFeaturedDescPublishedAtDesc(CareerGuideArticle.ArticleStatus status);

    List<CareerGuideArticle> findByStatusOrderByPublishedAtDesc(CareerGuideArticle.ArticleStatus status);

    List<CareerGuideArticle> findAllByOrderByUpdatedAtDesc();

    boolean existsBySlug(String slug);
}
