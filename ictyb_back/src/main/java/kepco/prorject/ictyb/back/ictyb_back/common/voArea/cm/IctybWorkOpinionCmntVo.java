package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import lombok.*;

import jakarta.persistence.*;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.BaseVo;

import java.util.UUID;

/**
 * ictyb_work_opinion_cmnt 테이블 엔티티 (업무 협의 댓글)
 */
@Entity
@Table(name = "ictyb_work_opinion_cmnt")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class IctybWorkOpinionCmntVo extends BaseVo {

    @Id
    @Column(name = "CMNT_ID", length = 20) // 댓글ID
    private String cmntId;

    @PrePersist
    protected void prePersist() {
        if (cmntId == null) {
            cmntId = UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        }
    }

    @Column(name = "FRST_REGR_EMPNO", length = 10) // 최초등록자사번
    private String frstRegrEmpno;

    @Column(name = "LST_CHGR_EMPNO", length = 10) // 최종변경자사번
    private String lstChgrEmpno;

    @Column(name = "OPN_ID", length = 20) // 협의ID (부모)
    private String opnId;

    @Column(name = "CMNT_CTT", length = 4000) // 댓글내용
    private String cmntCtt;

    @Column(name = "WRTR_EMPNO", length = 10) // 작성자사번
    private String wrtrEmpno;

    @Column(name = "WRTR_NM", length = 60) // 작성자명
    private String wrtrNm;

    @Column(name = "WRTR_ROLE_NM", length = 100) // 작성자 역할명 (예: 실무자, 한전 담당자)
    private String wrtrRoleNm;
}
