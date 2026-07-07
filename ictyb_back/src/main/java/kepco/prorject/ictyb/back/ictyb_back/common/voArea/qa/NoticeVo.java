package kepco.prorject.ictyb.back.ictyb_back.common.voArea.qa;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import jakarta.persistence.*;
/**
 * its_notice 테이블 엔티티 (Q&A: NOTICE_TYPE = 'Q')
 */
@Entity
@Table(name = "its_notice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
public class NoticeVo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NOTICE_NO")
    private Long noticeNo;

    @Column(name = "NOTICE_TITLE", length = 1000)
    private String noticeTitle;

    @Column(name = "NOTICE_DEP_CD", length = 16)
    private String noticeDepCd;

    @Column(name = "NOTICE_CONTENTS", length = 4000)
    private String noticeContents;

    @Column(name = "PRIORITY")
    private Integer priority;

    @Column(name = "REG_USER_SABUN", length = 10)
    private String regUserSabun;

    @Column(name = "REG_USER_DEP_CD", length = 16)
    private String regUserDepCd;

    @Column(name = "REG_USER_NAME", length = 12)
    private String regUserName;

    @Column(name = "REG_DT", length = 14)
    private String regDt;

    @Column(name = "END_DT", length = 14)
    private String endDt;

    @Column(name = "DEL_YN", length = 1)
    private String delYn;

    @Column(name = "VIEW_CNT")
    private Integer viewCnt;

    /** 'Q' = Q&A, 'N' = 공지사항, 'D' = 자료실 */
    @Column(name = "NOTICE_TYPE", length = 1)
    private String noticeType;
}
