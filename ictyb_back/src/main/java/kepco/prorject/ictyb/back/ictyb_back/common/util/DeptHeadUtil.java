package kepco.prorject.ictyb.back.ictyb_back.common.util;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.KdnUserVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.KepcoUserVo;

/**
 * "처장" 여부 판별 공용 유틸. KEPCO와 KDN은 서로 다른 테이블/컬럼으로 처장을 표시한다:
 * - KEPCO(its_kepco_user): JIKGUB_HAN(직급명)이 "1직급"인 인원이 처장이다(2026-07-22, 사용자 확인).
 * - KDN(its_kdn_user): POSITION_CODE(직책코드)가 "P005"인 인원이 처장이다(POSITION_NM="처장").
 * 결재자 후보 필터링(WorkMyServiceImpl)과 로그인 사용자 정보(JwtServiceImp)에서 동일한 기준을 쓰기
 * 위해 이 유틸로 묶는다 - 각 파일에 판별값을 따로 하드코딩하지 않는다.
 */
public final class DeptHeadUtil {

    private DeptHeadUtil() {
    }

    public static final String KEPCO_DEPT_HEAD_JIKGUB_HAN = "1직급";
    public static final String KDN_DEPT_HEAD_POSITION_CODE = "P005";

    public static boolean isKepcoDeptHead(KepcoUserVo user) {
        return user != null && KEPCO_DEPT_HEAD_JIKGUB_HAN.equals(user.getJikgubHan());
    }

    public static boolean isKdnDeptHead(KdnUserVo user) {
        return user != null && KDN_DEPT_HEAD_POSITION_CODE.equals(user.getPositionCode());
    }
}
