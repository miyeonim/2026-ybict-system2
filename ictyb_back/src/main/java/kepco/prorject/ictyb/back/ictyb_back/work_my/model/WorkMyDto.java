package kepco.prorject.ictyb.back.ictyb_back.work_my.model;

import lombok.*;
import org.springframework.core.io.Resource;

import java.util.List;

public class WorkMyDto {

    // ── 목록 응답 ─────────────────────────────────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListItem {
        private String workOrderNo;     // 지시서 처리번호 (INST_ID)
        private String title;           // 지시제목
        private String department;      // 부서 (영업/배전/기술)
        private String part;            // 파트명
        private String approvalStatus;  // 결재 대기 / 결재 완료 / 미요청
        private String status;          // 접수 / 처리 중 / 완료 / 협의
        private String dueDt;           // 완료예정일
        private List<ApprovalHistoryItem> approvalHistory; // 결재이력 (승인/반려, 시분초 포함)
        private boolean returnedToMe;   // 반송되어 현재 로그인 사용자가 다시 처리해야 하는 건인지
        private boolean hasUnreadDiscussion; // 아직 읽지 않은 협의(신규 등록/댓글)가 있는지 - 협의 탭 new! 배지 판단용
    }

    // ── 결재이력 (승인/반려 처리자 + 시각) ──────────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApprovalHistoryItem {
        private String sabun;      // 처리자 사번
        private String name;       // 처리자 이름
        private String actIdNm;    // 처리한 단계명 (예: 지시서 승인)
        private String signLabel;  // 승인 / 반려
        private String regDt;      // 처리일시 yyyyMMddHHmmss (시분초 포함)
        private String reason;     // 반려(반송) 사유, 승인 건은 null
    }

    // ── 다음 단계 담당자 후보 ─────────────────────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Candidate {
        private String sabun;   // 사번
        private String name;    // 이름
        private String roleNm;  // 역할 표시명 (예: KDN 부장)
    }

    // ── 다음 단계 담당자 후보 응답 (현재 대기 단계 코드 포함) ──────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NextCandidatesResponse {
        private String currentActId;   // 현재 대기 중인 단계 코드 (예: "109")
        private List<Candidate> candidates;
    }

    // ── 승인 요청 ─────────────────────────────────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApproveRequest {
        private String actorSabun; // 승인 처리자(로그인 사용자) 사번 - 프론트가 useAuthContext 기준으로 채워 보낸다
        private String actorName;  // 승인 처리자(로그인 사용자) 이름
        private String nextSabun;  // 다음 단계 담당자 사번
        private String nextName;   // 다음 단계 담당자 이름
        private String workResult; // 조치사항 (109단계 "작업결과 보고" 승인 시 필수)
        private String isSecret;             // 조치사항 개인정보 포함 여부 - Y/N (109단계에서만 사용, its_user_info_secret)
        private String oppbClYn;             // 공개구분 - Y(대외 게시용)/N(기타), isSecret=Y일 때만 유효
        private String userSecretContent;    // 개인정보 포함 항목, isSecret=Y일 때만 유효
        private String attachExpireDate;     // 파기일 (yyyy-MM-dd), isSecret=Y일 때만 유효
    }

    // ── 반송 요청 ─────────────────────────────────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReturnRequest {
        private String actorSabun;  // 반송 처리자(로그인 사용자) 사번
        private String actorName;   // 반송 처리자(로그인 사용자) 이름
        private String reason;      // 반송 사유
        private String targetActId; // 반송 대상 단계 코드 (미지정 시 직전 단계로 반송)
    }

    // ── 처리기간 연장 요청 (109단계 결재대기 중 지시서 작성자만 가능) ──────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExtendPeriodRequest {
        private String actorSabun;    // 연장 요청자(로그인 사용자) 사번
        private String newWorkPeriod; // 새 처리기간 (일), 기존 처리기간보다 커야 한다
    }

    // ── 반송 대상 단계 후보 ───────────────────────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReturnTarget {
        private String actId;    // 단계 코드 (예: "107")
        private String actIdNm;  // 단계명 (예: 지시서 접수)
        private String sabun;    // 반송 시 재배정될 처리자 사번 (그 단계를 마지막으로 처리한 사람)
        private String name;     // 반송 시 재배정될 처리자 이름
    }

    // ── 반송 대상 단계 후보 응답 ───────────────────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReturnTargetsResponse {
        private String currentActId;    // 현재 대기 중인 단계 코드
        private List<ReturnTarget> targets; // 직전 단계부터 오래된 순
    }

    // ── 코드성 드롭다운 옵션 (임시, WorkOrderCodeOptions 참고) ──────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CodeOption {
        private String code;
        private String label;

        public static CodeOption of(String code, String label) {
            return CodeOption.builder().code(code).label(label).build();
        }
    }

    // ── 단위시스템 옵션 (its_system_info 기반) ───────────────────────
    // 단위시스템을 선택하면 업무분야/DRS영향여부가 이 값들로 자동 채워진다 (프론트에서 조회 없이 즉시 반영).
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UnitSystemOption {
        private String code;          // SYSTEM_CD
        private String label;         // CMS_NAME (단위시스템명)
        private String businessField; // DOMAIN_NAME + "-" + SUBDOMAIN_NAME (업무분야)
        private String drsImptYn;     // its_it_work_report.DRS_IMPT_YN 의미 기준으로 환산된 값 (Y=영향 있음/N=영향 없음)
    }

    // ── 업무지시서 등록 폼 옵션 응답 ─────────────────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateOptions {
        private java.util.List<CodeOption> serviceTypeOptions;
        private java.util.List<CodeOption> workTypeOptions;
        private java.util.List<CodeOption> workGubunOptions;
        private java.util.List<CodeOption> workLevelOptions;
        private java.util.List<CodeOption> drsImptOptions;
        private java.util.List<UnitSystemOption> unitSystemOptions;
    }

    // ── 업무지시서 등록 요청 ─────────────────────────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private String regUserSabun;         // 등록자(로그인 사용자) 사번 - 프론트가 useAuthContext 기준으로 채워 보낸다
        private String regUserName;          // 등록자 이름
        private String regUserDepId;         // 등록자 부서ID
        private String regUserDepTitle;      // 등록자 부서명
        private String workOrderNo;          // 등록 다이얼로그를 열 때 미리 예약해둔 등록번호(INST_ID)
        private String changeTitle;          // 제목
        private String changeReason;         // 지시내용
        private String systemCd;             // 단위시스템 코드 (its_system_info.SYSTEM_CD)
        private String serviceType;          // 서비스유형 코드
        private String workType;             // 작업유형 코드
        private String workGubun;            // 작업구분 코드
        private String workLevel;            // 작업레벨 (상/중/하)
        private String workPeriod;           // 처리기간 (일)
        private String workDuration;         // 작업기간 (일) - 처리기간과 별개, its_it_work_report.WORK_PERIOD_MANUAL
        private String expectedFinishedDt;   // 처리예정일 (yyyy-MM-dd)
        private String drsImptYn;            // DRS(재해복구시스템) 영향 여부 - Y/N
        private String isSecret;             // 개인정보 포함 여부 - Y/N (its_user_info_secret)
        private String oppbClYn;             // 공개구분 - Y(대외 게시용)/N(기타), isSecret=Y일 때만 유효
        private String userSecretContent;    // 개인정보 포함 항목, isSecret=Y일 때만 유효
        private String attachExpireDate;     // 파기일 (yyyy-MM-dd), isSecret=Y일 때만 유효
        private String initialApproverSabun; // 최초 결재자(한전 파트장) 사번
        private String initialApproverName;  // 최초 결재자 이름
        private String sourceRequestNo;      // 작업 요청서(RealWorkReportVo) 기반 작성 시 그 요청서 번호(INST_ID). 일반 등록이면 null/blank.
    }

    // ── 작업 요청서 등록 요청 (100 완료 + 102 결재 대기 생성. 102/103 승인·반송, 103→104
    // 전환(지시서 신규 생성)은 아직 구현되어 있지 않다 - 등록까지만 구현된 상태) ──────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateWorkRequestReq {
        private String regUserSabun;        // 등록자(로그인 사용자) 사번 - 프론트가 useAuthContext 기준으로 채워 보낸다
        private String regUserName;         // 등록자 이름
        private String regUserDepId;        // 등록자 부서ID
        private String regUserDepTitle;     // 등록자 부서명
        private String workRequestNo;       // 등록 다이얼로그를 열 때 미리 예약해둔 요청번호(INST_ID)
        private String changeTitle;         // 제목
        private String chgRsnCtt;           // 목적 및 근거
        private String changeReason;        // 요청사항
        private String serviceType;         // 서비스유형 코드
        private String systemCd;            // 단위시스템 코드 (its_system_info.SYSTEM_CD, 업무분야 파생용)
        private String expectedDt;          // 희망완료일 (yyyy-MM-dd)
        private String isSecret;             // 개인정보 포함 여부 - Y/N (its_user_info_secret)
        private String oppbClYn;             // 공개구분 - Y(대외 게시용)/N(기타), isSecret=Y일 때만 유효
        private String userSecretContent;    // 개인정보 포함 항목, isSecret=Y일 때만 유효
        private String attachExpireDate;     // 파기일 (yyyy-MM-dd), isSecret=Y일 때만 유효
        private String initialApproverSabun; // 요청서 승인(102) 결재자 사번
        private String initialApproverName;  // 요청서 승인(102) 결재자 이름
    }

    // ── 작업 요청서(MY) 목록 응답 ─────────────────────────────────────
    // its_it_work_report가 아니라 its_real_work_report 기반이라 부서·파트 개념이 없다
    // (103 접수 후 지시서로 전환돼야 비로소 생긴다 - 그 전환은 아직 구현 전).
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RequestListItem {
        private String workRequestNo;   // 요청서 처리번호 (INST_ID)
        private String title;           // 요청서제목
        private String approvalStatus;  // 결재 대기 / 미요청 (아직 완료 상태에 도달하는 흐름이 없어 결재 완료는 나오지 않는다)
        private String status;          // 접수 / 처리 중 (협의/완료는 요청서에 아직 없는 개념)
        private String dueDt;           // 희망완료일
        private List<ApprovalHistoryItem> approvalHistory;
    }

    // ── 작업 요청서 상세 ──────────────────────────────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RequestDetail {
        private String workRequestNo;
        private String changeTitle;       // 제목
        private String chgRsnCtt;         // 목적 및 근거
        private String changeReason;      // 요청사항
        private String serviceType;
        private String serviceTypeLabel;
        private String systemCd;
        private String systemCdLabel;
        private String businessField;
        private String expectedDt;        // 희망완료일 yyyy-MM-dd
        private String sendItSabun;       // IT담당자 사번 (103 접수 전엔 null)
        private String sendItName;        // IT담당자 이름
        private String workSabun;         // 작업책임자 사번 (아직 자동 지정 로직 전이라 항상 null)
        private String workName;          // 작업책임자 이름
        private String regUserSabun;
        private String regUserName;
        private String regUserDepNm;
        private String regDt;             // 요청 일시
        private String isSecret;          // 개인정보 포함 여부 - Y/N (its_user_info_secret)
        private String isSecretLabel;     // 개인정보 포함 여부 - "포함"/"미포함"
        private String oppbClYn;          // 공개구분 - Y(대외 게시용)/N(기타)
        private String userSecretContent; // 개인정보 포함 항목
        private String attachExpireDate;  // 파기일 (yyyy-MM-dd)
        private List<AttachmentItem> attachments;
        private String currentActId;      // 현재 대기 중인 단계 코드
        private boolean myTurn;
        private List<ApprovalHistoryItem> approvalHistory;
    }

    // ── 작업 요청서 승인 요청 (102 승인 시 103 담당자 지정) ─────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestApproveRequest {
        private String actorSabun; // 승인 처리자(로그인 사용자) 사번
        private String actorName;  // 승인 처리자(로그인 사용자) 이름
        private String nextSabun;
        private String nextName;
    }

    // ── 업무지시서 수정 요청 (104/106 단계, 한전 담당자 전용) ─────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String actorSabun;           // 수정 처리자(로그인 사용자) 사번
        private String changeTitle;          // 제목
        private String changeReason;         // 지시내용
        private String systemCd;             // 단위시스템 코드 (its_system_info.SYSTEM_CD)
        private String serviceType;          // 서비스유형 코드
        private String workType;             // 작업유형 코드
        private String workGubun;            // 작업구분 코드
        private String workLevel;            // 작업레벨 (상/중/하)
        private String workPeriod;           // 처리기간 (일)
        private String workDuration;         // 작업기간 (일) - 처리기간과 별개, its_it_work_report.WORK_PERIOD_MANUAL
        private String expectedFinishedDt;   // 처리예정일 (yyyy-MM-dd)
        private String drsImptYn;            // DRS(재해복구시스템) 영향 여부 - Y/N
        private String isSecret;             // 개인정보 포함 여부 - Y/N (its_user_info_secret)
        private String oppbClYn;             // 공개구분 - Y(대외 게시용)/N(기타), isSecret=Y일 때만 유효
        private String userSecretContent;    // 개인정보 포함 항목, isSecret=Y일 때만 유효
        private String attachExpireDate;     // 파기일 (yyyy-MM-dd), isSecret=Y일 때만 유효
        private List<String> removeAttachSeqs; // 삭제할 기존 첨부파일 순번
    }

    // ── 첨부파일 항목 ─────────────────────────────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AttachmentItem {
        private String seq;
        private String realFileName;
        private String fileSize;
        private String regDt;
    }

    // ── 작업결과(조치사항) ────────────────────────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WorkResultItem {
        private String result;
        private String workerName;
        private String regDt;
        private List<AttachmentItem> attachments;
        private String isSecret;          // 조치사항 개인정보 포함 여부 - Y/N
        private String isSecretLabel;     // 조치사항 개인정보 포함 여부 - "포함"/"미포함"
        private String oppbClYn;          // 공개구분 - Y(대외 게시용)/N(기타)
        private String userSecretContent; // 개인정보 포함 항목
        private String attachExpireDate;  // 파기일 (yyyy-MM-dd)
    }

    // ── 업무지시서 상세 (등록 정보 + 첨부파일 + 작업결과) ──────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Detail {
        private String workOrderNo;
        private String sourceRequestNo; // 요청서 기반 전환 지시서인 경우 원본 요청서 번호(REQ_ID), 아니면 null
        private String regUserDepNm;    // IT담당자(지시서 최초 등록자)의 소속
        private String requesterDepNm;  // 요청자(원본 작업 요청서 작성자)의 소속. 요청서 기반이 아니면 null
        private String requesterName;   // 요청자 이름. 요청서 기반이 아니면 null
        private String requesterSabun;  // 요청자 사번. 요청서 기반이 아니면 null
        private String requesterTel;    // 요청자 전화번호(유선 우선, 없으면 휴대폰). 요청서 기반이 아니면 null
        private String changeTitle;
        private String changeReason;
        private String systemCd;         // 단위시스템 코드 (수정 폼 프리필용, its_system_info.SYSTEM_CD)
        private String systemCdLabel;    // 단위시스템명 (CMS_NAME)
        private String businessField;    // 업무분야 - DOMAIN_NAME + "-" + SUBDOMAIN_NAME
        private String serviceType;      // 서비스유형 코드 (수정 폼 프리필용)
        private String serviceTypeLabel;
        private String workType;         // 작업유형 코드 (수정 폼 프리필용)
        private String workTypeLabel;
        private String workGubun;        // 작업구분 코드 (수정 폼 프리필용)
        private String workGubunLabel;
        private String workLevel;
        private String workPeriod;
        private String workDuration;       // 작업기간 (일) - 처리기간과 별개, its_it_work_report.WORK_PERIOD_MANUAL
        private String expectedFinishedDt; // 처리예정일, yyyy-MM-dd
        private String hopeFinishedDt;      // 희망완료일 (읽기 전용) - its_real_work_report.EXPECTED_DT를 REQ_ID=INST_ID로 조인해 조회, yyyy-MM-dd
        private String targetDepNm;
        private String drsImptYn;    // DRS영향 코드 (수정 폼 프리필용)
        private String drsImptLabel; // DRS영향 - "영향 있음"/"영향 없음"
        private String isSecret;     // 개인정보 포함 여부 - Y/N (수정 폼 프리필용)
        private String isSecretLabel; // 개인정보 포함 여부 - "포함"/"미포함"
        private String oppbClYn;      // 공개구분 - Y(대외 게시용)/N(기타) (수정 폼 프리필용)
        private String userSecretContent; // 개인정보 포함 항목
        private String attachExpireDate;  // 파기일 (yyyy-MM-dd)
        private List<AttachmentItem> attachments;
        private String currentActId; // 현재 대기 중인 단계 코드 (완료 건은 null)
        private boolean myTurn;      // 로그인 사용자가 현재 결재 대기자인지
        private boolean canEdit;     // 작업지시서를 수정할 수 있는지 (104/106 단계의 현재 결재자, 한전 담당자만 해당)
        private boolean canDelete;   // 작업지시서를 삭제할 수 있는지 (등록자 본인만, 진행 단계 무관)
        private boolean canExtendPeriod; // 처리기간을 연장할 수 있는지 (109단계 결재대기 중 + 등록자 본인만 해당)
        private WorkResultItem workResult; // 조치사항 (109단계 완료 전에는 null)
        private List<ApprovalHistoryItem> approvalHistory; // 결재이력 (지금까지 거쳐간 승인/반려 처리자 + 시각)
    }

    // ── 첨부파일 다운로드 ─────────────────────────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DownloadFile {
        private Resource resource;
        private String realFileName;
    }
}
