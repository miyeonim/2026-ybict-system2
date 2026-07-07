package kepco.prorject.ictyb.back.ictyb_back.work_opinion.model;

import lombok.*;

import java.util.List;

public class WorkOpinionDto {

    // ── 댓글 첨부파일 ─────────────────────────────────────────────
    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AttachmentItem {
        private Long seqNo;
        private String realFileName;
        private Long fileSize;
    }

    // ── 댓글 응답 ──────────────────────────────────────────────────
    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CommentItem {
        private String cmntId;
        private String opnId;
        private String cmntCtt;
        private String wrtrEmpno;
        private String wrtrNm;
        private String wrtrRoleNm;
        private String regDt;
        private List<AttachmentItem> attachments;
    }

    // ── 협의 스레드 응답 (댓글 목록 포함) ─────────────────────────
    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DiscussionItem {
        private String opnId;
        private String instrNo;
        private String opnTitle;
        private String wrtrEmpno;
        private String wrtrNm;
        private String regDt;
        private List<CommentItem> comments;
    }

    // ── 협의 생성 요청 ─────────────────────────────────────────────
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateDiscussionReq {
        private String instrNo;
        private String opnTitle;
        private String wrtrEmpno;
        private String wrtrNm;
        private String wrtrRoleNm; // 첨부파일이 있어 최초 댓글을 함께 생성할 때만 사용
    }

    // ── 댓글 등록 요청 ─────────────────────────────────────────────
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CreateCommentReq {
        private String opnId;
        private String cmntCtt;
        private String wrtrEmpno;
        private String wrtrNm;
        private String wrtrRoleNm;
    }

    // ── 첨부파일 다운로드 ─────────────────────────────────────────
    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DownloadFile {
        private org.springframework.core.io.Resource resource;
        private String realFileName;
    }
}
