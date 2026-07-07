package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk;

import lombok.*;

import java.io.Serializable;

/**
 * its_user_info_secret 복합 PK (INST_ID + DOC_TYPE + REQ_ID)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserInfoSecretPk implements Serializable {
    private String instId;
    private String docType;
    private String reqId;
}
