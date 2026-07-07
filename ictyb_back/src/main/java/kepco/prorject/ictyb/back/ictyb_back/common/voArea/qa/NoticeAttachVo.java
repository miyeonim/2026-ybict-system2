package kepco.prorject.ictyb.back.ictyb_back.common.voArea.qa;

import lombok.*;

import jakarta.persistence.*;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.qa.pk.NoticeAttachPk;

/**
 * its_notice_attach 테이블 엔티티
 */
@Entity
@Table(name = "its_notice_attach")
@Getter
@Setter 
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(NoticeAttachPk.class)
public class NoticeAttachVo {

    @Id
    @Column(name = "NOTICE_NO", length = 14)
    private String noticeNo;

    @Id
    @Column(name = "SEQ", length = 3)
    private String seq;

    @Column(name = "REAL_FILE_NAME", length = 1000)
    private String realFileName;

    @Column(name = "FILE_NAME", length = 1000)
    private String fileName;

    @Column(name = "FILE_LOCATION", length = 1000)
    private String fileLocation;

    @Column(name = "REG_DT", length = 14)
    private String regDt;

    @Column(name = "FILE_SIZE", length = 10)
    private String fileSize;

    //파일 확장자 1글자 
    @Column(name = "ATTACH_TYPE", length = 1)
    private String attachType;

    //파일 전체 확장자
    @Column(name = "ATTACH_FULL_TYPE", length = 10)
    private String attachFullType;

}