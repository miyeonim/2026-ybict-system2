package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import lombok.*;

import jakarta.persistence.*;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.WorkResultAttachPk;

/**
 * its_work_result_attach 테이블 엔티티 (결과보고_KDN첨부)
 */
@Entity
@Table(name = "its_work_result_attach")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(WorkResultAttachPk.class)
public class WorkResultAttachVo {

    @Id
    @Column(name = "INST_ID", length = 14) // 처리번호
    private String instId;

    @Id
    @Column(name = "SEQ", length = 3) // 순번
    private String seq;

    @Id
    @Column(name = "REQ_ID", length = 14) // 요청번호
    private String reqId;

    @Column(name = "REAL_FILE_NAME", length = 1000) // 실제파일명
    private String realFileName;

    @Column(name = "FILE_NAME", length = 1000) // 변환파일명
    private String fileName;

    @Column(name = "FILE_LOCATION", length = 1000) // 파일위치
    private String fileLocation;

    @Column(name = "REG_DT", length = 14) // 등록일
    private String regDt;

    @Column(name = "ITMSFLG", length = 1) // ITMS구분값
    private String itmsflg;

    @Column(name = "ATTACH_TYPE", length = 2) // 첨부파일유형
    private String attachType;

    @Column(name = "FILE_SIZE", length = 10) // 파일크기
    private String fileSize;
}
