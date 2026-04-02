package Nhom08.Project.repository;

import Nhom08.Project.entity.InterviewQuestionBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewQuestionBankRepository extends JpaRepository<InterviewQuestionBank, Long> {

    List<InterviewQuestionBank> findByActiveTrueOrderByDisplayOrderAsc();

    List<InterviewQuestionBank> findByTypeKeyAndActiveTrueOrderByDisplayOrderAsc(String typeKey);

    /**
     * Lấy câu hỏi theo typeKey và roleKey (bao gồm câu hỏi chung role_key IS NULL).
     */
    @Query("SELECT q FROM InterviewQuestionBank q " +
           "WHERE q.active = true " +
           "AND q.typeKey = :typeKey " +
           "AND (q.roleKey = :roleKey OR q.roleKey IS NULL) " +
           "ORDER BY q.displayOrder ASC")
    List<InterviewQuestionBank> findByTypeKeyAndRoleKey(
            @Param("typeKey") String typeKey,
            @Param("roleKey") String roleKey);

    /**
     * Lấy ngẫu nhiên N câu hỏi theo typeKey và roleKey.
     */
    @Query(value = "SELECT * FROM interview_question_bank " +
                   "WHERE active = 1 AND type_key = :typeKey " +
                   "AND (role_key = :roleKey OR role_key IS NULL) " +
                   "ORDER BY RAND() LIMIT :limit",
           nativeQuery = true)
    List<InterviewQuestionBank> findRandomByTypeAndRole(
            @Param("typeKey") String typeKey,
            @Param("roleKey") String roleKey,
            @Param("limit") int limit);

    List<InterviewQuestionBank> findByCategoryAndActiveTrueOrderByDisplayOrderAsc(String category);
}
