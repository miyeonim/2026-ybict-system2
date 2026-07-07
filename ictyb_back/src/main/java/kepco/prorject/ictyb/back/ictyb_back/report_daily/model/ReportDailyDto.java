package kepco.prorject.ictyb.back.ictyb_back.report_daily.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.*;
import org.springframework.core.io.Resource;

public class ReportDailyDto {

    // ── 유효 파트 목록 (ybict_part_info 기준) ───────────────────────
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
        private String authorName;
        private List<String> partNames;
        private int totalInProgress;
        private int totalDelayed;
        private int totalDistributed;
        private int attachmentCount;
    }

    // ── 상세 응답 ─────────────────────────────────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Detail {
        private Long reportId;
        private LocalDate reportDate;
        private String authorSabun;
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

    // ── 등록 요청 ─────────────────────────────────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        private LocalDate reportDate;
        private List<PartRequest> parts;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartRequest {
        private String partId;
        private List<PersonRequest> people;
        private String efficiencyContent;
        private String mainInstructionContent;
        private String wasErrorContent;
        private String meetingSchedule;
        private String specialNotes;
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
