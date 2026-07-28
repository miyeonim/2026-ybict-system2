package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import jakarta.persistence.*;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.CodePk;
import lombok.*;

@Entity
@Table(name = "its_code")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeVo {

    @EmbeddedId
    private CodePk id; // 복합키 (구분, 코드)

    @Column(name = "BIGO", length = 100)
    private String bigo; // 비고

    @Column(name = "CODE_NAME", length = 30)
    private String codeName; // 코드이름

    @Column(name = "UPT_DT", length = 14)
    private String uptDt; // 등록일

    @Column(name = "VIEW_ORDER", precision = 2, scale = 0)
    private Integer viewOrder; // 순번
}
