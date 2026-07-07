package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import jakarta.persistence.*;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.WorkHistoryPk;
import lombok.Data;
import org.hibernate.annotations.Comment;

@Data
@Entity
@Table(name = "its_work_history")
@IdClass(WorkHistoryPk.class)
@Comment("결재이력")
public class WorkHistoryVo {

    @Id
    @Column(name = "INST_ID", length = 14, columnDefinition = "VARCHAR(14) COMMENT '처리번호'")
    private String instId;

    @Id
    @Column(name = "SEQ", length = 3, columnDefinition = "VARCHAR(3) COMMENT '순번'")
    private String seq;

    @Column(name = "REG_SABUN", length = 8, columnDefinition = "VARCHAR(8) COMMENT '결재자사번'")
    private String regSabun;

    @Column(name = "ACT_ID", length = 3, columnDefinition = "VARCHAR(3) COMMENT '작업ID'")
    private String actId;

    @Column(name = "ACT_ID_NM", length = 200, columnDefinition = "VARCHAR(200) COMMENT '작업한글명'")
    private String actIdNm;

    @Column(name = "ACT_SIGN", length = 1, columnDefinition = "VARCHAR(1) COMMENT '유형코드'")
    private String actSign;

    @Column(name = "REG_NAME", length = 12, columnDefinition = "VARCHAR(12) COMMENT '결재자이름'")
    private String regName;

    @Column(name = "REG_DT", length = 14, columnDefinition = "VARCHAR(14) COMMENT '결재일'")
    private String regDt;

    @Column(name = "REG_CNTNT", length = 4000, columnDefinition = "VARCHAR(4000) COMMENT '결재의견'")
    private String regCntnt;
}