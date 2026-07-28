package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CodePk implements Serializable {

    @Column(name = "GUBUN", length = 2)
    private String gubun; // 구분

    @Column(name = "CODE", length = 2)
    private String code; // 코드
}
