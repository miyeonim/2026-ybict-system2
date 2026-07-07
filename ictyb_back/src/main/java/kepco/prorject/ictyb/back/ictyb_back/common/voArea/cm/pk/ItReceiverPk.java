package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk;

import lombok.*;

import java.io.Serializable;

/**
 * its_it_receiver 복합 PK (INST_ID + SABUN)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ItReceiverPk implements Serializable {
    private String instId;
    private String sabun;
}
