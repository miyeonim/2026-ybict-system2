package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import lombok.*;

import jakarta.persistence.*;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.BaseVo;

/**
 * its_work_opinion_req 테이블 엔티티 (의견요청 내역)
 */
@Entity
@Table(name = "its_work_opinion_req")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class WorkOpinionReqVo extends BaseVo {

    @Id
    @Column(name = "OPN_ID", length = 20) // 의견ID
    private String opnId;

    @Column(name = "FRST_REGR_EMPNO", length = 10) // 최초등록사번
    private String frstRegrEmpno;

    @Column(name = "LST_CHGR_EMPNO", length = 10) // 최종변경사번
    private String lstChgrEmpno;

    @Column(name = "INSTR_NO", length = 20) // 지시번호
    private String instrNo;

    @Column(name = "REQ_ID", length = 30) // 청구ID
    private String reqId;

    @Column(name = "OPN_CTT", length = 4000) // 의견내용
    private String opnCtt;
}
