package Nhom08.Project.repository;

import Nhom08.Project.entity.ChatbotPrompt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho Chatbot Prompt
 */
public interface ChatbotPromptRepository extends JpaRepository<ChatbotPrompt, Integer> {

    /**
     * Lấy tất cả prompt đang active, sắp theo category và id
     */
    List<ChatbotPrompt> findByIsActiveTrueOrderByCategoryAscIdDesc();

    /**
     * Lấy prompt theo category
     */
    List<ChatbotPrompt> findByCategoryAndIsActiveTrue(String category);

    /**
     * Tìm prompt theo tên
     */
    Optional<ChatbotPrompt> findByPromptName(String promptName);

    /**
     * Tìm all active prompts cho keyword matching
     */
    @Query("SELECT p FROM ChatbotPrompt p WHERE p.isActive = true ORDER BY p.id DESC")
    List<ChatbotPrompt> findAllActiveForMatching();
}
