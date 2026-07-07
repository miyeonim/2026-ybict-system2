package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk;

import lombok.*;

import java.io.Serializable;

/**
 * its_it_work_report_attach 복합 PK (INST_ID + SEQ)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ItWorkReportAttachPk implements Serializable {
    private String instId;
    private String seq;
}
