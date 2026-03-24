package Nhom08.Project.service;

import Nhom08.Project.dto.CareerGuideArticleDetailView;
import Nhom08.Project.dto.CareerGuideArticleSummaryView;
import Nhom08.Project.dto.CareerGuideArticleUpsertRequest;
import Nhom08.Project.dto.CareerGuideCategoryView;
import Nhom08.Project.dto.CareerGuideHomeView;
import Nhom08.Project.dto.CareerGuideTagView;
import Nhom08.Project.entity.CareerGuideArticle;
import Nhom08.Project.entity.CareerGuideCategory;
import Nhom08.Project.entity.CareerGuideTag;
import Nhom08.Project.repository.CareerGuideArticleRepository;
import Nhom08.Project.repository.CareerGuideCategoryRepository;
import Nhom08.Project.repository.CareerGuideTagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CareerGuideService {

    private static final String DEFAULT_AUTHOR = "Ban biên tập CareerViet";
    private static final int DEFAULT_HOME_LIMIT = 12;
    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private final CareerGuideArticleRepository articleRepository;
    private final CareerGuideCategoryRepository categoryRepository;
    private final CareerGuideTagRepository tagRepository;

    public CareerGuideService(
            CareerGuideArticleRepository articleRepository,
            CareerGuideCategoryRepository categoryRepository,
            CareerGuideTagRepository tagRepository
    ) {
        this.articleRepository = articleRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
    }

    @Transactional(readOnly = true)
    public CareerGuideHomeView getHomeView() {
        return new CareerGuideHomeView(
            getCategories(),
            getPublishedArticles(null, null, null, DEFAULT_HOME_LIMIT)
        );
    }

    @Transactional(readOnly = true)
    public List<CareerGuideCategoryView> getCategories() {
        List<CareerGuideCategory> categories = categoryRepository.findByActiveTrueOrderByDisplayOrderAsc();
        List<CareerGuideArticle> published = articleRepository.findByStatusOrderByPinnedDescFeaturedDescPublishedAtDesc(
            CareerGuideArticle.ArticleStatus.PUBLISHED
        );

        Map<Long, Long> counts = published.stream()
            .filter(article -> article.getCategory() != null && article.getCategory().getId() != null)
            .collect(Collectors.groupingBy(article -> article.getCategory().getId(), Collectors.counting()));

        return categories.stream()
            .map(category -> new CareerGuideCategoryView(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getIcon(),
                category.getAccentColor(),
                counts.getOrDefault(category.getId(), 0L)
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<CareerGuideTagView> getTags() {
        return tagRepository.findByActiveTrueOrderByDisplayOrderAsc().stream()
            .map(this::toTagView)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<CareerGuideArticleSummaryView> getPublishedArticles(String categorySlug, String tagSlug, String query, Integer limit) {
        int safeLimit = normalizeLimit(limit, DEFAULT_HOME_LIMIT);
        List<CareerGuideArticle> articles = articleRepository.findByStatusOrderByPinnedDescFeaturedDescPublishedAtDesc(
            CareerGuideArticle.ArticleStatus.PUBLISHED
        );

        List<CareerGuideArticle> filtered = filterArticles(articles, categorySlug, tagSlug, query);
        return filtered.stream()
            .limit(safeLimit)
            .map(this::toSummaryView)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<CareerGuideArticleSummaryView> getAdminArticles() {
        return articleRepository.findAllByOrderByUpdatedAtDesc().stream()
            .map(this::toSummaryView)
            .toList();
    }

    @Transactional
    public CareerGuideArticleDetailView getArticleDetail(String slug) {
        CareerGuideArticle article = articleRepository.findBySlugAndStatus(slug, CareerGuideArticle.ArticleStatus.PUBLISHED)
            .orElseThrow(() -> new NoSuchElementException("Không tìm thấy bài viết"));

        article.setViews(safeViews(article.getViews()) + 1);
        if (article.getPublishedAt() == null) {
            article.setPublishedAt(LocalDateTime.now());
        }

        CareerGuideArticleSummaryView summary = toSummaryView(article);
        List<CareerGuideArticleSummaryView> related = getRelatedArticles(article, 4);

        return new CareerGuideArticleDetailView(
            summary,
            article.getContentHtml(),
            summary.tags(),
            related
        );
    }

    @Transactional
    public CareerGuideArticleSummaryView createArticle(CareerGuideArticleUpsertRequest request) {
        CareerGuideArticle article = new CareerGuideArticle();
        applyRequest(article, request, true);
        articleRepository.save(article);
        return toSummaryView(article);
    }

    @Transactional
    public CareerGuideArticleSummaryView updateArticle(Long id, CareerGuideArticleUpsertRequest request) {
        CareerGuideArticle article = articleRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Không tìm thấy bài viết"));
        applyRequest(article, request, false);
        articleRepository.save(article);
        return toSummaryView(article);
    }

    @Transactional
    public void deleteArticle(Long id) {
        if (!articleRepository.existsById(id)) {
            throw new NoSuchElementException("Không tìm thấy bài viết");
        }
        articleRepository.deleteById(id);
    }

    private List<CareerGuideArticle> filterArticles(
            List<CareerGuideArticle> articles,
            String categorySlug,
            String tagSlug,
            String query
    ) {
        CareerGuideCategory category = null;
        if (StringUtils.hasText(categorySlug)) {
            category = categoryRepository.findBySlug(categorySlug.trim()).orElse(null);
            if (category == null) {
                return List.of();
            }
        }

        CareerGuideTag tag = null;
        if (StringUtils.hasText(tagSlug)) {
            tag = tagRepository.findBySlug(tagSlug.trim()).orElse(null);
            if (tag == null) {
                return List.of();
            }
        }
        String normalizedQuery = normalizeForSearch(query);
        final CareerGuideCategory selectedCategory = category;
        final CareerGuideTag selectedTag = tag;

        return articles.stream()
            .filter(article -> selectedCategory == null || (article.getCategory() != null && Objects.equals(article.getCategory().getId(), selectedCategory.getId())))
            .filter(article -> selectedTag == null || containsTag(article, selectedTag.getId()))
            .filter(article -> !StringUtils.hasText(normalizedQuery) || matchesQuery(article, normalizedQuery))
            .toList();
    }

    private boolean containsTag(CareerGuideArticle article, Long tagId) {
        return article.getTags() != null && article.getTags().stream().anyMatch(tag -> Objects.equals(tag.getId(), tagId));
    }

    private boolean matchesQuery(CareerGuideArticle article, String query) {
        String tagNames = article.getTags() == null ? "" : article.getTags().stream()
            .map(CareerGuideTag::getName)
            .collect(Collectors.joining(" "));

        String searchable = normalizeForSearch(
            safeString(article.getTitle()) + " " +
            safeString(article.getExcerpt()) + " " +
            safeString(article.getAuthorName()) + " " +
            stripHtml(article.getContentHtml()) + " " +
            safeString(article.getCategory() != null ? article.getCategory().getName() : "") + " " +
            tagNames
        );
        return searchable.contains(query);
    }

    private List<CareerGuideArticleSummaryView> getRelatedArticles(CareerGuideArticle current, int limit) {
        List<CareerGuideArticle> published = articleRepository.findByStatusOrderByPinnedDescFeaturedDescPublishedAtDesc(
            CareerGuideArticle.ArticleStatus.PUBLISHED
        );

        List<CareerGuideArticleSummaryView> sameCategory = published.stream()
            .filter(article -> !Objects.equals(article.getId(), current.getId()))
            .filter(article -> sameCategory(article, current))
            .map(this::toSummaryView)
            .limit(limit)
            .toList();

        if (sameCategory.size() >= limit) {
            return sameCategory;
        }

        Set<Long> seenIds = sameCategory.stream()
            .map(CareerGuideArticleSummaryView::id)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        List<CareerGuideArticleSummaryView> related = new ArrayList<>(sameCategory);
        for (CareerGuideArticle article : published) {
            if (Objects.equals(article.getId(), current.getId())) {
                continue;
            }
            if (sameCategory(article, current)) {
                continue;
            }
            if (seenIds.add(article.getId())) {
                related.add(toSummaryView(article));
            }
            if (related.size() >= limit) {
                break;
            }
        }

        return related.stream().limit(limit).toList();
    }

    private boolean sameCategory(CareerGuideArticle left, CareerGuideArticle current) {
        return left.getCategory() != null
            && current.getCategory() != null
            && Objects.equals(left.getCategory().getId(), current.getCategory().getId());
    }

    private void applyRequest(CareerGuideArticle article, CareerGuideArticleUpsertRequest request, boolean creating) {
        CareerGuideCategory category = requireCategory(request.categoryId());
        String title = normalizeText(request.title());
        String excerpt = normalizeText(request.excerpt());
        String contentHtml = normalizeText(request.contentHtml());

        article.setCategory(category);
        article.setTitle(title);
        article.setExcerpt(excerpt);
        article.setContentHtml(contentHtml);
        article.setCoverImageUrl(StringUtils.hasText(request.coverImageUrl())
            ? request.coverImageUrl().trim()
            : defaultCoverImage(category));
        article.setCoverImageAlt(StringUtils.hasText(request.coverImageAlt())
            ? request.coverImageAlt().trim()
            : title);
        article.setAuthorName(StringUtils.hasText(request.authorName())
            ? request.authorName().trim()
            : DEFAULT_AUTHOR);
        article.setFeatured(Boolean.TRUE.equals(request.featured()));
        article.setPinned(Boolean.TRUE.equals(request.pinned()));
        article.setReadTimeMinutes(normalizeReadTime(request.readTimeMinutes(), contentHtml));
        article.setStatus(parseStatus(request.status()));
        article.setTags(resolveTags(request.tagIds()));

        String requestedSlug = StringUtils.hasText(request.slug()) ? slugify(request.slug()) : null;
        String fallbackSlug = creating || !StringUtils.hasText(article.getSlug()) ? slugify(title) : article.getSlug();
        String candidateSlug = StringUtils.hasText(requestedSlug) ? requestedSlug : fallbackSlug;
        article.setSlug(ensureUniqueSlug(candidateSlug, article.getId()));

        if (article.getStatus() == CareerGuideArticle.ArticleStatus.PUBLISHED) {
            if (article.getPublishedAt() == null) {
                article.setPublishedAt(LocalDateTime.now());
            }
        } else if (creating) {
            article.setPublishedAt(null);
        }

        if (creating && article.getViews() == null) {
            article.setViews(0L);
        }
    }

    private CareerGuideCategory requireCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại"));
    }

    private List<CareerGuideTag> resolveTags(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> requested = tagIds.stream()
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        Map<Long, CareerGuideTag> found = tagRepository.findAllById(requested).stream()
            .collect(Collectors.toMap(CareerGuideTag::getId, tag -> tag, (left, right) -> left, LinkedHashMap::new));

        List<Long> missing = requested.stream()
            .filter(id -> !found.containsKey(id))
            .toList();

        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Một hoặc nhiều tag không tồn tại");
        }

        List<CareerGuideTag> ordered = new ArrayList<>();
        for (Long id : requested) {
            ordered.add(found.get(id));
        }
        return ordered;
    }

    private CareerGuideTagView toTagView(CareerGuideTag tag) {
        return new CareerGuideTagView(tag.getId(), tag.getName(), tag.getSlug());
    }

    private CareerGuideArticleSummaryView toSummaryView(CareerGuideArticle article) {
        CareerGuideCategory category = article.getCategory();
        List<CareerGuideTagView> tags = article.getTags() == null
            ? List.of()
            : article.getTags().stream().map(this::toTagView).toList();

        return new CareerGuideArticleSummaryView(
            article.getId(),
            article.getTitle(),
            article.getSlug(),
            article.getExcerpt(),
            article.getCoverImageUrl(),
            article.getCoverImageAlt(),
            category != null ? category.getId() : null,
            category != null ? category.getName() : null,
            category != null ? category.getSlug() : null,
            category != null ? category.getIcon() : null,
            category != null ? category.getAccentColor() : null,
            article.getAuthorName(),
            article.getPublishedAt(),
            article.getReadTimeMinutes(),
            Boolean.TRUE.equals(article.getFeatured()),
            Boolean.TRUE.equals(article.getPinned()),
            safeViews(article.getViews()),
            article.getStatus() != null ? article.getStatus().name() : null,
            tags
        );
    }

    private int normalizeLimit(Integer limit, int defaultLimit) {
        if (limit == null || limit <= 0) {
            return defaultLimit;
        }
        return Math.min(limit, 50);
    }

    private long safeViews(Long views) {
        return views == null ? 0L : views;
    }

    private Integer normalizeReadTime(Integer readTimeMinutes, String contentHtml) {
        if (readTimeMinutes != null && readTimeMinutes > 0) {
            return readTimeMinutes;
        }

        String plainText = stripHtml(contentHtml);
        if (!StringUtils.hasText(plainText)) {
            return 5;
        }

        int words = plainText.trim().split("\\s+").length;
        return Math.max(3, (int) Math.ceil(words / 220.0));
    }

    private String defaultCoverImage(CareerGuideCategory category) {
        String slug = category != null ? category.getSlug() : "";
        return switch (slug) {
            case "bi-quyet-tim-viec" -> "/images/career-guide/guide-ats.svg";
            case "con-duong-su-nghiep" -> "/images/career-guide/guide-growth.svg";
            case "thi-truong-lao-dong" -> "/images/career-guide/guide-market.svg";
            case "wiki-career" -> "/images/career-guide/guide-wiki.svg";
            case "thu-gian" -> "/images/career-guide/guide-relax.svg";
            case "su-kien-nghe-nghiep" -> "/images/career-guide/guide-event.svg";
            case "goc-doi-tac" -> "/images/career-guide/guide-partner.svg";
            default -> "/images/career-guide/guide-default.svg";
        };
    }

    private CareerGuideArticle.ArticleStatus parseStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return CareerGuideArticle.ArticleStatus.PUBLISHED;
        }

        try {
            return CareerGuideArticle.ArticleStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Trạng thái bài viết không hợp lệ");
        }
    }

    private String ensureUniqueSlug(String baseSlug, Long currentId) {
        String rootSlug = StringUtils.hasText(baseSlug) ? baseSlug : "bai-viet";
        String candidate = rootSlug;
        int suffix = 1;

        while (true) {
            CareerGuideArticle existing = articleRepository.findBySlug(candidate).orElse(null);
            if (existing == null || Objects.equals(existing.getId(), currentId)) {
                return candidate;
            }
            suffix++;
            candidate = rootSlug + "-" + suffix;
        }
    }

    private String slugify(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("(^-|-$)", "");
        return StringUtils.hasText(normalized) ? normalized : "bai-viet";
    }

    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.trim();
    }

    private String normalizeForSearch(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        normalized = DIACRITICS.matcher(normalized).replaceAll("");
        return normalized.toLowerCase(Locale.ROOT);
    }

    private String stripHtml(String html) {
        if (!StringUtils.hasText(html)) {
            return "";
        }
        return html.replaceAll("(?s)<[^>]*>", " ").replaceAll("\\s+", " ").trim();
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }
}
