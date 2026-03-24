package Nhom08.Project.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "career_guide_articles",
    indexes = {
        @Index(name = "idx_career_guide_article_slug", columnList = "slug", unique = true),
        @Index(name = "idx_career_guide_article_status_published", columnList = "status,published_at")
    }
)
public class CareerGuideArticle {

    public enum ArticleStatus {
        DRAFT,
        PUBLISHED,
        ARCHIVED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CareerGuideCategory category;

    @Column(nullable = false, unique = true, length = 220)
    private String slug;

    @Column(nullable = false, length = 250)
    private String title;

    @Column(length = 500)
    private String excerpt;

    @Column(name = "content_html", columnDefinition = "LONGTEXT", nullable = false)
    private String contentHtml;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(name = "cover_image_alt", length = 255)
    private String coverImageAlt;

    @Column(name = "author_name", length = 120)
    private String authorName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ArticleStatus status = ArticleStatus.PUBLISHED;

    @Column(nullable = false)
    private Boolean featured = false;

    @Column(nullable = false)
    private Boolean pinned = false;

    @Column(name = "read_time_minutes")
    private Integer readTimeMinutes;

    @Column(name = "views")
    private Long views = 0L;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "career_guide_article_tags",
        joinColumns = @JoinColumn(name = "article_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<CareerGuideTag> tags = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) status = ArticleStatus.PUBLISHED;
        if (featured == null) featured = false;
        if (pinned == null) pinned = false;
        if (views == null) views = 0L;
        if (readTimeMinutes == null) readTimeMinutes = 5;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public CareerGuideCategory getCategory() { return category; }
    public void setCategory(CareerGuideCategory category) { this.category = category; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getExcerpt() { return excerpt; }
    public void setExcerpt(String excerpt) { this.excerpt = excerpt; }
    public String getContentHtml() { return contentHtml; }
    public void setContentHtml(String contentHtml) { this.contentHtml = contentHtml; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
    public String getCoverImageAlt() { return coverImageAlt; }
    public void setCoverImageAlt(String coverImageAlt) { this.coverImageAlt = coverImageAlt; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public ArticleStatus getStatus() { return status; }
    public void setStatus(ArticleStatus status) { this.status = status; }
    public Boolean getFeatured() { return featured; }
    public void setFeatured(Boolean featured) { this.featured = featured; }
    public Boolean getPinned() { return pinned; }
    public void setPinned(Boolean pinned) { this.pinned = pinned; }
    public Integer getReadTimeMinutes() { return readTimeMinutes; }
    public void setReadTimeMinutes(Integer readTimeMinutes) { this.readTimeMinutes = readTimeMinutes; }
    public Long getViews() { return views; }
    public void setViews(Long views) { this.views = views; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<CareerGuideTag> getTags() { return tags; }
    public void setTags(List<CareerGuideTag> tags) { this.tags = tags; }
}
