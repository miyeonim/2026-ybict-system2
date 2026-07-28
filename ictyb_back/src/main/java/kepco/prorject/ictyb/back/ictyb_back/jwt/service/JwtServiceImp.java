package kepco.prorject.ictyb.back.ictyb_back.jwt.service;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.AccessLogVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.KdnDepVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.KdnUserVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.MwLginTknInfoVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.KepcoUserVo;
import kepco.prorject.ictyb.back.ictyb_back.common.util.DeptHeadUtil;
import kepco.prorject.ictyb.back.ictyb_back.common.util.KepcoSabunUtil;
import kepco.prorject.ictyb.back.ictyb_back.jwt.JwtTokenProvider;
import kepco.prorject.ictyb.back.ictyb_back.jwt.model.JwtUserDto;
import kepco.prorject.ictyb.back.ictyb_back.jwt.model.LoginRequest;
import kepco.prorject.ictyb.back.ictyb_back.jwt.model.LoginResponse;
import kepco.prorject.ictyb.back.ictyb_back.jwt.repository.AccessLogRepository;
import kepco.prorject.ictyb.back.ictyb_back.jwt.repository.KdnDepRepository;
import kepco.prorject.ictyb.back.ictyb_back.jwt.repository.KdnUserRepository;
import kepco.prorject.ictyb.back.ictyb_back.jwt.repository.MwLginTknInfoRepository;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.UserInfoVo;
import kepco.prorject.ictyb.back.ictyb_back.report_daily.repository.UserInfoRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.KepcoUserRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor // 생성자 주입
public class JwtServiceImp implements JwtService {

    private final KdnDepRepository kdnDepRepository;
    private final KdnUserRepository kdnUserRepository;
    private final KepcoUserRepository kepcoUserRepository;
    private final JwtTokenProvider jwtTokenProvider; // 1. JwtTokenProvider 주입 추가
    private final MwLginTknInfoRepository tokenRepository; // 2. 토큰 레포지토리 주입 추가
    private final AccessLogRepository accessLogRepository; // 3. 접속 로그 레포지토리 주입 추가
    private final UserInfoRepository userInfoRepository; // 영업 점검일지 파트/파트장/부장 여부 조회 (ictyb_user_info, KDN만)

    //SSO 자동 로그인 기능 추가
    @Override
    @Transactional // DB 등록 과정이 포함되므로 트랜잭션 처리를 권장합니다.
    public LoginResponse loginByEmpno(String userEmpno, HttpServletRequest request, HttpServletResponse response) {

        // SSO에서 넘어오는 userEmpno는 8자리 레거시 사번이다(예: pgsecuid 복호화 값). its_kepco_user는
        // [2026-07-23] 전부 10자리로 통일되어 있으므로 findBySabun(원본 PK 정확매칭) 대신
        // authenticate()/authenticateKepcoUser()와 동일하게 끝자리 매칭(findFirstBySabunEndingWith)을
        // 써야 한다 - 그렇지 않으면 8자리 입력이 10자리 PK와 매칭되지 않아 SSO 로그인이 항상 실패한다.
        Optional<KepcoUserVo> kepco_user = kepcoUserRepository.findFirstBySabunEndingWith(userEmpno);
        System.out.println("kepco_user1 :::" + kepco_user.toString());
        if (userEmpno == null || userEmpno.isEmpty()) {
            return new LoginResponse(false, null, null, "SSO로 사용자 정보를 찾을 수 없습니다.", null);
        }

        //JWT 값 세팅
        KepcoUserVo kepcoUserVo = kepco_user.get();

        // 🌟 수정 2: user를 실제로 생성
        // its_kepco_user만 사번이 10자리라, 신원(userEmpno)은 KepcoSabunUtil로 8자리로 변환해서 쓴다.
        String legacySabun = KepcoSabunUtil.toLegacySabun(kepcoUserVo.getSabun());
        JwtUserDto user = JwtUserDto.builder()
        .userEmpno(legacySabun)
        .empNm(kepcoUserVo.getName())
        .depId(kepcoUserVo.getSosokCd())
        .parDepId("")
        .depTitle(kepcoUserVo.getSosokHan())
        .kepcoMap("BONSA")
        .kepcoYn("Y")
        .deptHead(DeptHeadUtil.isKepcoDeptHead(kepcoUserVo))
        .build();


        // 실제 유효한 JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken();

        // HttpOnly 쿠키로 accessToken 심기 (기존 login()과 동일 로직)
        ResponseCookie cookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true).path("/").maxAge(3600).build();
        response.addHeader("Set-Cookie", cookie.toString());

