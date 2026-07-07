package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import lombok.*;

import jakarta.persistence.*;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.ItWorkReportAttachPk;

/**
 * its_it_work_report_attach 테이블 엔티티 (지시서_IT부서_첨부)
 */
@Entity
@Table(name = "its_it_work_report_attach")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(ItWorkReportAttachPk.class)
public class ItWorkReportAttachVo {

    @Id
    @Column(name = "INST_ID", length = 14) // 처리번호
    private String instId;

    @Id
    @Column(name = "SEQ", length = 3) // 순번
    private String seq;

    @Column(name = "REAL_FILE_NAME", length = 1000) // 실제파일명
    private String realFileName;

    @Column(name = "FILE_NAME", length = 1000) // 변환파일명
    private String fileName;

    @Column(name = "FILE_LOCATION", length = 1000) // 파일위치
    private String fileLocation;

    @Column(name = "REG_DT", length = 14) // 등록일
    private String regDt;

    @Column(name = "ATTACH_TYPE", length = 2) // 첨부파일유형
    private String attachType;

    @Column(name = "FILE_SIZE", length = 10) // 파일크기
    private String fileSize;
}
