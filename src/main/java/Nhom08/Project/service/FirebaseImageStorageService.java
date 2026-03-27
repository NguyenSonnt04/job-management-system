package Nhom08.Project.service;

import Nhom08.Project.config.FirebaseStorageProperties;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.StorageClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Service
public class FirebaseImageStorageService {

    private static final DateTimeFormatter DATE_SEGMENT = DateTimeFormatter.ofPattern("yyyy/MM");

    private final FirebaseStorageProperties properties;
    private final ObjectProvider<FirebaseApp> firebaseAppProvider;

    public FirebaseImageStorageService(
            FirebaseStorageProperties properties,
            @Qualifier("firebase-storage") ObjectProvider<FirebaseApp> firebaseAppProvider) {
        this.properties = properties;
        this.firebaseAppProvider = firebaseAppProvider;
    }

    public String uploadImage(MultipartFile file, String folder) throws IOException {
        validateEnabled();

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Chưa có ảnh được gửi lên");
        }

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("Chỉ hỗ trợ upload file ảnh");
        }

        if (file.getSize() > properties.getMaxImageSizeBytes()) {
            throw new IllegalArgumentException("Ảnh vượt quá dung lượng cho phép");
        }

        Bucket bucket = StorageClient.getInstance(firebaseAppProvider.getObject()).bucket();
        String objectName = buildObjectName(folder, file.getOriginalFilename(), contentType);

        BlobInfo blobInfo = BlobInfo.newBuilder(bucket.getName(), objectName)
                .setContentType(contentType)
                .setCacheControl("public, max-age=31536000")
                .build();

        bucket.getStorage().create(blobInfo, file.getBytes());
        return objectName;
    }

    public StoredImage loadImage(String objectName) {
        validateEnabled();

        if (!StringUtils.hasText(objectName)) {
            throw new IllegalArgumentException("Thiếu đường dẫn ảnh");
        }

        Bucket bucket = StorageClient.getInstance(firebaseAppProvider.getObject()).bucket();
        Blob blob = bucket.get(objectName);
        if (blob == null || !blob.exists()) {
            throw new IllegalArgumentException("Không tìm thấy ảnh");
        }

        String contentType = blob.getContentType();
        if (!StringUtils.hasText(contentType)) {
            contentType = "application/octet-stream";
        }

        return new StoredImage(blob.getContent(), contentType);
    }

    private void validateEnabled() {
        if (!properties.isEnabled() || firebaseAppProvider.getIfAvailable() == null) {
            throw new IllegalStateException("Firebase Storage chưa được cấu hình");
        }
    }

    private String buildObjectName(String folder, String originalFilename, String contentType) {
        String normalizedFolder = StringUtils.hasText(folder)
                ? folder.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9/_-]", "-")
                : "uploads";
        normalizedFolder = normalizedFolder.replaceAll("/+", "/").replaceAll("^/|/$", "");
        if (!StringUtils.hasText(normalizedFolder)) {
            normalizedFolder = "uploads";
        }

        String extension = resolveExtension(originalFilename, contentType);
        return normalizedFolder + "/"
                + LocalDate.now().format(DATE_SEGMENT) + "/"
                + UUID.randomUUID() + extension;
    }

    private String resolveExtension(String originalFilename, String contentType) {
        if (StringUtils.hasText(originalFilename)) {
            String fileName = originalFilename.replace('\\', '/');
            int slashIndex = fileName.lastIndexOf('/');
            if (slashIndex >= 0) {
                fileName = fileName.substring(slashIndex + 1);
            }
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex >= 0) {
                String ext = fileName.substring(dotIndex).toLowerCase(Locale.ROOT);
                if (ext.matches("\\.[a-z0-9]{1,10}")) {
                    return ext;
                }
            }
        }

        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            case "image/svg+xml" -> ".svg";
            default -> ".img";
        };
    }

    public record StoredImage(byte[] bytes, String contentType) {
    }
}
