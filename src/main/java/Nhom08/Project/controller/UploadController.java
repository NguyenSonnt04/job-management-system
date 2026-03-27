package Nhom08.Project.controller;

import Nhom08.Project.entity.User;
import Nhom08.Project.service.AuthService;
import Nhom08.Project.service.FirebaseImageStorageService;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private final FirebaseImageStorageService imageStorageService;
    private final AuthService authService;

    public UploadController(FirebaseImageStorageService imageStorageService, AuthService authService) {
        this.imageStorageService = imageStorageService;
        this.authService = authService;
    }

    @PostMapping("/images")
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "uploads") String folder,
            Authentication auth) {

        Map<String, Object> response = new HashMap<>();
        Optional<User> userOpt = resolveUser(auth);
        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Bạn cần đăng nhập để tải ảnh lên");
            return ResponseEntity.status(401).body(response);
        }

        try {
            String objectPath = imageStorageService.uploadImage(file, folder);
            response.put("success", true);
            response.put("path", objectPath);
            response.put("url", "/api/uploads/image?path=" + encode(objectPath));
            response.put("contentType", file.getContentType());
            response.put("size", file.getSize());
            response.put("message", "Tải ảnh lên thành công");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(503).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Không thể tải ảnh lên lúc này");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/image")
    public ResponseEntity<?> getImage(@RequestParam("path") String path) {
        try {
            FirebaseImageStorageService.StoredImage storedImage = imageStorageService.loadImage(path);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(storedImage.contentType()))
                    .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(storedImage.bytes());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(503).body(response);
        }
    }

    private Optional<User> resolveUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        return authService.findByEmail(auth.getName());
    }

    private String encode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
