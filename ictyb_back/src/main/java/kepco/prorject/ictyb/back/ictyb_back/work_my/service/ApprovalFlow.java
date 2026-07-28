package kepco.prorject.ictyb.back.ictyb_back.work_my.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 결재 진행단계(ACT_ID) 순서 정의.
 * its_it_work_report.ACT_ID = 마지막으로 완료된 단계, its_n_sign.ACT_ID = 완료를 대기 중인 단계.
 * 104(지시서 작성/한전 직원) → 106(지시서 승인/한전 파트장) → 107(지시서 접수/KDN 부장)
 * → 108(지시서 배부/KDN 파트장) → 109(결과 보고/KDN 대리) → 110(작업결과 검토/KDN 파트장)
 * → 111(작업결과 승인/KDN 부장) → 114(조치결과 승인/한전 직원) → 800(완료)
 *
 * 자료추출(its_code에서 CODE_NAME='자료추출'인 서비스유형(GUBUN='01') 또는 작업유형(GUBUN='03') 코드 -
 * 둘 중 하나만 해당해도 자료추출로 취급) 건은 108(지시서 배부)/111(작업결과 승인)을 생략한다:
 * 104 → 106 → 107(지시서 접수/KDN 부장) → 109(결과 보고/KDN 대리) → 110(작업결과 검토/KDN 파트장) → 114 → 800
 *
 * 자료추출 여부는 its_code 테이블 조회가 필요해 이 static 유틸리티 안에서는 판별할 수 없다 - 호출하는 쪽
 * (WorkMyServiceImpl.isDataExtraction(ItWorkReportVo report))에서 미리 계산한 boolean을 받아 쓴다.
 *
 * 일부 건은 결재 맨 앞에 "요청서" 3단계가 더 붙는다(2026-07-21 도입):
 * 100(요청서 작성/한전 비IT부서 직원) → 102(요청서 승인/한전 비IT부서 파트장) → 103(요청서 접수/한전 IT부서 직원)
 * → (이후 104부터는 위 지시서 흐름과 동일). 요청서는 its_it_work_report가 아니라 별도 테이블
 * its_real_work_report(RealWorkReportVo, 자체 INST_ID)에 저장되고, 103 접수가 끝나야 비로소
 * its_it_work_report(104) 레코드가 새로 만들어져 REQ_ID로 그 요청서를 참조한다 - 즉 100/102/103은
 * ItWorkReportVo 한 레코드 안에서 진행되는 단계가 아니므로, 아래 NEXT/PREV 맵에는 포함하지 않는다
 * (요청서 자신의 진행은 its_n_sign/its_work_history를 요청서의 INST_ID로 그대로 재사용해 추적하되,
 * 다음 단계 계산은 파일 하단의 REQUEST_NEXT/REQUEST_PREV로 별도 처리한다). ACT_ID_NM에는 라벨 조회
 * 일관성을 위해 셋 다 추가한다. 100(등록)/102(요청서 승인)은 승인·반송 둘 다 구현되어 있다. 103(요청서
 * 접수)은 REQUEST_NEXT에 "103" 키가 의도적으로 없어 approveWorkRequest로는 "승인"할 수 없다 - 103은
 * WorkMyServiceImpl.createWorkOrder(request.sourceRequestNo=이 요청서 번호)를 통해서만 완료된다
 * (지시서를 실제로 작성하는 행위 자체가 103 접수 완료를 겸함, 2026-07-21). 103의 반송(102로 되돌림)은
 * REQUEST_PREV에 "103"→"102"가 있어 returnWorkRequestToPrevious로 그대로 지원된다.
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
    // 이 단계 순서를 바꾸면 위 쿼리의 예외 조건(WORK_TYPE='01'(자료추출) AND ACT_ID='107' AND WORKER_SABUN 존재)도 같이 확인할 것.
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

    /** 완료된 단계 → 다음에 대기할 단계 (자료추출 건은 108/111 생략) */
    public static String next(String currentActId, boolean isDataExtraction) {
        return (isDataExtraction ? NEXT_DATA_EXTRACTION : NEXT).get(currentActId);
    }

    /** 대기 중인 단계 코드의 한글명 (11개라 Map.of 10쌍 제한을 넘어 Map.ofEntries를 쓴다) */
    public static final Map<String, String> ACT_ID_NM = Map.ofEntries(
            Map.entry("100", "요청서 작성"),
            Map.entry("102", "요청서 승인"),
            Map.entry("103", "요청서 접수"),
            Map.entry("104", "지시서 작성"),
            Map.entry("106", "지시서 승인"),
            Map.entry("107", "지시서 접수"),
            Map.entry("108", "지시서 배부"),
            Map.entry("109", "결과 보고"),
            Map.entry("110", "작업결과 검토"),
            Map.entry("111", "작업결과 승인"),
            Map.entry("114", "조치결과 승인")
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
    public static String revertReportActId(String targetActId, boolean isDataExtraction) {
        Map<String, String> prevMap = isDataExtraction ? PREV_DATA_EXTRACTION : PREV;
        return prevMap.getOrDefault(targetActId, targetActId);
    }

    /** 104(작성)부터 800(완료)까지, 해당 건의 실제 진행 순서 전체 목록 (자료추출 건은 108/111 생략). */
    public static List<String> sequence(boolean isDataExtraction) {
        Map<String, String> nextMap = isDataExtraction ? NEXT_DATA_EXTRACTION : NEXT;
        List<String> seq = new ArrayList<>();
        String cur = "104";
        while (cur != null) {
            seq.add(cur);
            cur = nextMap.get(cur);
        }
        return seq;
    }

    /**
     * 반송 시 선택 가능한 이전 단계 목록. currentActId 이전(자신 제외)의 모든 단계를,
     * 가장 최근(직전) 단계부터 처음 작성 단계(104) 순으로 반환한다.
     * 결재 흐름은 분기 없이 순차 진행이므로 currentActId 이전 단계는 전부 이 건에서 실제로 거쳐온 단계다.
     */
    public static List<String> priorSteps(String currentActId, boolean isDataExtraction) {
        List<String> seq = sequence(isDataExtraction);
        int idx = seq.indexOf(currentActId);
        if (idx <= 0) {
            return List.of();
        }
        List<String> prior = new ArrayList<>(seq.subList(0, idx));
        Collections.reverse(prior);
        return prior;
    }

    /**
     * targetActId가 109(결과 보고, 작업자(WORKER_SABUN/NAME) 배정 시점) 이거나 그 이전 단계인지 여부.
     * true면 그 배정 자체를 되돌리는 것이므로 WORKER_SABUN/NAME도 함께 초기화해야 한다.
     */
    public static boolean isAtOrBeforeWorkerAssignment(String targetActId, boolean isDataExtraction) {
        List<String> seq = sequence(isDataExtraction);
        int targetIdx = seq.indexOf(targetActId);
        int workerIdx = seq.indexOf("109");
        return targetIdx >= 0 && workerIdx >= 0 && targetIdx <= workerIdx;
    }

    // ── 요청서(100/102/103) 체인 - its_real_work_report(RealWorkReportVo) 전용 ──────────────
    // 위 NEXT/PREV/sequence()는 its_it_work_report(ItWorkReportVo) 체인(104~800) 전용이라 섞지
    // 않는다. 요청서는 자료추출 같은 분기가 없는 3단계짜리 짧은 체인이라 boolean 파라미터가 없다.
    // 2026-07-21: 100(작성)+102(결재 대기 생성) 등록만 구현했다가, 같은 날 102(요청서 승인) 승인·반송까지
    // 추가로 구현함. 103(요청서 접수) 자체의 승인·반송, 103 이후 지시서(104) 신규 생성 전환은 여전히
    // REQUEST_NEXT에 "103" 키가 없어 approve() 호출 시 자연스럽게 막힌다 - 다음 작업으로 남겨둠.

    /** 완료된 요청서 단계 → 다음에 대기할 단계. "103"은 아직 키가 없어 100/102만 승인 가능하다. */
    public static final Map<String, String> REQUEST_NEXT = Map.of(
            "100", "102",
            "102", "103"
    );

    /** 대기 중인 요청서 단계 → 직전에 완료됐던 단계 (반송 대상 판단용) */
    public static final Map<String, String> REQUEST_PREV = Map.of(
            "102", "100",
            "103", "102"
    );

    public static String requestNext(String currentActId) {
        return REQUEST_NEXT.get(currentActId);
    }

    /** 요청서 진행 순서 전체 목록 (100 → 102 → 103). */
    public static List<String> requestSequence() {
        List<String> seq = new ArrayList<>();
        String cur = "100";
        while (cur != null) {
            seq.add(cur);
            cur = REQUEST_NEXT.get(cur);
        }
        return seq;
    }

    /** 반송 시 선택 가능한 이전 요청서 단계 목록 (직전 단계부터 오래된 순). */
    public static List<String> requestPriorSteps(String currentActId) {
        List<String> seq = requestSequence();
        int idx = seq.indexOf(currentActId);
        if (idx <= 0) {
            return List.of();
        }
        List<String> prior = new ArrayList<>(seq.subList(0, idx));
        Collections.reverse(prior);
        return prior;
    }
}
