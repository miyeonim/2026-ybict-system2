package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ictyb_sales_daily_report_person 테이블 엔티티
 * 점검일지 파트 1개 안에서 인원별 진행중/일정지연/배포건수를 기록한다.
 * 진행중·일정지연은 등록 시점에 제출된 값을 그대로 저장하는 스냅샷이며,
 * 작업지시서 테이블에서 실시간 집계하지 않는다.
 */
@Entity
@Table(name = "ictyb_sales_daily_report_person")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesDailyReportPersonVo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PERSON_SEQ")
    private Long personSeq;

    @Column(name = "REPORT_ID")
    private Long reportId;

    @Column(name = "PART_ID", length = 20, columnDefinition = "VARCHAR(20) COMMENT '파트ID'")
    private String partId;

    @Column(name = "PERSON_NM", length = 50, columnDefinition = "VARCHAR(50) COMMENT '인원명'")
    private String personNm;

    @Column(name = "IN_PROGRESS_CNT", columnDefinition = "INT DEFAULT 0 COMMENT '진행중 건수'")
    private Integer inProgressCnt;

    @Column(name = "DELAYED_CNT", columnDefinition = "INT DEFAULT 0 COMMENT '일정지연 건수'")
    private Integer delayedCnt;

    @Column(name = "DISTRIBUTED_CNT", columnDefinition = "INT DEFAULT 0 COMMENT '배포건수'")
    private Integer distributedCnt;
}
