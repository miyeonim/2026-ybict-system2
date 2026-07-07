package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import lombok.*;

import jakarta.persistence.*;

/**
 * its_sign_save 테이블 엔티티 (결재라인자동설정)
 */
@Entity
@Table(name = "its_sign_save")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignSaveVo {

    @Id
    @Column(name = "REG_SABUN", length = 10) // 현재결재자
    private String regSabun;

    @Column(name = "SUPER_SABUN", length = 10) // 상위결재자
    private String superSabun;
}
