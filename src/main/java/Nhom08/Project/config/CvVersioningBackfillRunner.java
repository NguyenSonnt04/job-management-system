package Nhom08.Project.config;

import Nhom08.Project.service.CvVersioningService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class CvVersioningBackfillRunner implements ApplicationRunner {

    private final CvVersioningService cvVersioningService;

    public CvVersioningBackfillRunner(CvVersioningService cvVersioningService) {
        this.cvVersioningService = cvVersioningService;
    }

    @Override
    public void run(ApplicationArguments args) {
        cvVersioningService.backfillMissingVersions();
    }
}
