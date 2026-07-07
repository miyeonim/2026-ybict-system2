package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkHistoryPk implements Serializable {
    private String instId;
    private String seq;
}
