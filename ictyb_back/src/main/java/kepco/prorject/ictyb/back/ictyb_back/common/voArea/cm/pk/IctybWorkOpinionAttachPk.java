package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk;

import lombok.*;

import java.io.Serializable;

/**
 * ictyb_work_opinion_attach 복합 PK (CMNT_ID + SEQ_NO)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class IctybWorkOpinionAttachPk implements Serializable {
    private String cmntId;
    private Long seqNo;
}
