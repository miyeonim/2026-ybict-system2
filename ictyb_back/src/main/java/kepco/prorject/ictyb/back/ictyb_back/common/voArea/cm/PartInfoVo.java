package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import jakarta.persistence.*;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.PartInfoPk;
import lombok.Data;

@Data
@Entity
@Table(name = "ybict_part_info")
@IdClass(PartInfoPk.class)
public class PartInfoVo {

    @Column(name = "PAR_DEP_ID", nullable = false, columnDefinition = "VARCHAR(20) COMMENT '상위부서ID'")
    private String parDepId;

    @Id
    @Column(name = "DEP_ID", nullable = false, columnDefinition = "VARCHAR(20) COMMENT '부서ID'")
    private String depId;

    @Column(name = "DEP_TITLE", columnDefinition = "VARCHAR(100) COMMENT '부서명'")
    private String depTitle;

    @Id
    @Column(name = "PART_ID", nullable = false, columnDefinition = "VARCHAR(20) COMMENT '파트ID'")
    private String partId;

    @Column(name = "PART_NM", columnDefinition = "VARCHAR(100) COMMENT '파트명'")
    private String partNm;

    @Column(name = "PART_ORDER", columnDefinition = "INT DEFAULT 0 COMMENT '파트순번'")
    private Integer partOrder;

    @Column(name = "PART_START_DT", columnDefinition = "DATE COMMENT '파트시작일'")
    private java.time.LocalDate partStartDt;

    @Column(name = "PART_END_DT", columnDefinition = "DATE COMMENT '파트종료일'")
    private java.time.LocalDate partEndDt;

    @Column(name = "USE_YN", columnDefinition = "CHAR(1) DEFAULT 'Y' COMMENT '사용여부'")
    private String useYn;
}
