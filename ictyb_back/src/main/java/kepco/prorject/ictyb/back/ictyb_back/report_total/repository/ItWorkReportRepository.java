package kepco.prorject.ictyb.back.ictyb_back.report_total.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.ItWorkReportVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.ItWorkReportPk;

@Repository("reportTotalItWorkReportRepository")
public interface ItWorkReportRepository extends JpaRepository<ItWorkReportVo, ItWorkReportPk> {

     //[년도별 파트별 완료율]///////////////////////////////////////////////////////////////////////////
@Query(value = """
    WITH
    -- [STEP 1] 담당자 사번 결정
    -- 우선순위 1: its_it_work_report.REG_SABUN이 ybict_user_info에 존재하고
    --             WORK_START_DT가 part_start_dt ~ part_end_dt 범위 안에 있으면 사용
    -- 우선순위 2: 위 조건 불만족 시 its_work_history에서 최신 6자리 사번 사용
    latest_handler AS (
        SELECT
            r.INST_ID,
            CASE
                WHEN r.WORKER_SABUN IS NOT NULL
                AND r.WORKER_SABUN <> ''
                AND EXISTS (
                    SELECT 1 FROM ybict_user_info ui
                    WHERE ui.EMPNO = r.WORKER_SABUN
                        AND DATE(STR_TO_DATE(r.WORK_START_DT, '%Y%m%d%H%i%s'))
                            BETWEEN ui.PART_START_DT AND ui.PART_END_DT
                )
                THEN r.WORKER_SABUN
                ELSE (
                    SELECT h.REG_SABUN
                    FROM its_work_history h
                    WHERE h.INST_ID = r.INST_ID
                    AND LENGTH(h.REG_SABUN) = 6
                    ORDER BY CAST(h.SEQ AS UNSIGNED) DESC
                    LIMIT 1
                )
            END AS handler_sabun
        FROM its_it_work_report r
        WHERE LEFT(r.WORK_START_DT, 4) = :year
    ),
    -- [STEP 2] 선택한 연도(year)에 해당하는 업무 리포트 필터링
    report_with_handler AS (
        SELECT r.INST_ID, r.ACT_ID, r.WORK_START_DT
        FROM its_it_work_report r
        INNER JOIN latest_handler lh ON r.INST_ID = lh.INST_ID
        WHERE LEFT(r.WORK_START_DT, 4) = :year
    ),
    -- [STEP 3] 업무별 담당자의 부서(PART_ID)를 매칭하고 최신 파트 우선순위 지정
    report_with_part AS (
        SELECT rh.INST_ID, rh.ACT_ID, ui.PART_ID,
               ROW_NUMBER() OVER (PARTITION BY rh.INST_ID ORDER BY ui.PART_START_DT DESC) AS rn
        FROM report_with_handler rh
        INNER JOIN latest_handler lh ON rh.INST_ID = lh.INST_ID
        INNER JOIN ybict_user_info ui ON ui.EMPNO = lh.handler_sabun
        WHERE ui.PART_ID NOT LIKE '%\\_0000'
    ),
    -- [STEP 4] 각 파트별로 완료(ACT_ID='800') 건수와 전체 건수 집계
    base AS (
        SELECT PART_ID,
               SUM(CASE WHEN ACT_ID = '800' THEN 1 ELSE 0 END) AS done_cnt,
               COUNT(*) AS total_cnt
        FROM report_with_part
        WHERE rn = 1
        GROUP BY PART_ID
    )
    -- [STEP 5] 부서정보(pi)와 집계 데이터(base) 결합 및 정렬 적용
    SELECT
        pi.DEP_TITLE, pi.PART_ID, pi.PART_NM,
        IFNULL(b.total_cnt, 0) AS total,
        IFNULL(b.done_cnt, 0) AS done,
        IFNULL(b.total_cnt, 0) - IFNULL(b.done_cnt, 0) AS pending,
        CASE WHEN IFNULL(b.total_cnt, 0) = 0 THEN 100
             ELSE ROUND(IFNULL(b.done_cnt, 0) * 100.0 / b.total_cnt) END AS pct
    FROM ybict_part_info pi
    LEFT JOIN base b ON pi.PART_ID = b.PART_ID
    WHERE pi.USE_YN = 'Y'
      AND pi.PART_ID NOT LIKE '%\\_0000'
    ORDER BY FIELD(pi.DEP_TITLE, '영업시스템운영부', '배전시스템운영부', '영배시스템기술부'), pi.PART_ORDER
    """, nativeQuery = true)
    List<Object[]> getCompletionStats(@Param("year") String year);


