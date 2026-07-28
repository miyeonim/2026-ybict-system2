package kepco.prorject.ictyb.back.ictyb_back.report_daily.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.*;
import org.springframework.core.io.Resource;

public class ReportDailyDto {

    // ── 유효 파트 목록 (ictyb_part_info 기준) ───────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PartOption {
        private String partId;
        private String partNm;
        private Integer partOrder;
    }

    // ── 파트 소속 인원별 작업지시 현황 (DB 집계) ─────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PersonStat {
        private String personSabun;
        private String personNm;
        private int inProgressCnt;
        private int delayedCnt;
    }

    // ── 목록 응답 ─────────────────────────────────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListItem {
        private Long reportId;
        private LocalDate reportDate;
        private String authorName; // 최종 수정자
        private List<String> partNames;
        private int totalInProgress;
        private int totalDelayed;
        private int totalDistributed;
        private int attachmentCount;
        private String overallStatus; // 작성중/파트장결재대기/부장결재대기/승인완료/반려
    }

    // ── 상세 응답 ─────────────────────────────────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Detail {
        private Long reportId; // 아직 아무도 제출하지 않은 날짜라면 null
        private LocalDate reportDate;
        private String authorSabun; // 최종 수정자
        private String authorName;
        private List<PartDetail> parts;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PartDetail {
        private String partId;
        private String partNm;
        private List<Person> people;
        private String efficiencyContent;
        private String mainInstructionContent;
        private String wasErrorContent;
        private String meetingSchedule;
        private String specialNotes;
        private List<Attach> attachments;

        // ── 결재 상태 ──
        private String status; // DRAFT/SUBMITTED/PART_APPROVED/FINAL_APPROVED/REJECTED
        private String authorSabun;
        private String authorName;
        private LocalDateTime submittedDt;
        private String partLeaderName;
        private LocalDateTime partLeaderApprovedDt;
        private String headName;
        private LocalDateTime headApprovedDt;
        private String rejectedByName;
        private String rejectedByRole; // PART_LEADER/HEAD
        private String rejectReason;
        private LocalDateTime rejectedDt;

        // ── 호출자(로그인 사용자) 기준 권한 (서버가 계산해서 내려줌) ──
        private String myRole; // AUTHOR/LEADER/HEAD/VIEWER
        private boolean canEdit;
        private boolean canApprove;
        private boolean canReject;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Person {
        private String personNm;
        private int inProgressCnt;
        private int delayedCnt;
        private int distributedCnt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Attach {
        private String seq;
        private String realFileName;
        private Long fileSize;
        private LocalDateTime regDt;
    }

    // ── 파트 제출 요청 (파트 소속 직원 1인이 자기 파트 내용을 제출) ──────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmitPartRequest {
        private String actorSabun;   // 제출자(로그인 사용자) 사번 - 프론트가 useAuthContext 기준으로 채워 보낸다
        private String actorName;    // 제출자 이름
        private LocalDate reportDate;
        private List<PersonRequest> people;
        private String efficiencyContent;
        private String mainInstructionContent;
        private String wasErrorContent;
        private String meetingSchedule;
        private String specialNotes;
    }

    // ── 파트 승인 요청 (승인 자체는 사유 등 별도 입력이 없어 처리자 식별값만 싣는다) ──────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApproveRequest {
        private String actorSabun; // 승인 처리자(로그인 사용자) 사번
        private String actorName;  // 승인 처리자 이름
    }

    // ── 반려 요청 ─────────────────────────────────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RejectRequest {
        private String actorSabun; // 반려 처리자(로그인 사용자) 사번
        private String actorName;  // 반려 처리자 이름
        private String reason;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonRequest {
        private String personNm;
        private int inProgressCnt;
        private int delayedCnt;
        private int distributedCnt;
    }

    // ── 첨부파일 다운로드 결과 ───────────────────────────────────────
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
