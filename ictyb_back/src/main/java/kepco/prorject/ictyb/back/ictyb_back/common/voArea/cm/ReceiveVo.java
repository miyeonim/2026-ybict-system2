package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import lombok.*;

import jakarta.persistence.*;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.ReceivePk;

/**
 * its_receive 테이블 엔티티 (IT부서수신자)
 */
@Entity
@Table(name = "its_receive")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(ReceivePk.class)
public class ReceiveVo {

    @Id
    @Column(name = "INST_ID", length = 14) // 지시서번호
    private String instId;

    @Id
    @Column(name = "SABUN", length = 10) // 사번
    private String sabun;

    @Column(name = "NAME", length = 10) // 이름
    private String name;
}
