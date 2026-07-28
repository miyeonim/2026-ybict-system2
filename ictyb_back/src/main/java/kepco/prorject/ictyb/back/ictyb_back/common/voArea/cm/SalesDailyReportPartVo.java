package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.SalesDailyReportPartPk;
import kepco.prorject.ictyb.back.ictyb_back.report_daily.model.ReportPartStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ictyb_sales_daily_report_part 테이블 엔티티 (점검일지 1개 파트분 작성 내용)
 * REPORT_ID, PART_ID는 ictyb_sales_daily_report / ictyb_part_info를 가리키는 값이지만
 * 프로젝트 컨벤션상 FK 제약은 두지 않고 조회 시점에 키 컬럼으로만 조인한다.
 */
@Entity
@Table(name = "ictyb_sales_daily_report_part")
@IdClass(SalesDailyReportPartPk.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesDailyReportPartVo {

    @Id
    @Column(name = "REPORT_ID")
    private Long reportId;

    @Id
    @Column(name = "PART_ID", length = 20, columnDefinition = "VARCHAR(20) COMMENT '파트ID (ictyb_part_info.PART_ID)'")
    private String partId;

    @Column(name = "PART_NM", length = 100, columnDefinition = "VARCHAR(100) COMMENT '파트명 스냅샷'")
    private String partNm;

    @Column(name = "EFFICIENCY_CONTENT", length = 2000, columnDefinition = "VARCHAR(2000) COMMENT '유지보수 관리 효율화 및 주요 배포내용'")
    private String efficiencyContent;

    @Column(name = "MAIN_INSTRUCTION_CONTENT", length = 2000, columnDefinition = "VARCHAR(2000) COMMENT '주요 작업지시 내용'")
    private String mainInstructionContent;

    @Column(name = "WAS_ERROR_CONTENT", length = 2000, columnDefinition = "VARCHAR(2000) COMMENT 'WAS 오류 내역'")
    private String wasErrorContent;

    @Column(name = "MEETING_SCHEDULE", length = 1000, columnDefinition = "VARCHAR(1000) COMMENT '회의예정'")
    private String meetingSchedule;

    @Column(name = "SPECIAL_NOTES", length = 1000, columnDefinition = "VARCHAR(1000) COMMENT '특이사항'")
    private String specialNotes;

    @Column(name = "AUTHOR_SABUN", length = 20, columnDefinition = "VARCHAR(20) COMMENT '이 파트 작성자 사번'")
    private String authorSabun;

    @Column(name = "AUTHOR_NAME", length = 50, columnDefinition = "VARCHAR(50) COMMENT '이 파트 작성자명'")
    private String authorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'DRAFT' COMMENT '결재 상태'")
    private ReportPartStatus status;

    @Column(name = "SUBMITTED_DT")
    private LocalDateTime submittedDt;

    @Column(name = "PART_LEADER_SABUN", length = 20)
    private String partLeaderSabun;

    @Column(name = "PART_LEADER_NAME", length = 50)
    private String partLeaderName;

    @Column(name = "PART_LEADER_APPROVED_DT")
    private LocalDateTime partLeaderApprovedDt;

    @Column(name = "HEAD_SABUN", length = 20)
    private String headSabun;

    @Column(name = "HEAD_NAME", length = 50)
    private String headName;

    @Column(name = "HEAD_APPROVED_DT")
    private LocalDateTime headApprovedDt;

    @Column(name = "REJECTED_BY_SABUN", length = 20)
    private String rejectedBySabun;

    @Column(name = "REJECTED_BY_NAME", length = 50)
    private String rejectedByName;

    @Column(name = "REJECTED_BY_ROLE", length = 20, columnDefinition = "VARCHAR(20) COMMENT '반려자 역할: PART_LEADER/HEAD'")
    private String rejectedByRole;

    @Column(name = "REJECT_REASON", length = 500)
    private String rejectReason;

    @Column(name = "REJECTED_DT")
    private LocalDateTime rejectedDt;
}
