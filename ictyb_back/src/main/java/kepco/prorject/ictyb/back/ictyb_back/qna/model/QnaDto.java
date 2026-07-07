package kepco.prorject.ictyb.back.ictyb_back.qna.model;


import lombok.*;

import java.util.List;

public class QnaDto {

    // ── 목록 응답 ─────────────────────────────────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListItem {
        private Long   noticeNo;
        private String noticeTitle;
        private String regUserName;
        private String regDt;
        private int    viewCnt;
        private int    attachCount;
    }

    // ── 상세 응답 ─────────────────────────────────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Detail {
        private Long         noticeNo;
        private String       noticeTitle;
        private String       noticeDepCd;
        private String       noticeContents;
        private int          priority;
        private String       regUserSabun;
        private String       regUserDepCd;
        private String       regUserName;
        private String       regDt;
        private String       endDt;
        private String       delYn;
        private int          viewCnt;
        private String       noticeType;
        private List<Attach> attachList;
    }

    // ── 첨부파일 ──────────────────────────────────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Attach {
        private String noticeNo;
        private String seq;
        private String realFileName;
        private String fileName;
        private String fileLocation;
        private String regDt;
        private String attachType;
        private String fileSize;
    }

    // ── 등록 요청 ─────────────────────────────────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        private String noticeTitle;
        private String noticeContents;
        private String noticeDepCd;
        private String regUserSabun;
        private String regUserDepCd;
        private String regUserName;
        private String endDt;
        private int    priority;
    }
}
