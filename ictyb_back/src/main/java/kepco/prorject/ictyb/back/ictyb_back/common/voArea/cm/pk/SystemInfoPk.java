package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SystemInfoPk implements Serializable {
    private String domain;
    private String subdomain;
    private String seq;
}
