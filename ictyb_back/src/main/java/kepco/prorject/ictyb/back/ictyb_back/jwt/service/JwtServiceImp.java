package kepco.prorject.ictyb.back.ictyb_back.jwt.service;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.KdnDepVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.KdnUserVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.MwLginTknInfoVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.KepcoDepVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.KepcoUserVo;
import kepco.prorject.ictyb.back.ictyb_back.jwt.JwtTokenProvider;
import kepco.prorject.ictyb.back.ictyb_back.jwt.model.JwtUserDto;
import kepco.prorject.ictyb.back.ictyb_back.jwt.model.LoginRequest;
import kepco.prorject.ictyb.back.ictyb_back.jwt.model.LoginResponse;
import kepco.prorject.ictyb.back.ictyb_back.jwt.repository.KdnDepRepository;
import kepco.prorject.ictyb.back.ictyb_back.jwt.repository.KdnUserRepository;
import kepco.prorject.ictyb.back.ictyb_back.jwt.repository.MwLginTknInfoRepository;
import kepco.prorject.ictyb.back.ictyb_back.jwt.repository.KepcoDepRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.KepcoUserRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
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
    private final KepcoDepRepository kepcoDepRepository;
    private final KepcoUserRepository kepcoUserRepository;
    private final JwtTokenProvider jwtTokenProvider; // 1. JwtTokenProvider 주입 추가
    private final MwLginTknInfoRepository tokenRepository; // 2. 토큰 레포지토리 주입 추가

    //SSO 자동 로그인 기능 추가
    @Override
    @Transactional // DB 등록 과정이 포함되므로 트랜잭션 처리를 권장합니다.
    public LoginResponse loginByEmpno(String userEmpno, HttpServletRequest request, HttpServletResponse response) {
        
        Optional<KepcoUserVo> kepco_user = kepcoUserRepository.findBySabun(userEmpno);
        System.out.println("kepco_user1 :::" + kepco_user.toString());
        if (userEmpno == null || userEmpno.isEmpty()) {
            return new LoginResponse(false, null, null, "SSO로 사용자 정보를 찾을 수 없습니다.", null);
        }

        //JWT 값 세팅
        KepcoUserVo kepcoUserVo = kepco_user.get();

        // 🌟 수정 2: user를 실제로 생성
        JwtUserDto user = JwtUserDto.builder()
        .userEmpno(userEmpno)
        .empNm(kepcoUserVo.getName())
        .depId(kepcoUserVo.getSosokCd())
        .parDepId("")
        .depTitle(kepcoUserVo.getSosokHan())
        .kepcoMap("BONSA")
        .build();


        // 실제 유효한 JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken();

        // HttpOnly 쿠키로 accessToken 심기 (기존 login()과 동일 로직)
        ResponseCookie cookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true).path("/").maxAge(3600).build();
        response.addHeader("Set-Cookie", cookie.toString());

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

        // 2. KDN에 없으면 한전(KEPCO) 인사정보(ictyb_kepco_user)에서 조회
        Optional<KepcoUserVo> kepcoUser = kepcoUserRepository.findBySabun(request.getUserEmpno());
        if (kepcoUser.isPresent()) {
            return authenticateKepcoUser(kepcoUser.get());
        }

        return new LoginResponse(false, null, null, "존재하지 않는 사번입니다.", null);
    }

    private LoginResponse authenticateKdnUser(KdnUserVo user, String password) {
        // 비밀번호 비교 (실제 운영 시에는 BCrypt 등을 사용하여 암호화 비교해야 합니다)
        if (!user.getUserPwd().equals(password)) {
            return new LoginResponse(false, null, null, "비밀번호가 일치하지 않습니다.", null);
        }

        Optional<KdnDepVo> depInfoOpt = kdnDepRepository.findByDepId(user.getDepId());
        if (depInfoOpt.isEmpty()) {
            return new LoginResponse(false, null, null, "사용자의 부서 정보를 찾을 수 없습니다.", null);
        }
        KdnDepVo userDepInfo = depInfoOpt.get();

        JwtUserDto userInfo = JwtUserDto.builder()
            .depId(userDepInfo.getDepId())          // 부서ID
            .parDepId(userDepInfo.getParDepId())    // 처명
            .depTitle(userDepInfo.getDepTitle())    // 부서명
            .kepcoMap(userDepInfo.getKepcoMap())    // 본사여부
            .userEmpno(user.getLoginId())           // 사번
            .empNm(user.getUserNm())                // 이름
            .build();

        return issueTokenResponse(userInfo);
    }

    /**
     * 한전(KEPCO) 사용자 로그인 인증 (ictyb_kepco_user/ictyb_kepco_dep 기준)
     * 한전 사람은 실제로는 SSO로 인증되어 비밀번호가 없으므로, 비밀번호 검증 없이(공란이어도) 사번만으로 로그인시킨다.
     * 추후 한전 망으로 이관하면서 실제 SSO 연동으로 교체할 예정.
     */
    private LoginResponse authenticateKepcoUser(KepcoUserVo user) {
        Optional<KepcoDepVo> depInfoOpt = kepcoDepRepository.findByOfCd(user.getSosokCd());
        if (depInfoOpt.isEmpty()) {
            return new LoginResponse(false, null, null, "사용자의 부서 정보를 찾을 수 없습니다.", null);
        }
        KepcoDepVo userDepInfo = depInfoOpt.get();

        JwtUserDto userInfo = JwtUserDto.builder()
            .depId(userDepInfo.getOfCd())
            .parDepId(userDepInfo.getOfHan1())
            .depTitle(userDepInfo.getDisplayTitle())
            .kepcoMap("Y") // 한전(본사) 소속
            .userEmpno(user.getSabun())
            .empNm(user.getName())
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

        return new LoginResponse(true, accessToken, refreshToken, "로그인에 성공하셨습니다.", userInfo);
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
}
