package kepco.prorject.ictyb.back.ictyb_back.common.util;

/**
 * its_kepco_user(한전 인사정보) 테이블만 사번이 10자리이고, 그 외 전체 시스템(레거시 KDN 테이블들:
 * its_n_sign/its_work_history/its_it_work_report/its_real_work_report 등)은 8자리 사번 체계를 쓴다 -
 * 10자리 한전 사번 앞의 2자리를 버리면 나머지 8자리가 그 체계와 일치한다(2026-07-22, 사용자 확인).
 *
 * [2026-07-23] its_kepco_user는 예외 없이 전부 10자리 SABUN만 갖도록 정리했다(schema.sql의
 * "SABUN 10자리 통일 마이그레이션" 블록 참고 - 예전엔 일부 8자리 행이 섞여 있어서 결재자 후보
 * 목록에 같은 사람이 8자리/10자리 행으로 중복 표시되는 버그가 있었다). 이제 이 테이블에서 읽은
 * SABUN은 항상 length==10이라고 가정해도 되지만, 이 메서드는 혹시 모를 8자리 입력이 들어와도
 * 그대로 반환하도록 방어적으로 남겨둔다. 화면/다른 테이블에는 항상 8자리(레거시 체계)로 노출해야
 * 하므로, its_kepco_user에서 사번을 읽어 로그인 신원·결재자 표시 등 다른 곳에 쓸 때는 반드시
 * 이 변환을 거친다 - 원본 테이블 자체는 이 유틸이 건드리지 않는다.
 */
public final class KepcoSabunUtil {

    private KepcoSabunUtil() {
    }

    public static String toLegacySabun(String kepcoSabun) {
        if (kepcoSabun != null && kepcoSabun.length() == 10) {
            return kepcoSabun.substring(2);
        }
        return kepcoSabun;
    }
}