        recordLoginSuccess(legacySabun);

        return new LoginResponse(true, accessToken, refreshToken, "SSO 자동 로그인 성공", user);
    }

    @Override
    @Transactional // DB 등록 과정이 포함되므로 트랜잭션 처리를 권장합니다.
    public LoginResponse authenticate(LoginRequest request) {

        // 1. KDN 직원(its_kdn_user) 우선 조회
        Optional<KdnUserVo> kdnUser = kdnUserRepository.findByLoginId(request.getUserEmpno());
        if (kdnUser.isPresent()) {
            return authenticateKdnUser(kdnUser.get(), request.getPassword());
        }

        // 2. KDN에 없으면 한전(KEPCO) 인사정보(its_kepco_user)에서 조회
        // 로그인은 항상 8자리 사번으로 받는다(2026-07-22, 사용자 확인) - its_kepco_user의 원본 10자리
        // SABUN과 직접 비교(findBySabun)하지 않고 끝자리 일치(findFirstBySabunEndingWith)로 찾는다.
        Optional<KepcoUserVo> kepcoUser = kepcoUserRepository.findFirstBySabunEndingWith(request.getUserEmpno());
        if (kepcoUser.isPresent()) {
            return authenticateKepcoUser(kepcoUser.get());
        }

        return new LoginResponse(false, null, null, "존재하지 않는 사번입니다.", null);
    }

    private LoginResponse authenticateKdnUser(KdnUserVo user, String password) {
        // 비밀번호 비교 (실제 운영 시에는 BCrypt 등을 사용하여 암호화 비교해야 합니다)
        if (!user.getUserPwd().equals(password)) {
            recordLoginFailure(user.getLoginId());
            return new LoginResponse(false, null, null, "비밀번호가 일치하지 않습니다.", null);
        }

        Optional<KdnDepVo> depInfoOpt = kdnDepRepository.findByDepId(user.getDepId());
        if (depInfoOpt.isEmpty()) {
            return new LoginResponse(false, null, null, "사용자의 부서 정보를 찾을 수 없습니다.", null);
        }
        KdnDepVo userDepInfo = depInfoOpt.get();

        UserInfoVo partInfo = userInfoRepository.findCurrentByEmpno(user.getLoginId()).orElse(null);

        JwtUserDto userInfo = JwtUserDto.builder()
            .depId(userDepInfo.getDepId())          // 부서ID
            .parDepId(userDepInfo.getParDepId())    // 처명
            .depTitle(userDepInfo.getDepTitle())    // 부서명
            .kepcoMap(userDepInfo.getKepcoMap())    // 본사여부
            .userEmpno(user.getLoginId())           // 사번
            .empNm(user.getUserNm())                // 이름
            .kepcoYn("N")                   // 한전사람 여부
            .deptHead(DeptHeadUtil.isKdnDeptHead(user))
            .partId(partInfo != null ? partInfo.getPartId() : null)
            .partLeader(partInfo != null && "Y".equals(partInfo.getPartleaderYn()))
            .bujan(partInfo != null && "Y".equals(partInfo.getBujanYn()))
            .build();

        return issueTokenResponse(userInfo);
    }

    /**
     * 한전(KEPCO) 사용자 로그인 인증 (its_kepco_user 기준)
     * 한전 사람은 실제로는 SSO로 인증되어 비밀번호가 없으므로, 비밀번호 검증 없이(공란이어도) 사번만으로 로그인시킨다.
     * 추후 한전 망으로 이관하면서 실제 SSO 연동으로 교체할 예정.
     * 2026-07-22: its_kepco_dep(조직도) 조회 없이 its_kepco_user 컬럼만으로 부서정보를 구성한다 -
     * SOSOK_CD/SOSOK_HAN이 사용자 row에 이미 있어 조직도 테이블과 무관하게 로그인 가능(그 테이블/엔티티는 남겨두되 여기서는 더 이상 참조하지 않음).
     * its_kepco_user만 사번이 10자리라 신원(userEmpno)은 KepcoSabunUtil로 8자리로 변환해서 쓴다
     * (그 외 전체 시스템 - its_n_sign/its_work_history 등 - 은 8자리 사번 체계, 앞 2자리를 버리면 일치함).
     */
    private LoginResponse authenticateKepcoUser(KepcoUserVo user) {
        JwtUserDto userInfo = JwtUserDto.builder()
            .depId(user.getSosokCd())
            .parDepId("")
            .depTitle(user.getSosokHan())
            .kepcoMap("BONSA") // 한전(본사) 소속
            .userEmpno(KepcoSabunUtil.toLegacySabun(user.getSabun()))
            .empNm(user.getName())
            .kepcoYn("Y")
            .deptHead(DeptHeadUtil.isKepcoDeptHead(user))
            .build();

        return issueTokenResponse(userInfo);
    }

    private LoginResponse issueTokenResponse(JwtUserDto userInfo) {
        System.out.println("로그인된 사용자 정보: " + userInfo.toString());

        // 실제 유효한 JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(userInfo);
        String refreshToken = jwtTokenProvider.createRefreshToken();

        // Refresh Token을 DB에 저장하기 위한 엔티티 생성 및 세팅
        MwLginTknInfoVo tokenInfo = new MwLginTknInfoVo();
        tokenInfo.setRfstknKey(refreshToken);
        tokenInfo.setFrstRegrEmpno(userInfo.getUserEmpno());
        tokenInfo.setLstChgrEmpno(userInfo.getUserEmpno());
        tokenInfo.setUserEmpno(userInfo.getUserEmpno());
        tokenInfo.setExpYmd(LocalDate.now().plusDays(1)); // 만료일 하루 뒤 계산

        // DB 테이블(ICTYB_MW_LGIN_TKN_INFO)에 영속화
        tokenRepository.save(tokenInfo);

        recordLoginSuccess(userInfo.getUserEmpno());

        return new LoginResponse(true, accessToken, refreshToken, "로그인에 성공하셨습니다.", userInfo);
    }

    /**
     * 로그인 성공 시 접속 로그(its_access_log) 갱신: 접속시간/마지막접속일 최신화, 실패횟수(CNT) 리셋
     */
    private void recordLoginSuccess(String sabun) {
        AccessLogVo accessLog = accessLogRepository.findById(sabun).orElseGet(AccessLogVo::new);
        accessLog.setSabun(sabun);
        accessLog.setConnectTime(LocalDate.now());
        accessLog.setLoginDt(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        accessLog.setCnt(0);
        accessLogRepository.save(accessLog);
    }

    /**
     * 로그인 실패(비밀번호 불일치) 시 접속 로그(its_access_log)의 실패횟수(CNT)만 누적
     */
    private void recordLoginFailure(String sabun) {
        AccessLogVo accessLog = accessLogRepository.findById(sabun).orElseGet(AccessLogVo::new);
        accessLog.setSabun(sabun);
        accessLog.setCnt((accessLog.getCnt() == null ? 0 : accessLog.getCnt()) + 1);
        accessLogRepository.save(accessLog);
    }



    /**
     * 🌟 쿠키 토큰으로부터 사용자 정보 조회
     */
    public LoginResponse getUserInfoFromToken(String token) {
        try {
            JwtUserDto userInfo = jwtTokenProvider.getUserInfo(token);
            return new LoginResponse(true, null,null, "조회 성공", userInfo);
        } catch (Exception e) {
            return new LoginResponse(false, null, null,  "토큰 만료 또는 유효하지 않음", null);
        }
    }


    /**
     * 🌟 리프레쉬 토는 재발급(한전사람) 또는 만료시 재로그인 화면
     */
    @Override
    @Transactional
    public LoginResponse refreshAccessToken(String refreshToken) {

        // 1. DB에서 refresh token 레코드 조회 (없으면 원본 소유자를 알 수 없어 자동갱신 불가)
        Optional<MwLginTknInfoVo> tokenInfoOpt = tokenRepository.findByRfstknKey(refreshToken);
        if (tokenInfoOpt.isEmpty()) {
            return new LoginResponse(false, null, null, "유효하지 않은 refresh token 입니다. 다시 로그인해주세요.", null);
        }

        MwLginTknInfoVo tokenInfo = tokenInfoOpt.get();
        String userEmpno = tokenInfo.getUserEmpno();
        boolean isExpired = tokenInfo.getExpYmd().isBefore(LocalDate.now());

        // 2. 사용자 정보 재조회 (한전, KDN)
        Optional<JwtUserDto> userInfoOpt = resolveUserInfo(userEmpno);
        if (userInfoOpt.isEmpty()) {
            tokenRepository.delete(tokenInfo);
            return new LoginResponse(false, null, null, "사용자 정보를 찾을 수 없습니다.", null);
        }
        JwtUserDto userInfo = userInfoOpt.get();
        boolean isKepco = "Y".equals(userInfo.getKepcoYn());

        // 3. 만료됐는데 한전 사용자가 아니면 → 자동갱신 불가, 재로그인(비밀번호) 필요
        if (isExpired && !isKepco) {
            tokenRepository.delete(tokenInfo);
            return new LoginResponse(false, null, null, "세션이 만료되었습니다. 다시 로그인해주세요.", null);
        }

        // 4. 여기 도달하면 둘 중 하나: (a) 아직 안 만료된 정상 refresh, (b) 만료됐지만 한전 사용자 → 자동 재인증
        String newAccessToken = jwtTokenProvider.createAccessToken(userInfo);
        String newRefreshToken = jwtTokenProvider.createRefreshToken();

        tokenRepository.delete(tokenInfo); // rotation: 기존 RT 폐기
        MwLginTknInfoVo newTokenInfo = new MwLginTknInfoVo();
        newTokenInfo.setRfstknKey(newRefreshToken);
        newTokenInfo.setUserEmpno(userEmpno);
        newTokenInfo.setFrstRegrEmpno(userEmpno);
        newTokenInfo.setLstChgrEmpno(userEmpno);
        newTokenInfo.setExpYmd(LocalDate.now().plusDays(1));
        tokenRepository.save(newTokenInfo);

        String message = isExpired ? "한전 사용자 자동 재인증 완료" : "재발급 성공";
        return new LoginResponse(true, newAccessToken, newRefreshToken, message, userInfo);
    }

    /**
     * userEmpno로 KEPCO → KDN  순서로 사용자 정보를 재조회하는 공통 헬퍼
     * (authenticateKdnUser / authenticateKepcoUser의 부서정보 조립 로직 재사용)
     * 여기 들어오는 userEmpno는 (토큰에 저장된) 8자리로 잘린 신원이라, its_kepco_user의 원본
     * 10자리 SABUN과 직접 비교(findBySabun)하면 안 되고 끝자리 일치(findFirstBySabunEndingWith)로 찾는다.
     */
    private Optional<JwtUserDto> resolveUserInfo(String userEmpno) {
        Optional<KepcoUserVo> kepcoUser = kepcoUserRepository.findFirstBySabunEndingWith(userEmpno);
        if (kepcoUser.isPresent()) {
            KepcoUserVo user = kepcoUser.get();
            return Optional.of(JwtUserDto.builder()
                .depId(user.getSosokCd())
                .parDepId("")
                .depTitle(user.getSosokHan())
                .kepcoMap("BONSA")
                .userEmpno(KepcoSabunUtil.toLegacySabun(user.getSabun()))
                .empNm(user.getName())
                .kepcoYn("Y")
                .deptHead(DeptHeadUtil.isKepcoDeptHead(user))
                .build());
        }

        Optional<KdnUserVo> kdnUser = kdnUserRepository.findByLoginId(userEmpno);
        if (kdnUser.isPresent()) {
            Optional<KdnDepVo> depOpt = kdnDepRepository.findByDepId(kdnUser.get().getDepId());
            if (depOpt.isEmpty()) return Optional.empty();
            KdnDepVo dep = depOpt.get();
            UserInfoVo partInfo = userInfoRepository.findCurrentByEmpno(kdnUser.get().getLoginId()).orElse(null);
            return Optional.of(JwtUserDto.builder()
                .depId(dep.getDepId())
                .parDepId(dep.getParDepId())
                .depTitle(dep.getDepTitle())
                .kepcoMap(dep.getKepcoMap())
                .userEmpno(kdnUser.get().getLoginId())
                .empNm(kdnUser.get().getUserNm())
                .kepcoYn("N")
                .deptHead(DeptHeadUtil.isKdnDeptHead(kdnUser.get()))
                .partId(partInfo != null ? partInfo.getPartId() : null)
                .partLeader(partInfo != null && "Y".equals(partInfo.getPartleaderYn()))
                .bujan(partInfo != null && "Y".equals(partInfo.getBujanYn()))
                .build());
        }

        return Optional.empty();
    }



    
}
