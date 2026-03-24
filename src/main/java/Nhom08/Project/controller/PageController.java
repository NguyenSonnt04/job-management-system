package Nhom08.Project.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.stereotype.Controller;

import java.nio.charset.StandardCharsets;

import org.springframework.web.util.UriUtils;

@Controller
public class PageController {

    @GetMapping("/login")
    public String loginPage() {
        return "redirect:/candidate-login.html";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "redirect:/candidate-register.html";
    }

    @GetMapping("/403")
    public String accessDenied() {
        return "403";
    }

    /**
     * Trang đăng tuyển dụng
     */
    @GetMapping("/quan-ly-dang-tuyen/post-job")
    public String postJob() {
        return "redirect:/post-job.html";
    }

    /**
     * Trang quản lý đăng tuyển
     */
    @GetMapping("/quan-ly-dang-tuyen")
    public String manageJobs() {
        return "redirect:/quan-ly-dang-tuyen.html";
    }

    /**
     * Trang quản lý ứng viên
     */
    @GetMapping("/quan-ly-ung-vien")
    public String manageCandidates() {
        return "redirect:/quan-ly-ung-vien.html";
    }

    /**
     * Trang cẩm nang
     */
    @GetMapping("/cam-nang")
    public String careerGuideHome() {
        return "redirect:/cam-nang.html";
    }

    /**
     * Trang chi tiết cẩm nang
     */
    @GetMapping("/cam-nang/{slug}")
    public String careerGuideDetail(@PathVariable String slug) {
        String encodedSlug = UriUtils.encodePathSegment(slug, StandardCharsets.UTF_8);
        return "redirect:/cam-nang-chi-tiet.html?slug=" + encodedSlug;
    }
}
