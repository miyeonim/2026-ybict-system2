package kepco.prorject.ictyb.back.ictyb_back.jwt.service;

import kepco.prorject.ictyb.back.ictyb_back.jwt.model.LoginRequest;
import kepco.prorject.ictyb.back.ictyb_back.jwt.model.LoginResponse;

public interface JwtService {
/**
     * 사용자 인증을 처리하고 토큰을 반환합니다.
     * * @param request 사번, 비밀번호 또는 리프레시 토큰이 포함된 요청 객체
     * @return 로그인 성공 여부와 토큰 정보가 담긴 응답 객체
     */
    LoginResponse authenticate(LoginRequest request);

    //새로고침, 재접속시 token을 활용한 개인 정보 가져오기 
    LoginResponse getUserInfoFromToken(String token);

}
