package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk;

import lombok.*;

import java.io.Serializable;

/**
 * its_work_result_attach 복합 PK (INST_ID + SEQ + REQ_ID)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class WorkResultAttachPk implements Serializable {
    private String instId;
    private String seq;
    private String reqId;
}