    //[월별 작업건수]///////////////////////////////////////////////////////////////////////////
    @Query(value = """
    WITH RECURSIVE months AS (
        SELECT 1 AS m UNION ALL SELECT m + 1 FROM months WHERE m < 12
    ),
    -- 1. 부서 필터링 (불필요한 중복 조회 방지)
    filtered_parts AS (
        SELECT PART_ID FROM ybict_part_info 
        WHERE USE_YN = 'Y'
        AND (:depTitle = '전체' OR DEP_TITLE = :depTitle)
    ),
    -- 2. 실제 데이터 매칭 (history_handler를 분리하는 것이 성능상 유리할 수 있사옵니다)
    raw_data AS (
        SELECT 
            r.WORK_START_DT,
            LEFT(r.WORK_START_DT, 4) AS y,
            CAST(SUBSTRING(r.WORK_START_DT, 5, 2) AS UNSIGNED) AS m
        FROM its_it_work_report r
        INNER JOIN ybict_user_info ui ON ui.EMPNO = CASE 
            WHEN r.WORKER_SABUN IS NOT NULL AND r.WORKER_SABUN <> '' THEN r.WORKER_SABUN
            ELSE (SELECT h.REG_SABUN FROM its_work_history h 
                WHERE h.INST_ID = r.INST_ID AND LENGTH(h.REG_SABUN) = 6 
                ORDER BY CAST(h.SEQ AS UNSIGNED) DESC LIMIT 1)
        END
        AND DATE(STR_TO_DATE(r.WORK_START_DT, '%Y%m%d%H%i%s')) BETWEEN ui.PART_START_DT AND ui.PART_END_DT
        INNER JOIN filtered_parts fp ON ui.PART_ID = fp.PART_ID
        WHERE LEFT(r.WORK_START_DT, 4) IN (:year, :prevYear)
    ),
    -- 3. 연도/월별 집계
    stats AS (
        SELECT y, m, COUNT(*) AS cnt FROM raw_data GROUP BY y, m
    )
    -- 4. 1~12월 기반으로 LEFT JOIN하여 결과 도출
    SELECT 
    m.m AS month,

    CASE 
        WHEN m.m > MONTH(CURDATE()) THEN NULL
        ELSE IFNULL(s_curr.cnt, 0)
    END AS currentYY,

    IFNULL(s_prev.cnt, 0) AS prevYY

    FROM months m
    LEFT JOIN stats s_curr 
        ON m.m = s_curr.m 
        AND s_curr.y = :year

    LEFT JOIN stats s_prev 
        ON m.m = s_prev.m 
        AND s_prev.y = :prevYear

    ORDER BY m.m;
    """, nativeQuery = true)
    List<Map<String, Object>> getMonthlyCompletionStats(@Param("year") String year, @Param("prevYear") String prevYear, @Param("depTitle") String depTitle);



