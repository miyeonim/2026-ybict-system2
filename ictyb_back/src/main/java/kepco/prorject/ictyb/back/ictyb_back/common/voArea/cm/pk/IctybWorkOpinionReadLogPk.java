package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk;

import lombok.*;

import java.io.Serializable;

/**
 * ictyb_work_opinion_read_log 복합 PK (OPN_ID + SABUN)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class IctybWorkOpinionReadLogPk implements Serializable {
    private String opnId;
    private String sabun;
}
