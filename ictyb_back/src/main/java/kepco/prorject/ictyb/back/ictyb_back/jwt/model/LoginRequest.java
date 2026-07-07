package kepco.prorject.ictyb.back.ictyb_back.jwt.model;

import lombok.Data;

@Data
public class LoginRequest {
    private String userEmpno;     // 🌟 사번 (222142)
    private String password;      // 🌟 비밀번호 (222142)
    private String accessToken;   // 🌟 세션 연장 요청 시 검증할 기존 엑세스 토큰
    private String refreshToken;  // 🌟 로그인을 안 해도 유지해 줄 장기 리프레시 토큰
}