        //[파트별 완료율 랭킹]///////////////////////////////////////////////////////////////////////////
    /**
     * depTitle: '전체' | '영업시스템운영부' | '배전시스템운영부' | '영배시스템기술부'
     *
     * 담당자 결정 규칙
     *   1) WORKER_SABUN이 존재하면 해당 사번을 사용,
     *      단 WORK_START_DT(날짜)가 ybict_user_info.part_start_dt ~ part_end_dt 범위 내여야 한다.
     *   2) WORKER_SABUN이 없으면 its_work_history에서
     *      동일 INST_ID, SEQ 내림차순, REG_SABUN이 6자리인 첫 번째 행을 사용,
     *      마찬가지로 WORK_START_DT 날짜가 part_start_dt ~ part_end_dt 범위 내여야 한다.
     *
     * 결과 정렬: 총건수 내림차순 → 완료율 내림차순
     */
    @Query(value = """
    WITH
    -- [STEP 1] 담당자 사번 결정: WORKER_SABUN 있으면 바로 사용, 없으면 history에서 최신 6자리 사번 선택
    resolved AS (
        SELECT
            r.INST_ID,
            r.ACT_ID,
            DATE(STR_TO_DATE(r.WORK_START_DT, '%Y%m%d%H%i%s')) AS work_date,
            CASE
                WHEN r.WORKER_SABUN IS NOT NULL AND r.WORKER_SABUN <> ''
                    THEN r.WORKER_SABUN
                ELSE (
                    SELECT h.REG_SABUN
                    FROM its_work_history h
                    WHERE h.INST_ID = r.INST_ID
                      AND LENGTH(h.REG_SABUN) = 6
                    ORDER BY CAST(h.SEQ AS UNSIGNED) DESC
                    LIMIT 1
                )
            END AS sabun
        FROM its_it_work_report r
        WHERE LEFT(r.WORK_START_DT, 4) = :year
    ),
    -- [STEP 2] 담당자 사번과 ybict_user_info 매칭: 날짜가 파트 기간 안에 있어야 함
    matched AS (
        SELECT
            rv.INST_ID,
            rv.ACT_ID,
            ui.PART_ID
        FROM resolved rv
        INNER JOIN ybict_user_info ui
            ON ui.EMPNO = rv.sabun
           AND rv.work_date BETWEEN ui.PART_START_DT AND ui.PART_END_DT
    ),
    -- [STEP 3] 부서 필터 적용 및 총괄 파트 제외
    filtered AS (
        SELECT
            m.INST_ID,
            m.ACT_ID,
            pi.PART_NM,
            pi.DEP_TITLE
        FROM matched m
        INNER JOIN ybict_part_info pi
            ON pi.PART_ID = m.PART_ID
           AND pi.USE_YN = 'Y'
           AND pi.PART_ID NOT IN ('YY_0000', 'BJ_0000', 'GS_0000')
        WHERE (:depTitle = '전체'
               OR pi.DEP_TITLE = :depTitle)
    ),
    -- [STEP 4] 파트별 집계
    agg AS (
        SELECT
            PART_NM,
            DEP_TITLE,
            COUNT(*)                                          AS total_cnt,
            SUM(CASE WHEN ACT_ID = '800' THEN 1 ELSE 0 END) AS done_cnt
        FROM filtered
        GROUP BY PART_NM, DEP_TITLE
    )
    -- [STEP 5] 완료율 계산 후 순위 부여 (총건수 → 완료율 내림차순)
    SELECT
        ROW_NUMBER() OVER (
            ORDER BY total_cnt DESC,
                     ROUND(done_cnt * 100.0 / total_cnt) DESC
        )                                              AS num,
        PART_NM                                        AS name,
        CASE
            WHEN DEP_TITLE = '영업시스템운영부' THEN '영업'
            WHEN DEP_TITLE = '배전시스템운영부' THEN '배전'
            WHEN DEP_TITLE = '영배시스템기술부' THEN '기술'
            ELSE DEP_TITLE
        END                                            AS sub,
        total_cnt                                      AS total,
        done_cnt                                       AS done,
        total_cnt - done_cnt                           AS pending,
        ROUND(done_cnt * 100.0 / total_cnt)            AS pct
    FROM agg
    ORDER BY total_cnt DESC,
             ROUND(done_cnt * 100.0 / total_cnt) DESC
    LIMIT 5
    """, nativeQuery = true)
    List<Object[]> getPartRankStats(@Param("year") String year, @Param("depTitle") String depTitle);



