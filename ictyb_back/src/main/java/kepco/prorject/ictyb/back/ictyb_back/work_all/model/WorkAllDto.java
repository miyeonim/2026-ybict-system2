package kepco.prorject.ictyb.back.ictyb_back.work_all.model;

import lombok.*;

public class WorkAllDto {

    // ── 목록 응답 ─────────────────────────────────────────────────
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListItem {
        private String workOrderNo;     // 지시서 처리번호 (INST_ID)
        private String title;           // 지시제목
        private String workType;        // 작업유형 코드
        private String department;      // 부서 (영업/배전/기술)
        private String part;            // 파트명
        private String managerName;     // 작업자이름
        private String approvalStatus;  // 결재 완료 / 미요청
        private String status;          // 접수 / 처리 중 / 완료
        private String regDt;           // 등록일
        private String dueDt;           // 완료예정일
    }
}
