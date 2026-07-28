package kepco.prorject.ictyb.back.ictyb_back.work_all.repository;

/**
 * WorkAllRepository 쿼리들이 공통으로 쓰는 SQL 조각.
 * 담당자 사번 결정 -> ictyb_user_info/ictyb_part_info 매칭 규칙(WorkAllRepository 클래스 주석 참고)이
 * 목록/카운트 쿼리에 동일하게 적용되어야 하므로 CTE와 파생 표현식을 한 곳에서 관리한다.
 */
final class WorkAllQuerySql {

    static final String BASE_CTE = """
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
                INNER JOIN ictyb_user_info ui
                    ON ui.EMPNO = rv.sabun
                   AND rv.work_date BETWEEN ui.PART_START_DT AND ui.PART_END_DT
            )
            """;

    static final String JOINED = """
            FROM matched m
            INNER JOIN ictyb_part_info pi
                ON pi.PART_ID = m.PART_ID
               AND pi.USE_YN = 'Y'
               AND pi.PART_ID NOT LIKE '%\\_0000'
            LEFT JOIN ictyb_work_negotiation n
                ON n.INST_ID = m.INST_ID
               AND n.NEGOTIATION_YN = 'Y'
            """;

    static final String DEPARTMENT_EXPR = """
            CASE
                WHEN pi.DEP_TITLE = '영업시스템운영부' THEN '영업'
                WHEN pi.DEP_TITLE = '배전시스템운영부' THEN '배전'
                WHEN pi.DEP_TITLE = '영배시스템기술부' THEN '기술'
                ELSE pi.DEP_TITLE
            END
            """;

    static final String STATUS_EXPR = """
            CASE
                WHEN n.INST_ID IS NOT NULL THEN '협의'
                WHEN m.ACT_ID = '107' THEN '접수'
                WHEN m.ACT_ID = '800' THEN '완료'
                ELSE '처리 중'
            END
            """;

    // 마감일 조회 범위 (YYYY-MM-DD 문자열, 비어있으면 무시)
    static final String DUE_DT_FILTER = """
            AND (:startDueDt IS NULL OR :startDueDt = '' OR DATE(STR_TO_DATE(m.EXPECTED_FINISHED_DT, '%Y%m%d%H%i%s')) >= :startDueDt)
            AND (:endDueDt IS NULL OR :endDueDt = '' OR DATE(STR_TO_DATE(m.EXPECTED_FINISHED_DT, '%Y%m%d%H%i%s')) <= :endDueDt)
            """;

    private WorkAllQuerySql() {
    }
}
