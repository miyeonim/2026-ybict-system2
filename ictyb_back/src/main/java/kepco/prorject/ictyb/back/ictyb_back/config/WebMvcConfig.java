package kepco.prorject.ictyb.back.ictyb_back.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // 여기서 ObjectMapper를 빈으로 등록하면
    // 컨트롤러에서 @RequiredArgsConstructor를 통해 주입받을 수 있사옵니다.
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    // 다른 웹 설정(CORS, 인터셉터 등)이 있다면 여기에 추가하시면 되옵니다.
}