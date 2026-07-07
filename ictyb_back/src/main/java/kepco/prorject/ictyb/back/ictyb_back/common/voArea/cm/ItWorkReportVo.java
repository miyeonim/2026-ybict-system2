package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import jakarta.persistence.*;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.ItWorkReportPk;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "its_it_work_report")
@IdClass(ItWorkReportPk.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItWorkReportVo {

    @Id
    @Column(name = "INST_ID", length = 14, nullable = false, columnDefinition = "VARCHAR(14) COMMENT '처리번호'")
    private String instId;

    @Id
    @Column(name = "REQ_ID", length = 14, nullable = false, columnDefinition = "VARCHAR(14) COMMENT '요청번호'")
    private String reqId;

    @Column(name = "CHANGE_TITLE", length = 1000, columnDefinition = "VARCHAR(1000) COMMENT '지시제목'")
    private String changeTitle;

    @Column(name = "CHANGE_REASON", length = 4000, columnDefinition = "VARCHAR(4000) COMMENT '지시내용'")
    private String changeReason;

    @Column(name = "SERVICE_TYPE", length = 2, columnDefinition = "VARCHAR(2) COMMENT '서비스유형'")
    private String serviceType;

    @Column(name = "SYSTEM_CD", length = 6, columnDefinition = "VARCHAR(6) COMMENT '시스템코드'")
    private String systemCd;

    @Column(name = "WORK_GUBUN", length = 2, columnDefinition = "VARCHAR(2) COMMENT '작업구분'")
    private String workGubun;

    @Column(name = "EXPT_FP_CNT_EI", columnDefinition = "DECIMAL(10,0) COMMENT '예상기능_EI'")
    private BigDecimal exptFpCntEi;

    @Column(name = "EXPT_FP_CNT_EO", columnDefinition = "DECIMAL(10,0) COMMENT '예상기능_EO'")
    private BigDecimal exptFpCntEo;

    @Column(name = "EXPT_FP_CNT_EQ", columnDefinition = "DECIMAL(10,0) COMMENT '예상기능_EQ'")
    private BigDecimal exptFpCntEq;

    @Column(name = "EXPT_FP_CNT_ILF", columnDefinition = "DECIMAL(10,0) COMMENT '예상기능_ILF'")
    private BigDecimal exptFpCntIlf;

    @Column(name = "EXPT_FP_CNT_EIF", columnDefinition = "DECIMAL(10,0) COMMENT '예상기능_EIF'")
    private BigDecimal exptFpCntEif;

    @Column(name = "WORK_TYPE", length = 2, columnDefinition = "VARCHAR(2) COMMENT '작업유형'")
    private String workType;

    @Column(name = "WORK_LEVEL", length = 2, columnDefinition = "VARCHAR(2) COMMENT '작업레벨'")
    private String workLevel;

    @Column(name = "WORK_PERIOD_MANUAL", length = 5, columnDefinition = "VARCHAR(5) COMMENT '작업기간메뉴얼'")
    private String workPeriodManual;

    @Column(name = "WORK_PERIOD", length = 5, columnDefinition = "VARCHAR(5) COMMENT '작업기간'")
    private String workPeriod;

    @Column(name = "WORK_CHANGE_PERIOD", length = 5, columnDefinition = "VARCHAR(5) COMMENT '변경기간'")
    private String workChangePeriod;

    @Column(name = "WORK_CHANGE_PERIOD_CMT", length = 4000, columnDefinition = "VARCHAR(4000) COMMENT '변경기간_사유'")
    private String workChangePeriodCmt;

    @Column(name = "EXPECTED_FINISHED_DT", length = 14, columnDefinition = "VARCHAR(14) COMMENT '완료예정일'")
    private String expectedFinishedDt;

    @Column(name = "WORK_CODE", length = 3, columnDefinition = "VARCHAR(3) COMMENT '형상연계용작업코드'")
    private String workCode;

    @Column(name = "REQUEST_CODE", length = 3, columnDefinition = "VARCHAR(3) COMMENT '형상연계용요청코드'")
    private String requestCode;

    @Column(name = "WORK_START_DT", length = 14, columnDefinition = "VARCHAR(14) COMMENT '지시서시작일'")
    private String workStartDt;

    @Column(name = "WORK_END_DT", length = 14, columnDefinition = "VARCHAR(14) COMMENT '지시서종료일'")
    private String workEndDt;

    @Column(name = "REPORT_DT", length = 14, columnDefinition = "VARCHAR(14) COMMENT '작업보고일'")
    private String reportDt;

    @Column(name = "IS_SUCCESS", length = 1, columnDefinition = "VARCHAR(1) COMMENT '합격여부'")
    private String isSuccess;

    @Column(name = "SATISFY_POINT", length = 3, columnDefinition = "VARCHAR(3) COMMENT '만족도'")
    private String satisfyPoint;

    @Column(name = "SATISFY_CNT", length = 4000, columnDefinition = "VARCHAR(4000) COMMENT '만족도의견'")
    private String satisfyCnt;

    @Column(name = "ACT_ID", length = 3, columnDefinition = "VARCHAR(3) COMMENT '진행단계'")
    private String actId;

    @Column(name = "APPROVE1_SABUN", length = 10, columnDefinition = "VARCHAR(10) COMMENT '1차결재자사번'")
    private String approve1Sabun;

    @Column(name = "APPROVE1_NAME", length = 12, columnDefinition = "VARCHAR(12) COMMENT '1차결재자이름'")
    private String approve1Name;

    @Column(name = "APPROVE1_DT", length = 14, columnDefinition = "VARCHAR(14) COMMENT '1차결재일'")
    private String approve1Dt;

    @Column(name = "APPROVE2_SABUN", length = 10, columnDefinition = "VARCHAR(10) COMMENT '2차결재자사번'")
    private String approve2Sabun;

    @Column(name = "APPROVE2_NAME", length = 12, columnDefinition = "VARCHAR(12) COMMENT '2차결재자이름'")
    private String approve2Name;

    @Column(name = "APPROVE2_DT", length = 14, columnDefinition = "VARCHAR(14) COMMENT '2차결재일'")
    private String approve2Dt;

    @Column(name = "APPROVE3_SABUN", length = 10, columnDefinition = "VARCHAR(10) COMMENT '3차결재자사번'")
    private String approve3Sabun;

    @Column(name = "APPROVE3_NAME", length = 12, columnDefinition = "VARCHAR(12) COMMENT '3차결재자이름'")
    private String approve3Name;

    @Column(name = "APPROVE3_DT", length = 14, columnDefinition = "VARCHAR(14) COMMENT '3차결재일'")
    private String approve3Dt;

    @Column(name = "REG_USER_SABUN", length = 10, columnDefinition = "VARCHAR(10) COMMENT '등록자사번'")
    private String regUserSabun;

    @Column(name = "REG_USER_NAME", length = 12, columnDefinition = "VARCHAR(12) COMMENT '등록자이름'")
    private String regUserName;

    @Column(name = "REG_USER_DEP_CD", length = 16, columnDefinition = "VARCHAR(16) COMMENT '등록자부서코드'")
    private String regUserDepCd;

    @Column(name = "REG_USER_DEP_NM", length = 500, columnDefinition = "VARCHAR(500) COMMENT '등록자부서명'")
    private String regUserDepNm;

    @Column(name = "REG_DT", length = 14, columnDefinition = "VARCHAR(14) COMMENT '등록일'")
    private String regDt;

    @Column(name = "WORKER_SABUN", length = 10, columnDefinition = "VARCHAR(10) COMMENT '작업자ID'")
    private String workerSabun;

    @Column(name = "WORKER_NAME", length = 30, columnDefinition = "VARCHAR(30) COMMENT '작업자이름'")
    private String workerName;

    @Column(name = "WORKER_DEP_CD", length = 8, columnDefinition = "VARCHAR(8) COMMENT '작업부서'")
    private String workerDepCd;

    @Column(name = "WORKER_WORK_CNT", length = 3, columnDefinition = "VARCHAR(3) COMMENT '작업자작업건수'")
    private String workerWorkCnt;

    @Column(name = "WORKER_WORK_DAYS", length = 5, columnDefinition = "VARCHAR(5) COMMENT '작업자작업일수'")
    private String workerWorkDays;

    @Column(name = "ITMS_INST_ID", length = 10, columnDefinition = "VARCHAR(10) COMMENT 'ITMS처리번호'")
    private String itmsInstId;

    @Column(name = "EXPT_FP_SUM", columnDefinition = "DECIMAL(10,0) COMMENT '예상기능점수합계'")
    private BigDecimal exptFpSum;

    @Column(name = "CHANGE_ROOT", length = 2000, columnDefinition = "VARCHAR(2000) COMMENT '변경사유'")
    private String changeRoot;

    @Column(name = "TEST_MODE_YN", length = 1, columnDefinition = "VARCHAR(1) COMMENT '테스트모드여부'")
    private String testModeYn;

    @Column(name = "USER_MANUAL_YN", length = 1, columnDefinition = "VARCHAR(1) COMMENT '사용자메뉴얼갱신여부'")
    private String userManualYn;

    @Column(name = "DEPLOY_DT", length = 14, columnDefinition = "VARCHAR(14) COMMENT '배포일'")
    private String deployDt;

    @Column(name = "OPPB_YN", length = 1, columnDefinition = "VARCHAR(1) COMMENT '공개여부'")
    private String oppbYn;

    @Column(name = "DRS_IMPT_YN", length = 1, columnDefinition = "VARCHAR(1) COMMENT '재해복구시스템영향여부'")
    private String drsImptYn;
}