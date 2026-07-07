package kepco.prorject.ictyb.back.ictyb_back.work_my.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.ItWorkReportVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.ItWorkReportPk;

/**
 * its_it_work_report 레포지토리 (작업지시서 - MY)
 */
@Repository
public interface ItWorkReportRepository extends JpaRepository<ItWorkReportVo, ItWorkReportPk> {

    /** 진행 중 탭: 작업자 기준 조회 */
    List<ItWorkReportVo> findByWorkerSabun(String workerSabun);

    /** 관련자 판단(등록자) 기준 조회 */
    List<ItWorkReportVo> findByRegUserSabun(String regUserSabun);

    /**
     * 관련자 판단(결재자) 기준 조회.
     * 결재선 변경/회수 시 its_work_history 이력이 현재 상태와 어긋날 수 있어,
     * 이력 테이블이 아닌 문서에 기록된 결재자 필드(approve1~3Sabun)를 기준으로 판단한다.
     */
    @Query("SELECT r FROM ItWorkReportVo r WHERE :sabun IN (r.approve1Sabun, r.approve2Sabun, r.approve3Sabun)")
    List<ItWorkReportVo> findByApproverSabun(@Param("sabun") String sabun);

    /** 관련자 판단 후 instId 목록으로 일괄 조회 */
    List<ItWorkReportVo> findByInstIdIn(List<String> instIds);

    //[업무지시서(MY) 목록 조회]///////////////////////////////////////////////////////////////////////////
    /**
     * 관련자 판단으로 추려진 INST_ID 목록을 대상으로, work_all.repository.WorkAllRepository와 동일한
     * 사번 결정/부서·파트 매칭 규칙으로 목록을 조회한다 (결재대기·피드백 오버라이드는 서비스에서 적용).
     * 사번 결정: WORKER_SABUN이 있으면 그대로 사용. 없으면 109(작업결과 보고 단계)의
     * REG_SABUN을 사용하되, 109 이전 단계(104~108)에 ACT_SIGN='R'(반송)이 하나라도 있으면 미확정 처리한다.
     */
    @Query(value = """
        WITH
        resolved AS (
            SELECT
                r.INST_ID, r.CHANGE_TITLE, r.ACT_ID,
                r.APPROVE3_DT, r.EXPECTED_FINISHED_DT, r.REG_USER_DEP_NM,
                DATE(STR_TO_DATE(r.WORK_START_DT, '%Y%m%d%H%i%s')) AS work_date,
                CASE
                    WHEN r.WORKER_SABUN IS NOT NULL AND r.WORKER_SABUN <> ''
                        THEN r.WORKER_SABUN
                    WHEN EXISTS (
                        SELECT 1 FROM its_work_history h2
                        WHERE h2.INST_ID = r.INST_ID
                          AND h2.ACT_ID IN ('104','106','107','108')
                          AND h2.ACT_SIGN = 'R'
                    )
                        THEN NULL
                    ELSE (
                        SELECT h.REG_SABUN
                        FROM its_work_history h
                        WHERE h.INST_ID = r.INST_ID
                          AND h.ACT_ID = '109'
                        ORDER BY CAST(h.SEQ AS UNSIGNED) DESC
                        LIMIT 1
                    )
                END AS sabun
            FROM its_it_work_report r
            WHERE r.INST_ID IN (:instIds)
        ),
        matched AS (
            SELECT
                rv.INST_ID, rv.CHANGE_TITLE, rv.ACT_ID,
                rv.APPROVE3_DT, rv.EXPECTED_FINISHED_DT, rv.REG_USER_DEP_NM, ui.PART_ID
            FROM resolved rv
            LEFT JOIN ybict_user_info ui
                ON ui.EMPNO = rv.sabun
               AND rv.work_date BETWEEN ui.PART_START_DT AND ui.PART_END_DT
        )
        SELECT
            m.INST_ID,
            m.CHANGE_TITLE,
            CASE
                WHEN pi.DEP_TITLE = '영업시스템운영부' THEN '영업'
                WHEN pi.DEP_TITLE = '배전시스템운영부' THEN '배전'
                WHEN pi.DEP_TITLE = '영배시스템기술부' THEN '기술'
                WHEN pi.DEP_TITLE IS NOT NULL THEN pi.DEP_TITLE
                ELSE m.REG_USER_DEP_NM
            END AS DEPARTMENT,
            COALESCE(pi.PART_NM, '미배정') AS PART_NM,
            CASE WHEN m.APPROVE3_DT IS NOT NULL AND m.APPROVE3_DT <> '' THEN '결재 완료' ELSE '미요청' END AS APPROVAL_STATUS,
            CASE
                WHEN m.ACT_ID = '107' THEN '접수'
                WHEN m.ACT_ID = '800' THEN '완료'
                ELSE '처리 중'
            END AS STATUS,
            m.EXPECTED_FINISHED_DT
        FROM matched m
        LEFT JOIN ybict_part_info pi
            ON pi.PART_ID = m.PART_ID
           AND pi.USE_YN = 'Y'
           AND pi.PART_ID NOT LIKE '%\\_0000'
        ORDER BY m.EXPECTED_FINISHED_DT
        """, nativeQuery = true)
    List<Object[]> getWorksMyListByInstIds(@Param("instIds") List<String> instIds);
}