    // [장기 미처리 알림 페이징 조회] ///////////////////////////////////////////////////////////
    @Query(value = """
        WITH
        resolved AS (
            SELECT
                r.INST_ID, r.REQ_ID, r.CHANGE_TITLE, r.EXPECTED_FINISHED_DT, r.ACT_ID,
                DATE(STR_TO_DATE(r.WORK_START_DT, '%Y%m%d%H%i%s')) AS work_date,
                CASE
                    WHEN r.WORKER_SABUN IS NOT NULL AND r.WORKER_SABUN <> '' THEN r.WORKER_SABUN
                    ELSE (
                        SELECT h.REG_SABUN FROM its_work_history h 
                        WHERE h.INST_ID = r.INST_ID AND LENGTH(h.REG_SABUN) = 6 
                        ORDER BY CAST(h.SEQ AS UNSIGNED) DESC LIMIT 1
                    )
                END AS sabun
            FROM its_it_work_report r
            WHERE r.ACT_ID <> '800'
              AND r.EXPECTED_FINISHED_DT IS NOT NULL AND r.EXPECTED_FINISHED_DT <> ''
              AND DATE(STR_TO_DATE(r.EXPECTED_FINISHED_DT, '%Y%m%d%H%i%s')) < DATE_SUB(CURDATE(), INTERVAL 3 MONTH)
        ),
        matched AS (
            SELECT
                rv.INST_ID, rv.REQ_ID, rv.CHANGE_TITLE, rv.EXPECTED_FINISHED_DT, ui.PART_ID, rv.sabun,
                DATEDIFF(CURDATE(), DATE(STR_TO_DATE(rv.EXPECTED_FINISHED_DT, '%Y%m%d%H%i%s'))) AS overdue_days
            FROM resolved rv
            INNER JOIN ybict_user_info ui ON ui.EMPNO = rv.sabun 
                AND rv.work_date BETWEEN ui.PART_START_DT AND ui.PART_END_DT
        ),
        filtered AS (
            SELECT
                m.INST_ID, m.REQ_ID, m.CHANGE_TITLE, pi.DEP_TITLE AS dep_title, m.overdue_days
            FROM matched m
            INNER JOIN ybict_part_info pi ON pi.PART_ID = m.PART_ID 
                AND pi.USE_YN = 'Y' AND pi.PART_ID NOT IN ('YY_0000', 'BJ_0000', 'GS_0000')
        )
        SELECT 
            INST_ID, REQ_ID, CHANGE_TITLE, dep_title, 
            CONCAT(FLOOR(overdue_days / 30), '개월 경과') AS overdue_label
        FROM filtered
        ORDER BY overdue_days DESC
        """,
        countQuery = """
        WITH resolved AS (
            SELECT r.INST_ID, DATE(STR_TO_DATE(r.WORK_START_DT, '%Y%m%d%H%i%s')) AS work_date,
                   CASE WHEN r.WORKER_SABUN IS NOT NULL AND r.WORKER_SABUN <> '' THEN r.WORKER_SABUN
                        ELSE (SELECT h.REG_SABUN FROM its_work_history h WHERE h.INST_ID = r.INST_ID AND LENGTH(h.REG_SABUN) = 6 ORDER BY CAST(h.SEQ AS UNSIGNED) DESC LIMIT 1)
                   END AS sabun
            FROM its_it_work_report r
            WHERE r.ACT_ID <> '800' AND r.EXPECTED_FINISHED_DT IS NOT NULL AND r.EXPECTED_FINISHED_DT <> ''
              AND DATE(STR_TO_DATE(r.EXPECTED_FINISHED_DT, '%Y%m%d%H%i%s')) < DATE_SUB(CURDATE(), INTERVAL 3 MONTH)
        ),
        matched AS (
            SELECT rv.INST_ID, ui.PART_ID
            FROM resolved rv
            INNER JOIN ybict_user_info ui ON ui.EMPNO = rv.sabun AND rv.work_date BETWEEN ui.PART_START_DT AND ui.PART_END_DT
        )
        SELECT COUNT(*)
        FROM matched m
        INNER JOIN ybict_part_info pi ON pi.PART_ID = m.PART_ID AND pi.USE_YN = 'Y' AND pi.PART_ID NOT IN ('YY_0000', 'BJ_0000', 'GS_0000')
        """, nativeQuery = true)
    Page<Object[]> getLongUnresolvedAlerts(Pageable pageable);



