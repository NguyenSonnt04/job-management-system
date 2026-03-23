package Nhom08.Project.repository;

import Nhom08.Project.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository cho Login History
 */
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Integer> {

    /**
     * Lấy lịch sử đăng nhập theo username, mới nhất trước
     */
    List<LoginHistory> findByUsernameOrderByLoginTimeDesc(String username);

    /**
     * Lấy lịch sử đăng nhập theo IP
     */
    List<LoginHistory> findByIpAddressOrderByLoginTimeDesc(String ipAddress);

    /**
     * Lấy lịch sử đăng nhập trong khoảng thời gian
     */
    List<LoginHistory> findByLoginTimeBetweenOrderByLoginTimeDesc(LocalDateTime start, LocalDateTime end);

    /**
     * Đếm số lần đăng nhập thất bại của một username trong khoảng thời gian
     */
    long countByUsernameAndStatusAndLoginTimeAfter(String username, String status, LocalDateTime after);

    /**
     * Lấy lần đăng nhập gần nhất của username
     */
    LoginHistory findFirstByUsernameOrderByLoginTimeDesc(String username);
}
