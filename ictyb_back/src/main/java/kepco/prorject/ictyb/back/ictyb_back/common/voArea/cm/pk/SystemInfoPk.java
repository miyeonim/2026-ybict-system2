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
public class SystemInfoPk implements Serializable {

    @Column(name = "DOMAIN", length = 2)
    private String domain; // 대분류

    @Column(name = "SUBDOMAIN", length = 2)
    private String subdomain; // 중분류

    @Column(name = "SEQ", length = 2)
    private String seq; // 순번
}
