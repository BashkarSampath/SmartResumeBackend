package centennial.comp231.smartresumebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories("centennial.comp231.smartresumebackend.repos")
@ComponentScan(basePackages = { "centennial.comp231.smartresumebackend.*" })
@EntityScan("centennial.comp231.smartresumebackend.*")  
@SpringBootApplication
public class SmartResumeBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartResumeBackendApplication.class, args);
	}

}

