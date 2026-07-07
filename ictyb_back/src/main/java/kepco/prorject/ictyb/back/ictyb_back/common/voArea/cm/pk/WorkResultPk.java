package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk;

import lombok.*;

import java.io.Serializable;

/**
 * its_work_result 복합 PK (INST_ID + REQ_ID)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class WorkResultPk implements Serializable {
    private String instId;
    private String reqId;
}
