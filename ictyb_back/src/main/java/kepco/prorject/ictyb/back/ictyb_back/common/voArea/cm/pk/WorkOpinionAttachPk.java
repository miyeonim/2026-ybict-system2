package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk;

import lombok.*;

import java.io.Serializable;

/**
 * its_work_opinion_attach 복합 PK (OPN_ID + SEQ_NO)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class WorkOpinionAttachPk implements Serializable {
    private String opnId;
    private Long seqNo;
}
