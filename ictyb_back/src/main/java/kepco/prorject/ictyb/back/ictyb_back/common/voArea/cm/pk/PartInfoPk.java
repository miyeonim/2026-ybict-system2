package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartInfoPk implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String depId;   // 부서ID
    private String partId;  // 파트ID
}