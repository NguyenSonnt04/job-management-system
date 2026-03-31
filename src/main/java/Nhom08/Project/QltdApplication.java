package Nhom08.Project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class QltdApplication {

	public static void main(String[] args) {
		SpringApplication.run(QltdApplication.class, args);
	}

}
