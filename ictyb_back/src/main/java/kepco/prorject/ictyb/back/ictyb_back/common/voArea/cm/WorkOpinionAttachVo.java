package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import lombok.*;

import jakarta.persistence.*;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.BaseVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.WorkOpinionAttachPk;

/**
 * its_work_opinion_attach 테이블 엔티티 (의견요청 첨부파일)
 */
@Entity
@Table(name = "its_work_opinion_attach")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@IdClass(WorkOpinionAttachPk.class)
public class WorkOpinionAttachVo extends BaseVo {

    @Id
    @Column(name = "OPN_ID", length = 20) // 의견ID
    private String opnId;

    @Id
    @Column(name = "SEQ_NO") // 시퀀스번호
    private Long seqNo;

    @Column(name = "FRST_REGR_EMPNO", length = 10) // 최초등록자사번
    private String frstRegrEmpno;

    @Column(name = "LST_CHGR_EMPNO", length = 10) // 최종변경자사번
    private String lstChgrEmpno;

    @Column(name = "RL_FILE_NM", length = 255) // 실제파일명
    private String rlFileNm;

    @Column(name = "FILE_NM", length = 100) // 파일명
    private String fileNm;

    @Column(name = "FILE_PTH_CTT", length = 500) // 파일경로내용
    private String filePthCtt;

    @Column(name = "FILE_CPCT") // 파일용량
    private Long fileCpct;
}
