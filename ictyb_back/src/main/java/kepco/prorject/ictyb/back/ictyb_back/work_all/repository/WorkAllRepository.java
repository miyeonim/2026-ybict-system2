package kepco.prorject.ictyb.back.ictyb_back.work_all.repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.ItWorkReportVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.ItWorkReportPk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import static kepco.prorject.ictyb.back.ictyb_back.work_all.repository.WorkAllQuerySql.BASE_CTE;
import static kepco.prorject.ictyb.back.ictyb_back.work_all.repository.WorkAllQuerySql.DEPARTMENT_EXPR;
import static kepco.prorject.ictyb.back.ictyb_back.work_all.repository.WorkAllQuerySql.DUE_DT_FILTER;
import static kepco.prorject.ictyb.back.ictyb_back.work_all.repository.WorkAllQuerySql.JOINED;
import static kepco.prorject.ictyb.back.ictyb_back.work_all.repository.WorkAllQuerySql.STATUS_EXPR;

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
     * 3) 위 사번을 ictyb_user_info와 매칭 (WORK_START_DT가 PART_START_DT~PART_END_DT 범위 내여야
     * 함)
     * 4) 매칭된 PART_ID로 ictyb_part_info를 조인하여 부서(DEP_TITLE→영업/배전/기술)/파트(PART_NM) 산출
     * 사번/파트를 끝내 매칭하지 못한 건은 목록에서 제외된다.
     * 담당자명(managerName)은 WORKER_NAME이 비어 있으면(=사번을 history에서 가져온 경우) 매칭된
     * ictyb_user_info.USER_NM으로 대체한다.
     * 상태(STATUS)는 ictyb_work_negotiation에 수동 등록된 협의 건을 ACT_ID 기반 진행단계보다 우선한다.
     *
     * dept/part/status/마감일 범위로 필터링하고 Pageable로 페이지 단위 조회한다.
     * status='협의'로 필터링할 때는 seenNegotiations(사용자가 이미 확인한 협의 건)를 제외한다.
     */
    @Query(value = BASE_CTE + """
            SELECT
                m.INST_ID,
                m.CHANGE_TITLE,
                m.WORK_TYPE,
            """ + DEPARTMENT_EXPR + """
                 AS DEPARTMENT,
                pi.PART_NM,
                COALESCE(m.WORKER_NAME, m.USER_NM) AS WORKER_NAME,
                CASE WHEN m.APPROVE3_DT IS NOT NULL AND m.APPROVE3_DT <> '' THEN '결재 완료' ELSE '미요청' END AS APPROVAL_STATUS,
            """ + STATUS_EXPR + """
                 AS STATUS,
                m.REG_DT,
                m.EXPECTED_FINISHED_DT
            """ + JOINED + """
            WHERE
            """ + DEPARTMENT_EXPR + """
                 = :dept
              AND (:part = '전체' OR pi.PART_NM = :part)
              AND (:status = '전체' OR
            """ + STATUS_EXPR + """
                   = :status)
              AND (:status <> '협의' OR m.INST_ID NOT IN (:seenNegotiations))
            """ + DUE_DT_FILTER + """
            ORDER BY m.REG_DT DESC
            """,
            countQuery = BASE_CTE + """
            SELECT COUNT(*)
            """ + JOINED + """
            WHERE
            """ + DEPARTMENT_EXPR + """
                 = :dept
              AND (:part = '전체' OR pi.PART_NM = :part)
              AND (:status = '전체' OR
            """ + STATUS_EXPR + """
                   = :status)
              AND (:status <> '협의' OR m.INST_ID NOT IN (:seenNegotiations))
            """ + DUE_DT_FILTER,
            nativeQuery = true)
    Page<Object[]> getWorksAllList(
            @Param("dept") String dept,
            @Param("part") String part,
            @Param("status") String status,
            @Param("startDueDt") String startDueDt,
            @Param("endDueDt") String endDueDt,
            @Param("seenNegotiations") List<String> seenNegotiations,
            Pageable pageable);

    // [부서별 건수] (마감일 범위 내, 부서/파트/상태 필터 없음) /////////////////////////////////
    @Query(value = BASE_CTE + """
            SELECT
            """ + DEPARTMENT_EXPR + """
                 AS DEPARTMENT,
                COUNT(*) AS CNT
            """ + JOINED + """
            WHERE 1 = 1
            """ + DUE_DT_FILTER + """
            GROUP BY
            """ + DEPARTMENT_EXPR,
            nativeQuery = true)
    List<Object[]> getDeptCounts(
            @Param("startDueDt") String startDueDt,
            @Param("endDueDt") String endDueDt);

    // [선택 부서의 파트별 건수] (마감일 범위 내, 파트/상태 필터 없음) ///////////////////////////
    @Query(value = BASE_CTE + """
            SELECT
                pi.PART_NM,
                COUNT(*) AS CNT
            """ + JOINED + """
            WHERE
            """ + DEPARTMENT_EXPR + """
                 = :dept
            """ + DUE_DT_FILTER + """
            GROUP BY pi.PART_NM
            ORDER BY pi.PART_NM
            """,
            nativeQuery = true)
    List<Object[]> getPartCounts(
            @Param("dept") String dept,
            @Param("startDueDt") String startDueDt,
            @Param("endDueDt") String endDueDt);

    // [선택 부서+파트의 상태별 건수 + 전체 건수] (단일 행 반환) //////////////////////////////
    // 협의 건수만 seenNegotiations(사용자가 이미 확인한 협의 건)를 제외하고 집계한다.
    @Query(value = BASE_CTE + """
            SELECT
                SUM(CASE WHEN
            """ + STATUS_EXPR + """
                     = '접수' THEN 1 ELSE 0 END) AS CNT_RECEIVED,
                SUM(CASE WHEN
            """ + STATUS_EXPR + """
                     = '처리 중' THEN 1 ELSE 0 END) AS CNT_PROGRESS,
                SUM(CASE WHEN
            """ + STATUS_EXPR + """
                     = '협의' AND m.INST_ID NOT IN (:seenNegotiations) THEN 1 ELSE 0 END) AS CNT_NEGOTIATION,
                SUM(CASE WHEN
            """ + STATUS_EXPR + """
                     = '완료' THEN 1 ELSE 0 END) AS CNT_DONE,
                COUNT(*) AS CNT_TOTAL
            """ + JOINED + """
            WHERE
            """ + DEPARTMENT_EXPR + """
                 = :dept
              AND (:part = '전체' OR pi.PART_NM = :part)
            """ + DUE_DT_FILTER,
            nativeQuery = true)
    List<Object[]> getStatusCounts(
            @Param("dept") String dept,
            @Param("part") String part,
            @Param("startDueDt") String startDueDt,
            @Param("endDueDt") String endDueDt,
            @Param("seenNegotiations") List<String> seenNegotiations);
}
