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
        private String nextSabun;  // 다음 단계 담당자 사번
        private String nextName;   // 다음 단계 담당자 이름
        private String workResult; // 조치사항 (109단계 "작업결과 보고" 승인 시 필수)
    }

    // ── 반송 요청 ─────────────────────────────────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReturnRequest {
        private String reason;  // 반송 사유
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
    }

    // ── 업무지시서 등록 요청 ─────────────────────────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private String changeTitle;          // 제목
        private String changeReason;         // 지시내용
        private String serviceType;          // 서비스유형 코드
        private String workType;             // 작업유형 코드
        private String workGubun;            // 작업구분 코드
        private String workLevel;            // 작업레벨 (상/중/하)
        private String workPeriod;           // 처리기간 (일)
        private String expectedFinishedDt;   // 완료예정일 (yyyy-MM-dd)
        private String initialApproverSabun; // 최초 결재자(한전 파트장) 사번
        private String initialApproverName;  // 최초 결재자 이름
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
    }

    // ── 업무지시서 상세 (등록 정보 + 첨부파일 + 작업결과) ──────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Detail {
        private String workOrderNo;
        private String changeTitle;
        private String changeReason;
        private String serviceTypeLabel;
        private String workTypeLabel;
        private String workGubunLabel;
        private String workLevel;
        private String workPeriod;
        private String expectedFinishedDt; // yyyy-MM-dd
        private String targetDepNm;
        private List<AttachmentItem> attachments;
        private String currentActId; // 현재 대기 중인 단계 코드 (완료 건은 null)
        private boolean myTurn;      // 로그인 사용자가 현재 결재 대기자인지
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
