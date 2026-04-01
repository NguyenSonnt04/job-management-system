package Nhom08.Project.controller;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

@RestControllerAdvice(assignableTypes = {
        AdminHeroBannerController.class,
        AdminTopEmployerLogoController.class,
        JobController.class,
        UploadController.class
})
public class UploadExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatusCode.valueOf(413)).body(Map.of(
                "success", false,
                "message", "Ảnh vượt quá dung lượng cho phép 15MB"
        ));
    }
}
