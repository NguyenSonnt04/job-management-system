package Nhom08.Project.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@EnableConfigurationProperties(FirebaseStorageProperties.class)
public class FirebaseStorageConfig {

    private static final String FIREBASE_APP_NAME = "firebase-storage";

    @Bean(name = FIREBASE_APP_NAME)
    @ConditionalOnProperty(prefix = "firebase.storage", name = "enabled", havingValue = "true")
    public FirebaseApp firebaseApp(
            FirebaseStorageProperties properties,
            ResourceLoader resourceLoader) throws IOException {

        if (!StringUtils.hasText(properties.getBucket())) {
            throw new IllegalStateException("firebase.storage.bucket chưa được cấu hình");
        }
        if (!StringUtils.hasText(properties.getServiceAccountPath())) {
            throw new IllegalStateException("firebase.storage.service-account-path chưa được cấu hình");
        }

        for (FirebaseApp app : FirebaseApp.getApps()) {
            if (FIREBASE_APP_NAME.equals(app.getName())) {
                return app;
            }
        }

        Resource resource = resourceLoader.getResource(properties.getServiceAccountPath());
        if (!resource.exists()) {
            throw new IllegalStateException(
                    "Không tìm thấy file service account Firebase tại " + properties.getServiceAccountPath());
        }

        try (InputStream inputStream = resource.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(inputStream))
                    .setStorageBucket(properties.getBucket())
                    .build();

            return FirebaseApp.initializeApp(options, FIREBASE_APP_NAME);
        }
    }
}
