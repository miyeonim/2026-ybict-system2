package kepco.prorject.ictyb.back.ictyb_back.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS(Cross-Origin Resource Sharing) 설정
 * 프론트엔드(다른 포트/도메인)에서 백엔드 API를 호출할 수 있도록 허용하는 설정
 * CORS란? 브라우저가 다른 출처(포트, 도메인)의 API 호출을 기본적으로 막는 보안 정책
 */
@Configuration
public class CorsConfig {
    
    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {

        CorsConfiguration config = new CorsConfiguration();

        // 허용할 프론트엔드 주소 (docker-compose에서 프론트가 8080 포트)
        //config.addAllowedOrigin("http://localhost:8080");
        //config.addAllowedOrigin("http://localhost:5173");


        for (String origin : allowedOrigins) {
            config.addAllowedOrigin(origin.trim());
        }


        // 모든 HTTP 메서드 허용 (GET, POST, PUT, DELETE 등)
        config.addAllowedMethod("*");

        // 모든 헤더 허용 (Content-Type, Authorization 등)
        config.addAllowedHeader("*");

        // JWT 토큰을 응답 헤더로 전달할 경우 프론트에서 읽을 수 있도록 허용
        config.addExposedHeader("Authorization");

        // 쿠키/인증 정보 포함 요청 허용 (JWT refresh token 등 사용 시 필요)
        config.setAllowCredentials(true);

        // 위 설정을 모든 경로(/api/** 등)에 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}