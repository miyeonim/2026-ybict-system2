package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.BaseVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ictyb_sales_daily_report 테이블 엔티티 (영업 점검일지 헤더)
 */
@Entity
@Table(name = "ictyb_sales_daily_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesDailyReportVo extends BaseVo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REPORT_ID")
    private Long reportId;

    @Column(name = "REPORT_DATE", columnDefinition = "DATE COMMENT '작성일'")
    private LocalDate reportDate;

    @Column(name = "AUTHOR_SABUN", length = 20, columnDefinition = "VARCHAR(20) COMMENT '작성자 사번'")
    private String authorSabun;

    @Column(name = "AUTHOR_NAME", length = 50, columnDefinition = "VARCHAR(50) COMMENT '작성자명'")
    private String authorName;
}
