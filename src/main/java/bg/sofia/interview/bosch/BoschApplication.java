package bg.sofia.interview.bosch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class BoschApplication {

	public static void main(String[] args) {
		SpringApplication.run(BoschApplication.class, args);
	}

}
