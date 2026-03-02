package Nhom08.Project.config;

import Nhom08.Project.entity.NotificationTemplate;
import Nhom08.Project.repository.NotificationTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Seed du lieu mac dinh cho notification templates khi server khoi dong.
 * Chi insert neu key chua ton tai trong DB.
 * De thay doi noi dung: UPDATE truc tiep trong DB, khong can restart.
 */
@Component
public class NotificationTemplateSeeder implements ApplicationRunner {

    @Autowired
    private NotificationTemplateRepository templateRepo;

    @Override
    public void run(ApplicationArguments args) {
        seedIfAbsent(
            "INTERVIEW_TITLE",
            "B\u1ea1n \u0111\u01b0\u1ee3c m\u1eddi ph\u1ecfng v\u1ea5n!",
            "Tieu de thong bao moi phong van. Placeholder: {jobTitle}"
        );
        seedIfAbsent(
            "INTERVIEW_MESSAGE",
            "Ch\u00fac m\u1eebng! B\u1ea1n \u0111\u00e3 \u0111\u01b0\u1ee3c m\u1eddi ph\u1ecfng v\u1ea5n cho v\u1ecb tr\u00ed '{jobTitle}'. Nh\u00e0 tuy\u1ec3n d\u1ee5ng s\u1ebd li\u00ean h\u1ec7 v\u1edbi b\u1ea1n s\u1edbm!",
            "Noi dung thong bao moi phong van. Placeholder: {jobTitle}"
        );
    }

    private void seedIfAbsent(String key, String value, String description) {
        if (templateRepo.findByKey(key).isEmpty()) {
            NotificationTemplate t = new NotificationTemplate();
            t.setKey(key);
            t.setValue(value);
            t.setDescription(description);
            templateRepo.save(t);
        }
    }
}
