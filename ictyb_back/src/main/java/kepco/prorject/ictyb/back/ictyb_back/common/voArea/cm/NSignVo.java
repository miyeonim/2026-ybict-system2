package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import lombok.*;

import jakarta.persistence.*;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.NSignPk;

/**
 * its_n_sign 테이블 엔티티 (현재결재자정보)
 */
@Entity
@Table(name = "its_n_sign")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(NSignPk.class)
public class NSignVo {

    @Id
    @Column(name = "INST_ID", length = 14) // 요청/처리번호
    private String instId;

    @Id
    @Column(name = "SABUN", length = 10) // 사번
    private String sabun;

    @Column(name = "ACT_ID", length = 3) // 진행단계
    private String actId;

    @Column(name = "NAME", length = 10) // 이름
    private String name;
}
