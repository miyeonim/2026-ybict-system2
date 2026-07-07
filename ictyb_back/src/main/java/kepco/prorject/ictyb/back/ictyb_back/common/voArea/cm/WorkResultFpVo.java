package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import lombok.*;

import jakarta.persistence.*;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.WorkResultFpPk;

/**
 * its_work_result_fp 테이블 엔티티 (유지보수후결과)
 */
@Entity
@Table(name = "its_work_result_fp")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(WorkResultFpPk.class)
public class WorkResultFpVo {

    @Id
    @Column(name = "INST_ID", length = 14) // 지시번호
    private String instId;

    @Id
    @Column(name = "SEQ", length = 3) // 순번
    private String seq;

    @Id
    @Column(name = "REQ_ID", length = 14) // 요청번호
    private String reqId;

    @Column(name = "GUBUN", length = 1) // 구분
    private String gubun;

    @Column(name = "TYPE", length = 3) // 기능유형
    private String type;

    @Column(name = "FP") // 기능점수
    private Double fp;

    @Column(name = "FP_NAME", length = 200) // 기능명
    private String fpName;

    @Column(name = "UPT_SABUN", length = 10) // 처리자사번
    private String uptSabun;

    @Column(name = "UPT_DT", length = 14) // 처리날짜
    private String uptDt;

    @Column(name = "LANG_TP_NM", length = 100) // 언어유형명
    private String langTpNm;
}
