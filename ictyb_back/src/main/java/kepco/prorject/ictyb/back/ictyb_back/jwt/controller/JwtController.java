package kepco.prorject.ictyb.back.ictyb_back.jwt.controller;

import kepco.prorject.ictyb.back.ictyb_back.jwt.model.LoginRequest;
import kepco.prorject.ictyb.back.ictyb_back.jwt.model.LoginResponse;
import kepco.prorject.ictyb.back.ictyb_back.jwt.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse; // 이 패키지가 꼭 필요하옵니다.

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Tag(name = "인증 API", description = "로그인 관련 API")
@RestController
@RequestMapping("/api/auth")
public class JwtController {

    @Autowired
    private JwtService jwtService;

    @Operation(summary = "로그인", description = "사번과 비밀번호로 로그인을 수행합니다.")
    @PostMapping("/v1.0/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request, 
            HttpServletResponse response) { // 1. 응답 객체를 파라미터로 추가하소서

        System.out.println("백단 호출");

        LoginResponse result = jwtService.authenticate(request);

        if (result.isSuccess()) {
            // 2. 쿠키 생성
            Cookie cookie = new Cookie("token", result.getAccessToken());
            cookie.setHttpOnly(true); // 자바스크립트에서 접근 불가 (보안)
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60 * 24); // 1일
            response.addCookie(cookie); // 3. 응답에 쿠키 추가
        }
    
        return ResponseEntity.ok(result); // 4. 성공 응답 반환
    }


    // ──────────────────────────────────────────
    // 세션 확인 (/me)
    // 🔒 핵심: 토큰 없음 or 만료 시 200이 아닌 401을 반환해야
    //          프론트에서 무한 루프 없이 "인증 안 됨"으로 처리 가능합니다.
    //          (200 + success:false 반환 시 프론트가 계속 재시도할 수 있음)
    // ──────────────────────────────────────────
    @Operation(summary = "로그인 정보 조회", description = "쿠키의 토큰으로부터 사용자 정보를 반환합니다.")
    @GetMapping("/v1.0/me")
    public ResponseEntity<LoginResponse> me(
            @CookieValue(value = "token", required = false) String token) {

        // 1. 쿠키 자체가 없음 → 401
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, null, null, "토큰이 없습니다.", null));
        }

        LoginResponse result = jwtService.getUserInfoFromToken(token);

        // 2. 토큰 파싱 실패(만료, 변조 등) → 401 등 reuslt에 새로운 값을 넣어서 보냄 (무한로프 막기 위함)
        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        // 3. 정상
        return ResponseEntity.ok(result);
    }



    @Operation(summary = "로그아웃", description = "로그아웃 처리 후 쿠키를 만료시킵니다.")
    @PostMapping("/v1.0/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie tokenCookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, tokenCookie.toString());

        return ResponseEntity.ok(Map.of("success", true, "message", "로그아웃되었습니다."));
    }

}