package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import lombok.*;

import jakarta.persistence.*;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.RealWorkReportAttachPk;

/**
 * its_real_work_report_attach 테이블 엔티티 (요청서_실무부서_첨부)
 */
@Entity
@Table(name = "its_real_work_report_attach")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(RealWorkReportAttachPk.class)
public class RealWorkReportAttachVo {

    @Id
    @Column(name = "INST_ID", length = 14) // 요청번호
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

    @Column(name = "DEL_FLAG", length = 2) // 삭제여부
    private String delFlag;

    @Column(name = "FILE_SIZE", length = 10) // 파일크기
    private String fileSize;
}
