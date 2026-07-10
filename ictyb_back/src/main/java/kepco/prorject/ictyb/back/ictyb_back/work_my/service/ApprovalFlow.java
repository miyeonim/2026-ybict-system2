package kepco.prorject.ictyb.back.ictyb_back.work_my.service;

import java.util.Map;

/**
 * 결재 진행단계(ACT_ID) 순서 정의.
 * its_it_work_report.ACT_ID = 마지막으로 완료된 단계, its_n_sign.ACT_ID = 완료를 대기 중인 단계.
 * 104(지시서 작성/한전 직원) → 106(지시서 승인/한전 파트장) → 107(지시서 접수/KDN 부장)
 * → 108(지시서 배부/KDN 파트장) → 109(결과 보고/KDN 대리) → 110(작업결과 검토/KDN 파트장)
 * → 111(작업결과 승인/KDN 부장) → 114(조치결과 승인/한전 직원) → 800(완료)
 *
 * 자료추출(WORK_TYPE=02) 건은 108(지시서 배부)/111(작업결과 승인)을 생략한다:
 * 104 → 106 → 107(지시서 접수/KDN 부장) → 109(결과 보고/KDN 대리) → 110(작업결과 검토/KDN 파트장) → 114 → 800
 */
public final class ApprovalFlow {

    private ApprovalFlow() {
    }

    public static final String WORK_TYPE_DATA_EXTRACTION = "02";

    public enum Role {
        KEPCO_PART_LEADER, KDN_DEPT_HEAD, KDN_MEMBER, KDN_PART_LEADER, KEPCO_STAFF
    }

    /** 완료된 단계 → 다음에 대기할 단계 */
    public static final Map<String, String> NEXT = Map.of(
            "104", "106",
            "106", "107",
            "107", "108",
            "108", "109",
            "109", "110",
            "110", "111",
            "111", "114",
            "114", "800"
    );

    /** 대기 중인 단계 → 직전에 완료됐던 단계 (반송 대상 판단용) */
    public static final Map<String, String> PREV = Map.of(
            "106", "104",
            "107", "106",
            "108", "107",
            "109", "108",
            "110", "109",
            "111", "110",
            "114", "111"
    );

    // 주의: 108을 건너뛰는 이 흐름 때문에 자료추출 건은 부장(107) 승인 직후
    // WORKER_SABUN만 채워지고 its_it_work_report.ACT_ID는 담당자가 109를 완료할 때까지 "107"로 남는다.
    // report_total/repository/ItWorkReportRepository.java의 getCompletionStats 쿼리는
    // "108부터 집계"라는 전제로 ACT_ID 104~107을 통계에서 제외하는데, 그 전제가 이 흐름과 어긋나서
    // 한 차례 버그가 났었다(자료추출 건이 담당자에게 배정된 뒤에도 완료율 통계에 안 잡힘).
    // 이 단계 순서를 바꾸면 위 쿼리의 예외 조건(WORK_TYPE='02' AND ACT_ID='107' AND WORKER_SABUN 존재)도 같이 확인할 것.
    private static final Map<String, String> NEXT_DATA_EXTRACTION = Map.of(
            "104", "106",
            "106", "107",
            "107", "109",
            "109", "110",
            "110", "114",
            "114", "800"
    );

    private static final Map<String, String> PREV_DATA_EXTRACTION = Map.of(
            "106", "104",
            "107", "106",
            "109", "107",
            "110", "109",
            "114", "110"
    );

    public static boolean isDataExtraction(String workType) {
        return WORK_TYPE_DATA_EXTRACTION.equals(workType);
    }

    /** 완료된 단계 → 다음에 대기할 단계 (자료추출 건은 108/111 생략) */
    public static String next(String currentActId, String workType) {
        return (isDataExtraction(workType) ? NEXT_DATA_EXTRACTION : NEXT).get(currentActId);
    }

    /** 대기 중인 단계 → 직전에 완료됐던 단계 (자료추출 건은 108/111 생략) */
    public static String prev(String currentActId, String workType) {
        return (isDataExtraction(workType) ? PREV_DATA_EXTRACTION : PREV).get(currentActId);
    }

    /** 대기 중인 단계 코드의 한글명 */
    public static final Map<String, String> ACT_ID_NM = Map.of(
            "104", "지시서 작성",
            "106", "지시서 승인",
            "107", "지시서 접수",
            "108", "지시서 배부",
            "109", "결과 보고",
            "110", "작업결과 검토",
            "111", "작업결과 승인",
            "114", "조치결과 승인"
    );

    /** 대기 중인 단계 코드 → 그 단계를 수행할 역할 */
    public static final Map<String, Role> ROLE = Map.of(
            "106", Role.KEPCO_PART_LEADER,
            "107", Role.KDN_DEPT_HEAD,
            "108", Role.KDN_PART_LEADER,
            "109", Role.KDN_MEMBER,
            "110", Role.KDN_PART_LEADER,
            "111", Role.KDN_DEPT_HEAD,
            "114", Role.KEPCO_STAFF
    );

    /**
     * 반송 시 되돌아갈 its_it_work_report.ACT_ID(마지막 완료 단계) 계산.
     * targetActId(반송 대상 단계)의 이전 단계로 되돌리되, 더 이전 단계가 없으면(104) targetActId 그대로 둔다.
     */
    public static String revertReportActId(String targetActId, String workType) {
        Map<String, String> prevMap = isDataExtraction(workType) ? PREV_DATA_EXTRACTION : PREV;
        return prevMap.getOrDefault(targetActId, targetActId);
    }
}
