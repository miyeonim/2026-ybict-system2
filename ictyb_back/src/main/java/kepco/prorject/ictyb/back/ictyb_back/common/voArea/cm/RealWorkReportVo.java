package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import lombok.*;

import jakarta.persistence.*;

/**
 * its_real_work_report 테이블 엔티티 (요청서_실무부서)
 */
@Entity
@Table(name = "its_real_work_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RealWorkReportVo {

    @Id
    @Column(name = "INST_ID", length = 14) // 요청번호
    private String instId;

    @Column(name = "CHANGE_TITLE", length = 1000) // 요청서제목
    private String changeTitle;

    @Column(name = "CHANGE_REASON", length = 4000) // 요청서내용
    private String changeReason;

    @Column(name = "UPMU_TYPE", length = 3) // 업무유형_SEQ
    private String upmuType;

    @Column(name = "UPMU_TYPE_NAME1", length = 100) // 업무유형한글명1
    private String upmuTypeName1;

    @Column(name = "UPMU_TYPE_NAME2", length = 100) // 업무유형한글명2
    private String upmuTypeName2;

    @Column(name = "SERVICE_TYPE", length = 2) // 서비스유형
    private String serviceType;

    @Column(name = "SERVICE_TYPE_NM", length = 30) // 서비스유형한글명
    private String serviceTypeNm;

    @Column(name = "STATUS_CD", length = 3) // 현상태코드
    private String statusCd;

    @Column(name = "STATUS_NM", length = 20) // 현상태값
    private String statusNm;

    @Column(name = "EXPECTED_DT", length = 14) // 희망완료일
    private String expectedDt;

    @Column(name = "WORK_START_DT", length = 14) // 요청일
    private String workStartDt;

    @Column(name = "WORK_END_DT", length = 14) // 종료일
    private String workEndDt;

    @Column(name = "APPROVE1_SABUN", length = 10) // 1차결재자사번
    private String approve1Sabun;

    @Column(name = "APPROVE1_NAME", length = 12) // 1차결재자이름
    private String approve1Name;

    @Column(name = "APPROVE1_DT", length = 14) // 1차결재일
    private String approve1Dt;

    @Column(name = "APPROVE2_SABUN", length = 10) // 2차결재자사번
    private String approve2Sabun;

    @Column(name = "APPROVE2_NAME", length = 12) // 2차결재자이름
    private String approve2Name;

    @Column(name = "APPROVE2_DT", length = 14) // 2차결재일
    private String approve2Dt;

    @Column(name = "APPROVE3_SABUN", length = 10) // 3차결재자사번
    private String approve3Sabun;

    @Column(name = "APPROVE3_NAME", length = 12) // 3차결재자이름
    private String approve3Name;

    @Column(name = "APPROVE3_DT", length = 14) // 3차결재일
    private String approve3Dt;

    @Column(name = "SEND_IT_SABUN", length = 10) // 수신자사번
    private String sendItSabun;

    @Column(name = "SEND_IT_NAME", length = 12) // 수신자이름
    private String sendItName;

    @Column(name = "WORK_SABUN", length = 10) // 작업자사번
    private String workSabun;

    @Column(name = "WORK_NAME", length = 12) // 작업자이름
    private String workName;

    @Column(name = "REG_USER_SABUN", length = 10) // 등록자사번
    private String regUserSabun;

    @Column(name = "REG_USER_NAME", length = 12) // 등록자이름
    private String regUserName;

    @Column(name = "REG_USER_DEP_CD", length = 16) // 등록자소속코드
    private String regUserDepCd;

    @Column(name = "REG_USER_DEP_NM", length = 500) // 등록자소속명
    private String regUserDepNm;

    @Column(name = "SATISFY_POINT", length = 3) // 만족도점수
    private String satisfyPoint;

    @Column(name = "SATISFY_CNT", length = 4000) // 만족도내용
    private String satisfyCnt;

    @Column(name = "SMSFLG", length = 1) // SMS수신여부
    private String smsflg;

    @Column(name = "SYSTEM_CD", length = 6) // 시스템코드
    private String systemCd;

    @Column(name = "TESTFLAG", length = 2) // 요청자테스트참여여부
    private String testflag;

    @Column(name = "TEST_MODE_YN", length = 1) // 테스트여부
    private String testModeYn;

    @Column(name = "CHG_RSN_CTT", length = 4000) // 변경사유내용
    private String chgRsnCtt;

    @Column(name = "OPPB_YN", length = 1) // 공개여부
    private String oppbYn;
}
