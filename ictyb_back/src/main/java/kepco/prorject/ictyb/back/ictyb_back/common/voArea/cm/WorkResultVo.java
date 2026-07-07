package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import lombok.*;

import jakarta.persistence.*;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.WorkResultPk;

/**
 * its_work_result 테이블 엔티티 (결과보고_KDN)
 */
@Entity
@Table(name = "its_work_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(WorkResultPk.class)
public class WorkResultVo {

    @Id
    @Column(name = "INST_ID", length = 14) // 처리번호
    private String instId;

    @Id
    @Column(name = "REQ_ID", length = 14) // 요청번호
    private String reqId;

    @Column(name = "RESULT", length = 4000) // 작업결과
    private String result;

    @Column(name = "WORKER_ID", length = 8) // 작업자ID
    private String workerId;

    @Column(name = "WORKER_NAME", length = 12) // 작업자이름
    private String workerName;

    @Column(name = "WORKER_DEP_NM", length = 1000) // 작업자부서명
    private String workerDepNm;

    @Column(name = "WORKER_TEL", length = 20) // 작업자전화
    private String workerTel;

    @Column(name = "REG_DT", length = 14) // 등록일
    private String regDt;

    @Column(name = "QRY_CTT", length = 4000) // 쿼리내용
    private String qryCtt;
}
