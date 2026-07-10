package kepco.prorject.ictyb.back.ictyb_back.jwt.service;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;

public class SsoUtil {

    private static final Logger log = LoggerFactory.getLogger(SsoUtil.class);

    // ⚠️ 실제 서버(100.1.14.100)에 seed_decode 바이너리가 설치된 경로로 확인/수정 필요
    private static final String DECODE_BIN_PATH = "/KEPCO/seed_decode -d ";

    /** 외부 호출 진입점: 쿠키에서 SSO 사번을 추출. 없으면 null */
    public static String getUserId(HttpServletRequest request) {
        return getCookieValue(request);
    }

    private static String getCookieValue(HttpServletRequest request) {
        String cookieHeader = request.getHeader("Cookie");
        String pgsecuid = "";
        String userId = "";

        if (cookieHeader == null) {
            log.error("cookie is null");
            return null;
        }

        log.info("cookie check :: {}", cookieHeader);
        for (String cookie : cookieHeader.split(";")) {
            cookie = cookie.trim();
            int index = cookie.indexOf("=");
            if (index != -1) {
                String cookieName = cookie.substring(0, index).trim();
                String cookieValue = cookie.substring(index + 1).trim();
                if (cookieName.equals("pgsecuid")) {
                    pgsecuid = cookieValue;
                }
            } else {
                log.error("Abnormal cookie");
            }
        }

        if (pgsecuid.isEmpty()) {
            log.error("pgsecuid 없음.");
            return null;
        }

        Runtime rt = Runtime.getRuntime();
        Process ps = null;
        BufferedReader br = null;

        try {
            // 인자 분리 실행 (셸 인젝션 방지)
            ps = rt.exec(new String[]{"/KEPCO/seed_decode", "-d", pgsecuid});

            InputStream in = ps.getInputStream();
            InputStream err = ps.getErrorStream();
            InputStreamReader inr = new InputStreamReader(new SequenceInputStream(in, err));
            br = new BufferedReader(inr);

            userId = br.readLine();
            log.info("userId :::: {}", userId);
        } catch (Exception e) {
            log.error("sso 복호화 실패", e);
            return null;
        } finally {
            if (br != null) {
                try { br.close(); } catch (Exception e) { log.error("BufferedReader can't close!", e); }
            }
            if (ps != null) ps.destroy();
        }

        return (userId == null || userId.isEmpty()) ? null : userId;
    }
}