    // [마감 임박 알림 페이징 조회] ///////////////////////////////////////////////////////////
    @Query(value = """
        WITH
        resolved AS (
            SELECT
                r.INST_ID, r.REQ_ID, r.CHANGE_TITLE, r.EXPECTED_FINISHED_DT, r.ACT_ID,
                DATE(STR_TO_DATE(r.WORK_START_DT, '%Y%m%d%H%i%s')) AS work_date,
                CASE
                    WHEN r.WORKER_SABUN IS NOT NULL AND r.WORKER_SABUN <> '' THEN r.WORKER_SABUN
                    ELSE (
                        SELECT h.REG_SABUN FROM its_work_history h 
                        WHERE h.INST_ID = r.INST_ID AND LENGTH(h.REG_SABUN) = 6 
                        ORDER BY CAST(h.SEQ AS UNSIGNED) DESC LIMIT 1
                    )
                END AS sabun
            FROM its_it_work_report r
            WHERE r.ACT_ID <> '800'
              AND r.EXPECTED_FINISHED_DT IS NOT NULL AND r.EXPECTED_FINISHED_DT <> ''
              AND DATEDIFF(DATE(STR_TO_DATE(r.EXPECTED_FINISHED_DT, '%Y%m%d%H%i%s')), CURDATE()) BETWEEN 0 AND 3
        ),
        matched AS (
            SELECT
                rv.INST_ID, rv.REQ_ID, rv.CHANGE_TITLE, rv.EXPECTED_FINISHED_DT, ui.PART_ID, rv.sabun,
                DATEDIFF(DATE(STR_TO_DATE(rv.EXPECTED_FINISHED_DT, '%Y%m%d%H%i%s')), CURDATE()) AS remain_days
            FROM resolved rv
            INNER JOIN ybict_user_info ui ON ui.EMPNO = rv.sabun 
                AND rv.work_date BETWEEN ui.PART_START_DT AND ui.PART_END_DT
        ),
        filtered AS (
            SELECT
                m.INST_ID, m.REQ_ID, m.CHANGE_TITLE, pi.DEP_TITLE AS dep_title, m.remain_days,
                -- ✨ 해결 포인트: CASE WHEN 로직을 최종 SELECT가 아닌 내부 테이블 연산으로 끌어내림
                CASE 
                    WHEN m.remain_days = 0 THEN '오늘 마감'
                    ELSE CONCAT(m.remain_days, '일 후 마감') 
                END AS due_label
            FROM matched m
            INNER JOIN ybict_part_info pi ON pi.PART_ID = m.PART_ID 
                AND pi.USE_YN = 'Y' AND pi.PART_ID NOT IN ('YY_0000', 'BJ_0000', 'GS_0000')
        )
        -- ✨ 파서가 헷갈리지 않도록 최종 조회를 가장 단순한 형태로 변경
        SELECT 
            INST_ID, REQ_ID, CHANGE_TITLE, dep_title, due_label
        FROM filtered
        ORDER BY remain_days ASC
        """,
        countQuery = """
        WITH resolved AS (
            SELECT r.INST_ID, DATE(STR_TO_DATE(r.WORK_START_DT, '%Y%m%d%H%i%s')) AS work_date,
                   CASE WHEN r.WORKER_SABUN IS NOT NULL AND r.WORKER_SABUN <> '' THEN r.WORKER_SABUN
                        ELSE (SELECT h.REG_SABUN FROM its_work_history h WHERE h.INST_ID = r.INST_ID AND LENGTH(h.REG_SABUN) = 6 ORDER BY CAST(h.SEQ AS UNSIGNED) DESC LIMIT 1)
                   END AS sabun
            FROM its_it_work_report r
            WHERE r.ACT_ID <> '800' AND r.EXPECTED_FINISHED_DT IS NOT NULL AND r.EXPECTED_FINISHED_DT <> ''
              AND DATEDIFF(DATE(STR_TO_DATE(r.EXPECTED_FINISHED_DT, '%Y%m%d%H%i%s')), CURDATE()) BETWEEN 0 AND 3
        ),
        matched AS (
            SELECT rv.INST_ID, ui.PART_ID
            FROM resolved rv
            INNER JOIN ybict_user_info ui ON ui.EMPNO = rv.sabun AND rv.work_date BETWEEN ui.PART_START_DT AND ui.PART_END_DT
        )
        SELECT COUNT(*)
        FROM matched m
        INNER JOIN ybict_part_info pi ON pi.PART_ID = m.PART_ID AND pi.USE_YN = 'Y' AND pi.PART_ID NOT IN ('YY_0000', 'BJ_0000', 'GS_0000')
        """, nativeQuery = true)
    Page<Object[]> getDueAlerts(Pageable pageable);
}