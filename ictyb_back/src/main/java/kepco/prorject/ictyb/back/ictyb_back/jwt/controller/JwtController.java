package kepco.prorject.ictyb.back.ictyb_back.jwt.controller;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.KepcoUserVo;
import kepco.prorject.ictyb.back.ictyb_back.jwt.model.LoginRequest;
import kepco.prorject.ictyb.back.ictyb_back.jwt.model.LoginResponse;
import kepco.prorject.ictyb.back.ictyb_back.jwt.service.JwtService;
import kepco.prorject.ictyb.back.ictyb_back.jwt.service.SsoUtil;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.KepcoUserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse; // 이 패키지가 꼭 필요하옵니다.

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.Optional;

@Tag(name = "인증 API", description = "로그인 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class JwtController {

    @Autowired
    private JwtService jwtService;

    private final KepcoUserRepository kepcoUserRepository;


    @Operation(summary = "SSO로 자동 로그인 구현", description = "sso로 확인합니다.")
    @PostMapping("/v1.0/sso-login")
    public ResponseEntity<LoginResponse> ssoLogin(HttpServletRequest request, HttpServletResponse response) {
        //String userEmpno = SsoUtil.getUserId(request); // 쿠키(pgsecuid) 복호화해서 사번 추출 -> 향후 SSO 관련해서 추가할껏
        String userEmpno = null;
        //String userEmpno = "19109330"; //temp

        if (userEmpno == null || userEmpno.isEmpty()) {
            return ResponseEntity.ok(new LoginResponse(false, null, null, "SSO 정보 없음", null));
        }

        // its_kepco_user는 [2026-07-23] 전부 10자리 사번으로 통일됨 - SSO에서 오는 8자리 userEmpno와
        // 정확매칭(findBySabun)하면 항상 못 찾으므로 끝자리 매칭(findFirstBySabunEndingWith)을 쓴다.
        Optional<KepcoUserVo> kepco_user = kepcoUserRepository.findFirstBySabunEndingWith(userEmpno);
        System.out.println("kepco_user2 :::" + kepco_user.toString());
        if (kepco_user == null) {
            System.out.println("SSO 사번 확인됨 그러나 한전 사용자 아님 :: {"+userEmpno+"}");
            return ResponseEntity.ok(new LoginResponse(false, null, null, "한전 사용자가 아닙니다.", null));
        }

        return ResponseEntity.ok(jwtService.loginByEmpno(userEmpno, request, response));
    }


    @Operation(summary = "로그인", description = "사번과 비밀번호로 로그인을 수행합니다.")
    @PostMapping("/v1.0/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request, 
            HttpServletResponse response) { // 1. 응답 객체를 파라미터로 추가하소서

        System.out.println("백단 호출");

        LoginResponse result = jwtService.authenticate(request);

        if (result.isSuccess()) {
            // 2. 쿠키 생성
            Cookie cookie = new Cookie("accessToken", result.getAccessToken());
            cookie.setHttpOnly(true); // 자바스크립트에서 접근 불가 (보안)
            cookie.setSecure(true);     //https token 
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60);  // 1시간
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
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        // 1. 쿠키 자체가 없음 → 401
        if (accessToken == null || accessToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, null, null, "토큰이 없습니다.", null));
        }

        LoginResponse result = jwtService.getUserInfoFromToken(accessToken);

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
        ResponseCookie tokenCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, tokenCookie.toString());

        return ResponseEntity.ok(Map.of("success", true, "message", "로그아웃되었습니다."));
    }


    @Operation(summary = "토큰 재발급", description = "refresh token으로 access token을 재발급합니다.")
    @PostMapping("/v1.0/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @RequestBody Map<String, String> body,
            HttpServletResponse response) {

        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(false, null, null, "refresh token이 없습니다.", null));
        }

        LoginResponse result = jwtService.refreshAccessToken(refreshToken);

        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }

        // 새 access token을 쿠키로 심기
        Cookie cookie = new Cookie("accessToken", result.getAccessToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60); // access token 실제 만료시간(1시간)과 일치시킴
        response.addCookie(cookie);

        return ResponseEntity.ok(result);
    }

}