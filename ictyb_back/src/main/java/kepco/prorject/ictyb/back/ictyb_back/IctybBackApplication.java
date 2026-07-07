package kepco.prorject.ictyb.back.ictyb_back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@ComponentScan(basePackages = "kepco.prorject.ictyb.back.ictyb_back")
@EnableJpaAuditing
public class IctybBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(IctybBackApplication.class, args);
	}

}
