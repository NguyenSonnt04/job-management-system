package Nhom08.Project.service;

import Nhom08.Project.entity.CvTemplate;
import Nhom08.Project.entity.CvTemplateVersion;
import Nhom08.Project.entity.UserCv;
import Nhom08.Project.entity.UserCvVersion;
import Nhom08.Project.repository.CvTemplateRepository;
import Nhom08.Project.repository.CvTemplateVersionRepository;
import Nhom08.Project.repository.UserCvRepository;
import Nhom08.Project.repository.UserCvVersionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class CvVersioningService {

    private final CvTemplateRepository cvTemplateRepository;
    private final CvTemplateVersionRepository cvTemplateVersionRepository;
    private final UserCvRepository userCvRepository;
    private final UserCvVersionRepository userCvVersionRepository;

    public CvVersioningService(
            CvTemplateRepository cvTemplateRepository,
            CvTemplateVersionRepository cvTemplateVersionRepository,
            UserCvRepository userCvRepository,
            UserCvVersionRepository userCvVersionRepository) {
        this.cvTemplateRepository = cvTemplateRepository;
        this.cvTemplateVersionRepository = cvTemplateVersionRepository;
        this.userCvRepository = userCvRepository;
        this.userCvVersionRepository = userCvVersionRepository;
    }

    @Transactional
    public CvTemplate createTemplate(CvTemplate template, String changeNote) {
        template.setLatestVersionNo(1);
        CvTemplate saved = cvTemplateRepository.save(template);
        createTemplateVersion(saved, 1, saved.getTemplateContent(), changeNote);
        return saved;
    }

    @Transactional
    public CvTemplate updateTemplate(CvTemplate template, String previousContent, String changeNote) {
        boolean hasVersions = cvTemplateVersionRepository.existsByTemplateId(template.getId());
        int latestVersionNo = resolveLatestTemplateVersionNo(template);

        if (!hasVersions) {
            createTemplateVersion(template, latestVersionNo, defaultString(previousContent), "Backfilled initial template version");
        }

        boolean contentChanged = !Objects.equals(defaultString(previousContent), defaultString(template.getTemplateContent()));
        if (contentChanged) {
            latestVersionNo += 1;
            template.setLatestVersionNo(latestVersionNo);
        } else {
            template.setLatestVersionNo(latestVersionNo);
        }

        CvTemplate saved = cvTemplateRepository.save(template);

        if (contentChanged) {
            createTemplateVersion(saved, latestVersionNo, saved.getTemplateContent(), changeNote);
        }

        return saved;
    }

    @Transactional
    public CvTemplate upsertSeedTemplate(CvTemplate incoming) {
        return cvTemplateRepository.findByName(incoming.getName())
            .map(existing -> {
                String previousContent = existing.getTemplateContent();
                existing.setDescription(incoming.getDescription());
                existing.setPreviewColor(incoming.getPreviewColor());
                existing.setBadgeLabel(incoming.getBadgeLabel());
                existing.setBadgeBgColor(incoming.getBadgeBgColor());
                existing.setBadgeTextColor(incoming.getBadgeTextColor());
                existing.setCategory(incoming.getCategory());
                existing.setStyleTag(incoming.getStyleTag());
                existing.setSortOrder(incoming.getSortOrder());
                existing.setTemplateContent(incoming.getTemplateContent());
                return updateTemplate(existing, previousContent, "Seeder synced template content");
            })
            .orElseGet(() -> createTemplate(incoming, "Seeder created initial template version"));
    }

    @Transactional
    public UserCv createUserCv(UserCv cv, String savedBy) {
        populateSourceTemplateVersion(cv);
        cv.setCurrentVersionNo(1);
        UserCv saved = userCvRepository.save(cv);
        createUserCvVersion(saved, 1, saved.getCvName(), saved.getCvContent(), savedBy);
        return saved;
    }

    @Transactional
    public UserCv updateUserCv(UserCv cv, String previousName, String previousContent, String savedBy) {
        populateSourceTemplateVersion(cv);
        boolean hasVersions = userCvVersionRepository.existsByUserCvId(cv.getId());
        int latestVersionNo = resolveLatestUserCvVersionNo(cv);

        if (!hasVersions) {
            createUserCvVersion(cv, latestVersionNo, previousName, defaultString(previousContent), "SYSTEM");
        }

        boolean contentChanged = !Objects.equals(defaultString(previousContent), defaultString(cv.getCvContent()));
        boolean nameChanged = !Objects.equals(defaultString(previousName), defaultString(cv.getCvName()));

        if (contentChanged || nameChanged) {
            latestVersionNo += 1;
            cv.setCurrentVersionNo(latestVersionNo);
        } else {
            cv.setCurrentVersionNo(latestVersionNo);
        }

        UserCv saved = userCvRepository.save(cv);

        if (contentChanged || nameChanged) {
            createUserCvVersion(saved, latestVersionNo, saved.getCvName(), saved.getCvContent(), savedBy);
        }

        return saved;
    }

    @Transactional
    public void backfillMissingVersions() {
        List<CvTemplate> templates = cvTemplateRepository.findAll();
        for (CvTemplate template : templates) {
            if (!cvTemplateVersionRepository.existsByTemplateId(template.getId())) {
                int versionNo = template.getLatestVersionNo() != null && template.getLatestVersionNo() > 0
                    ? template.getLatestVersionNo()
                    : 1;
                template.setLatestVersionNo(versionNo);
                cvTemplateRepository.save(template);
                createTemplateVersion(template, versionNo, template.getTemplateContent(), "Backfilled initial template version");
            }
        }

        List<UserCv> userCvs = userCvRepository.findAll();
        for (UserCv userCv : userCvs) {
            populateSourceTemplateVersion(userCv);
            if (!userCvVersionRepository.existsByUserCvId(userCv.getId())) {
                int versionNo = userCv.getCurrentVersionNo() != null && userCv.getCurrentVersionNo() > 0
                    ? userCv.getCurrentVersionNo()
                    : 1;
                userCv.setCurrentVersionNo(versionNo);
                userCvRepository.save(userCv);
                createUserCvVersion(userCv, versionNo, userCv.getCvName(), userCv.getCvContent(), "SYSTEM");
            }
        }
    }

    private void populateSourceTemplateVersion(UserCv cv) {
        if (cv.getTemplateId() == null || cv.getSourceTemplateVersionNo() != null) {
            return;
        }

        cvTemplateRepository.findById(cv.getTemplateId()).ifPresent(template -> {
            int versionNo = template.getLatestVersionNo() != null && template.getLatestVersionNo() > 0
                ? template.getLatestVersionNo()
                : 1;
            cv.setSourceTemplateVersionNo(versionNo);
            if (cv.getTemplateName() == null || cv.getTemplateName().isBlank()) {
                cv.setTemplateName(template.getName());
            }
        });
    }

    private int resolveLatestTemplateVersionNo(CvTemplate template) {
        if (template.getLatestVersionNo() != null && template.getLatestVersionNo() > 0) {
            return template.getLatestVersionNo();
        }
        return cvTemplateVersionRepository.findTopByTemplateIdOrderByVersionNoDesc(template.getId())
            .map(CvTemplateVersion::getVersionNo)
            .orElse(1);
    }

    private int resolveLatestUserCvVersionNo(UserCv userCv) {
        if (userCv.getCurrentVersionNo() != null && userCv.getCurrentVersionNo() > 0) {
            return userCv.getCurrentVersionNo();
        }
        return userCvVersionRepository.findTopByUserCvIdOrderByVersionNoDesc(userCv.getId())
            .map(UserCvVersion::getVersionNo)
            .orElse(1);
    }

    private void createTemplateVersion(CvTemplate template, int versionNo, String content, String changeNote) {
        CvTemplateVersion version = new CvTemplateVersion();
        version.setTemplate(template);
        version.setVersionNo(versionNo);
        version.setTemplateContent(defaultString(content));
        version.setChangeNote(changeNote);
        cvTemplateVersionRepository.save(version);
    }

    private void createUserCvVersion(UserCv userCv, int versionNo, String cvName, String cvContent, String savedBy) {
        UserCvVersion version = new UserCvVersion();
        version.setUserCv(userCv);
        version.setVersionNo(versionNo);
        version.setCvName(cvName);
        version.setCvContent(defaultString(cvContent));
        version.setSavedBy(savedBy != null && !savedBy.isBlank() ? savedBy : "USER");
        userCvVersionRepository.save(version);
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
