package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk;

import lombok.*;

import java.io.Serializable;

/**
 * its_n_sign 복합 PK (INST_ID + SABUN)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class NSignPk implements Serializable {
    private String instId;
    private String sabun;
}
