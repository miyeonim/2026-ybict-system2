package kepco.prorject.ictyb.back.ictyb_back.work_my.service;

import java.util.Map;

/**
 * 결재 진행단계(ACT_ID) 순서 정의.
 * its_it_work_report.ACT_ID = 마지막으로 완료된 단계, its_n_sign.ACT_ID = 완료를 대기 중인 단계.
 * 104(지시서 작성) → 106(지시서 승인/한전 파트장) → 108(지시서 배부/KDN 부장)
 * → 109(작업결과 보고/KDN 직원) → 111(작업결과 승인/KDN 파트장) → 114(조치결과 승인/한전 직원) → 800(완료)
 */
public final class ApprovalFlow {

    private ApprovalFlow() {
    }

    public enum Role {
        KEPCO_PART_LEADER, KDN_DEPT_HEAD, KDN_MEMBER, KDN_PART_LEADER, KEPCO_STAFF
    }

    /** 완료된 단계 → 다음에 대기할 단계 */
    public static final Map<String, String> NEXT = Map.of(
            "104", "106",
            "106", "108",
            "108", "109",
            "109", "111",
            "111", "114",
            "114", "800"
    );

    /** 대기 중인 단계 → 직전에 완료됐던 단계 (반송 대상 판단용) */
    public static final Map<String, String> PREV = Map.of(
            "106", "104",
            "108", "106",
            "109", "108",
            "111", "109",
            "114", "111"
    );

    /** 대기 중인 단계 코드의 한글명 */
    public static final Map<String, String> ACT_ID_NM = Map.of(
            "104", "지시서 작성",
            "106", "지시서 승인",
            "108", "지시서 배부",
            "109", "작업결과 보고",
            "111", "작업결과 승인",
            "114", "조치결과 승인"
    );

    /** 대기 중인 단계 코드 → 그 단계를 수행할 역할 */
    public static final Map<String, Role> ROLE = Map.of(
            "106", Role.KEPCO_PART_LEADER,
            "108", Role.KDN_DEPT_HEAD,
            "109", Role.KDN_MEMBER,
            "111", Role.KDN_PART_LEADER,
            "114", Role.KEPCO_STAFF
    );

    /**
     * 반송 시 되돌아갈 its_it_work_report.ACT_ID(마지막 완료 단계) 계산.
     * targetActId(반송 대상 단계)의 이전 단계로 되돌리되, 더 이전 단계가 없으면(104) targetActId 그대로 둔다.
     */
    public static String revertReportActId(String targetActId) {
        return PREV.getOrDefault(targetActId, targetActId);
    }
}
