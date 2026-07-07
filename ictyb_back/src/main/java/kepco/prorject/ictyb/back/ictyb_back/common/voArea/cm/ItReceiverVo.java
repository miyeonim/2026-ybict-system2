package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import lombok.*;

import jakarta.persistence.*;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.ItReceiverPk;

/**
 * its_it_receiver 테이블 엔티티 (IT부서_접수자)
 */
@Entity
@Table(name = "its_it_receiver")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(ItReceiverPk.class)
public class ItReceiverVo {

    @Id
    @Column(name = "INST_ID", length = 14) // 문서번호
    private String instId;

    @Id
    @Column(name = "SABUN", length = 10) // 사번
    private String sabun;

    @Column(name = "DEP_ID", length = 10) // 부서코드
    private String depId;

    @Column(name = "REG_DT", length = 14) // 등록일
    private String regDt;
}
