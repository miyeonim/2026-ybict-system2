package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.SalesDailyReportAttachPk;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ictyb_sales_daily_report_attach 테이블 엔티티 (점검일지 파트별 첨부파일)
 */
@Entity
@Table(name = "ictyb_sales_daily_report_attach")
@IdClass(SalesDailyReportAttachPk.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesDailyReportAttachVo {

    @Id
    @Column(name = "REPORT_ID")
    private Long reportId;

    @Id
    @Column(name = "PART_ID", length = 20)
    private String partId;

    @Id
    @Column(name = "SEQ", length = 3)
    private String seq;

    @Column(name = "REAL_FILE_NAME", length = 1000, columnDefinition = "VARCHAR(1000) COMMENT '실제파일명'")
    private String realFileName;

    @Column(name = "FILE_NAME", length = 1000, columnDefinition = "VARCHAR(1000) COMMENT '변환파일명'")
    private String fileName;

    @Column(name = "FILE_LOCATION", length = 1000, columnDefinition = "VARCHAR(1000) COMMENT '파일위치'")
    private String fileLocation;

    @Column(name = "FILE_SIZE", columnDefinition = "BIGINT COMMENT '파일크기(byte)'")
    private Long fileSize;

    @Column(name = "ATTACH_FULL_TYPE", length = 10, columnDefinition = "VARCHAR(10) COMMENT '확장자'")
    private String attachFullType;

    @Column(name = "REG_DT", columnDefinition = "DATETIME COMMENT '등록일시'")
    private LocalDateTime regDt;
}
