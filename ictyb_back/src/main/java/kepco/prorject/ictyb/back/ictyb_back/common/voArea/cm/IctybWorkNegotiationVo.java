package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import lombok.*;

import jakarta.persistence.*;

/**
 * ictyb_work_negotiation 테이블 엔티티 (업무지시서 협의/피드백 표시 상태)
 */
@Entity
@Table(name = "ictyb_work_negotiation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IctybWorkNegotiationVo {

    @Id
    @Column(name = "INST_ID", length = 14) // 처리번호 (its_it_work_report.INST_ID)
    private String instId;

    @Column(name = "NEGOTIATION_YN", length = 1) // 협의여부
    private String negotiationYn;

    @Column(name = "REG_SABUN", length = 10) // 등록자사번
    private String regSabun;

    @Column(name = "REG_NAME", length = 12) // 등록자이름
    private String regName;

    @Column(name = "REG_DT", length = 14) // 등록일시
    private String regDt;
}
