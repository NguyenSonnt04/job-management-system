package Nhom08.Project.service;

import Nhom08.Project.dto.*;
import Nhom08.Project.entity.CareerPath;
import Nhom08.Project.entity.CareerPathSkill;
import Nhom08.Project.entity.CareerPathStage;
import Nhom08.Project.repository.CareerPathRepository;
import Nhom08.Project.repository.CareerPathSkillRepository;
import Nhom08.Project.repository.CareerPathStageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CareerPathService {

    private static final int DEFAULT_FEATURED_LIMIT = 6;
    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private final CareerPathRepository careerPathRepository;
    private final CareerPathStageRepository stageRepository;
    private final CareerPathSkillRepository skillRepository;

    public CareerPathService(
            CareerPathRepository careerPathRepository,
            CareerPathStageRepository stageRepository,
            CareerPathSkillRepository skillRepository
    ) {
        this.careerPathRepository = careerPathRepository;
        this.stageRepository = stageRepository;
        this.skillRepository = skillRepository;
    }

    @Transactional(readOnly = true)
    public CareerPathHomeView getHomeView() {
        List<CareerPath> featured = careerPathRepository.findByActiveTrueAndFeaturedTrueOrderByDisplayOrderAscTitleAsc();
        List<CareerPath> allPaths = careerPathRepository.findByActiveTrueOrderByDisplayOrderAscTitleAsc();

        Set<String> industries = allPaths.stream()
                .map(CareerPath::getIndustryField)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new CareerPathHomeView(
                featured.stream().limit(DEFAULT_FEATURED_LIMIT).map(this::toSummaryView).toList(),
                allPaths.stream().map(this::toSummaryView).toList(),
                new ArrayList<>(industries)
        );
    }

    @Transactional(readOnly = true)
    public List<CareerPathSummaryView> getAllPaths() {
        return careerPathRepository.findByActiveTrueOrderByDisplayOrderAscTitleAsc().stream()
                .map(this::toSummaryView)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CareerPathSummaryView> getFeaturedPaths() {
        return careerPathRepository.findByActiveTrueAndFeaturedTrueOrderByDisplayOrderAscTitleAsc().stream()
                .limit(DEFAULT_FEATURED_LIMIT)
                .map(this::toSummaryView)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CareerPathSummaryView> getPathsByIndustry(String industry) {
        return careerPathRepository.findByIndustryField(industry).stream()
                .filter(path -> Boolean.TRUE.equals(path.getActive()))
                .map(this::toSummaryView)
                .toList();
    }

    @Transactional
    public CareerPathDetailView getPathDetail(String slug) {
        CareerPath path = careerPathRepository.findBySlug(slug)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy lộ trình sự nghiệp"));

        path.setViews(path.getViews() + 1);
        careerPathRepository.save(path);

        List<CareerPathStage> stages = stageRepository.findByCareerPathIdOrderByStageOrderAsc(path.getId());

        return new CareerPathDetailView(
                path.getId(),
                path.getTitle(),
                path.getSlug(),
                path.getDescription(),
                path.getIconUrl(),
                path.getAccentColor(),
                path.getIndustryField(),
                path.getAverageSalaryMin(),
                path.getAverageSalaryMax(),
                path.getTotalDurationMonths(),
                path.getFeatured(),
                path.getViews(),
                stages.stream().map(this::toStageView).toList()
        );
    }

    private CareerPathSummaryView toSummaryView(CareerPath path) {
        int stageCount = stageRepository.findByCareerPathIdOrderByStageOrderAsc(path.getId()).size();

        return new CareerPathSummaryView(
                path.getId(),
                path.getTitle(),
                path.getSlug(),
                path.getDescription(),
                path.getIconUrl(),
                path.getAccentColor(),
                path.getIndustryField(),
                path.getAverageSalaryMin(),
                path.getAverageSalaryMax(),
                path.getTotalDurationMonths(),
                stageCount,
                path.getFeatured(),
                path.getViews()
        );
    }

    private CareerPathStageView toStageView(CareerPathStage stage) {
        List<CareerPathSkill> skills = skillRepository.findByStageIdOrderBySkillOrderAsc(stage.getId());

        return new CareerPathStageView(
                stage.getId(),
                stage.getStageOrder(),
                stage.getTitle(),
                stage.getDescription(),
                stage.getJobTitle(),
                stage.getExperienceLevel(),
                stage.getDurationMonths(),
                stage.getSalaryMin(),
                stage.getSalaryMax(),
                stage.getIconName(),
                stage.getIconColor(),
                skills.stream().map(this::toSkillView).toList()
        );
    }

    private CareerPathSkillView toSkillView(CareerPathSkill skill) {
        return new CareerPathSkillView(
                skill.getId(),
                skill.getSkillOrder(),
                skill.getName(),
                skill.getCategory(),
                skill.getIsRequired(),
                skill.getProficiencyLevel(),
                skill.getDescription()
        );
    }

    private String slugify(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return normalized.isEmpty() ? "lo-trinh" : normalized;
    }
}
