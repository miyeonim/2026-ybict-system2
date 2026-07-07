package kepco.prorject.ictyb.back.ictyb_back.common.voArea.qa.pk;

import lombok.*;

import java.io.Serializable;

/**
 * its_notice_attach 복합 PK (NOTICE_NO + SEQ)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class NoticeAttachPk implements Serializable {
    private String noticeNo;
    private String seq;
}
