package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import java.time.LocalDate;

import lombok.*;

import jakarta.persistence.*;

/**
 * its_rcll_log 테이블 엔티티 (요청서회수관리)
 */
@Entity
@Table(name = "its_rcll_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RcllLogVo {

    @Id
    @Column(name = "RCLL_CL_SEQNO", length = 20) // 회수구분순번
    private String rcllClSeqno;

    @Column(name = "FRST_REGR_DT") // 최초등록일시
    private LocalDate frstRegrDt;

    @Column(name = "FRST_REGR_EMPNO", length = 10) // 최초등록자사번
    private String frstRegrEmpno;

    @Column(name = "LST_CHG_DT") // 최종변경일시
    private LocalDate lstChgDt;

    @Column(name = "LST_CHGR_EMPNO", length = 10) // 최종변경자사번
    private String lstChgrEmpno;

    @Column(name = "REQ_ID", length = 30) // 청구ID
    private String reqId;

    @Column(name = "INSTR_NO", length = 20) // 지시번호
    private String instrNo;

    @Column(name = "RCLL_RSN_CTT", length = 4000) // 회수사유내용
    private String rcllRsnCtt;
}
