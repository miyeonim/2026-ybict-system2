package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ItWorkReportPk implements Serializable {
    
    @Column(name = "INST_ID", length = 14, nullable = false, columnDefinition = "VARCHAR(14) COMMENT '처리번호'")
    private String instId;

    @Column(name = "REQ_ID", length = 14, nullable = false, columnDefinition = "VARCHAR(14) COMMENT '요청번호'")
    private String reqId;
}