package kepco.prorject.ictyb.back.ictyb_back.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import kepco.prorject.ictyb.back.ictyb_back.jwt.model.JwtUserDto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;

import javax.crypto.SecretKey;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    // SonarQube 경고를 해결하기 위해 사용할 Key 객체
    private SecretKey key;

    private final long ACCESS_TOKEN_VALID_TIME = 30 * 60 * 1000L;          // 30분
    private final long REFRESH_TOKEN_VALID_TIME = 24 * 60 * 60 * 1000L;    // 1일

    /**
     * 의존성 주입이 완료된 후, 문자열 Key를 SecretKey 객체로 변환합니다.
     */
    @PostConstruct
    protected void init() {
        // 기존 signWith에서 내부적으로 처리하던 Base64 디코딩을 명시적으로 수행합니다.
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }


    /**
     * Access Token 생성 (Login ID가 곧 사원번호이므로 하나만 받음)
     */
    public String createAccessToken(JwtUserDto dto) {
        Date now = new Date();

        Claims claims = Jwts.claims().setSubject(dto.getUserEmpno());            //사번
        claims.put("empNm", dto.getEmpNm());                                //이름
        claims.put("depId", dto.getDepId());                                //부서ID
        claims.put("parDepId", dto.getParDepId());                          //처명
        claims.put("depTitle", dto.getDepTitle());                          //부서명
        claims.put("kepcoMap", dto.getKepcoMap());                          //본사여부
        
        
        return Jwts.builder()
                .setClaims(claims) // 토큰의 주체로 loginId(사원번호) 설정
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_VALID_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Refresh Token 생성
     */
    public String createRefreshToken() {
        Date now = new Date();
        return Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + REFRESH_TOKEN_VALID_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 🌟 토큰에서 로그인한 정보 JwtUserDto(loginId, empNm) 추출
     */
    public JwtUserDto getUserInfo(String token) {
        Claims claims = parseClaims(token);

        return new JwtUserDto(
            claims.get("depId", String.class),      // 부서ID
            claims.get("parDepId", String.class),   // 처명
            claims.get("depTitle", String.class),   // 부서명
            claims.get("kepcoMap", String.class),   // 본사여부
            claims.getSubject(),                                            // 사번
            claims.get("empNm", String.class)       // 이름
      );

    }
    
}