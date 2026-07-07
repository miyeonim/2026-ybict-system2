package kepco.prorject.ictyb.back.ictyb_back.work_all.repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.ItWorkReportVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.ItWorkReportPk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("workAllItWorkReportRepository")
public interface WorkAllRepository extends JpaRepository<ItWorkReportVo, ItWorkReportPk> {

    // [업무지시서(ALL) 목록
    // 조회]///////////////////////////////////////////////////////////////////////////
    /**
     * 담당자 사번 결정 및 부서/파트 매칭 규칙.
     * 1) WORKER_SABUN이 있으면 해당 사번 사용
     * 2) 없으면 109(작업결과 보고 단계) 기준으로 판단한다.
     * 단, 109 이전 단계인 104~108 중 ACT_SIGN='R'(반송)이 하나라도 있으면 그 진행 흐름을
     * 신뢰할 수 없으므로 사번 미확정 처리한다 (해당 파트 소속으로 인정하지 않음).
     * 반송 이력이 없으면 its_work_history에서 ACT_ID='109'인 행을 SEQ 내림차순으로 조회해
     * 첫 번째 행의 REG_SABUN(작업결과 보고 등록자 사번) 사용
     * 3) 위 사번을 ybict_user_info와 매칭 (WORK_START_DT가 PART_START_DT~PART_END_DT 범위 내여야
     * 함)
     * 4) 매칭된 PART_ID로 ybict_part_info를 조인하여 부서(DEP_TITLE→영업/배전/기술)/파트(PART_NM) 산출
     * 사번/파트를 끝내 매칭하지 못한 건은 목록에서 제외된다.
     * 담당자명(managerName)은 WORKER_NAME이 비어 있으면(=사번을 history에서 가져온 경우) 매칭된
     * ybict_user_info.USER_NM으로 대체한다.
     * 상태(STATUS)는 ictyb_work_negotiation에 수동 등록된 협의 건을 ACT_ID 기반 진행단계보다 우선한다.
     */
    @Query(value = """
            WITH
            resolved AS (
                SELECT
                    r.INST_ID, r.CHANGE_TITLE, r.WORK_TYPE, r.ACT_ID, r.WORKER_NAME,
                    r.APPROVE3_DT, r.REG_DT, r.EXPECTED_FINISHED_DT,
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
            ),
            matched AS (
                SELECT
                    rv.INST_ID, rv.CHANGE_TITLE, rv.WORK_TYPE, rv.ACT_ID, rv.WORKER_NAME,
                    rv.APPROVE3_DT, rv.REG_DT, rv.EXPECTED_FINISHED_DT, ui.PART_ID, ui.USER_NM
                FROM resolved rv
                INNER JOIN ybict_user_info ui
                    ON ui.EMPNO = rv.sabun
                   AND rv.work_date BETWEEN ui.PART_START_DT AND ui.PART_END_DT
            )
            SELECT
                m.INST_ID,
                m.CHANGE_TITLE,
                m.WORK_TYPE,
                CASE
                    WHEN pi.DEP_TITLE = '영업시스템운영부' THEN '영업'
                    WHEN pi.DEP_TITLE = '배전시스템운영부' THEN '배전'
                    WHEN pi.DEP_TITLE = '영배시스템기술부' THEN '기술'
                    ELSE pi.DEP_TITLE
                END AS DEPARTMENT,
                pi.PART_NM,
                COALESCE(m.WORKER_NAME, m.USER_NM) AS WORKER_NAME,
                CASE WHEN m.APPROVE3_DT IS NOT NULL AND m.APPROVE3_DT <> '' THEN '결재 완료' ELSE '미요청' END AS APPROVAL_STATUS,
                CASE
                    WHEN n.INST_ID IS NOT NULL THEN '협의'
                    WHEN m.ACT_ID = '107' THEN '접수'
                    WHEN m.ACT_ID = '800' THEN '완료'
                    ELSE '처리 중'
                END AS STATUS,
                m.REG_DT,
                m.EXPECTED_FINISHED_DT
            FROM matched m
            INNER JOIN ybict_part_info pi
                ON pi.PART_ID = m.PART_ID
               AND pi.USE_YN = 'Y'
               AND pi.PART_ID NOT LIKE '%\\_0000'
            LEFT JOIN ictyb_work_negotiation n
                ON n.INST_ID = m.INST_ID
               AND n.NEGOTIATION_YN = 'Y'
            ORDER BY m.REG_DT DESC
            """, nativeQuery = true)
    List<Object[]> getWorksAllList();
}
