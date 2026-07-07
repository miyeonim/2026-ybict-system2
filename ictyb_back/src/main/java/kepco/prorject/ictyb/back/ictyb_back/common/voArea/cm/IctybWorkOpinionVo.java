package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import lombok.*;

import jakarta.persistence.*;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.BaseVo;

import java.util.UUID;

/**
 * ictyb_work_opinion 테이블 엔티티 (업무 협의 스레드)
 */
@Entity
@Table(name = "ictyb_work_opinion")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class IctybWorkOpinionVo extends BaseVo {

    @Id
    @Column(name = "OPN_ID", length = 20) // 협의ID
    private String opnId;

    @PrePersist
    protected void prePersist() {
        if (opnId == null) {
            opnId = UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        }
    }

    @Column(name = "FRST_REGR_EMPNO", length = 10) // 최초등록자사번
    private String frstRegrEmpno;

    @Column(name = "LST_CHGR_EMPNO", length = 10) // 최종변경자사번
    private String lstChgrEmpno;

    @Column(name = "INSTR_NO", length = 20) // 지시번호
    private String instrNo;

    @Column(name = "OPN_TITLE", length = 200) // 협의 제목
    private String opnTitle;

    @Column(name = "WRTR_EMPNO", length = 10) // 작성자사번
    private String wrtrEmpno;

    @Column(name = "WRTR_NM", length = 60) // 작성자명
    private String wrtrNm;
}
