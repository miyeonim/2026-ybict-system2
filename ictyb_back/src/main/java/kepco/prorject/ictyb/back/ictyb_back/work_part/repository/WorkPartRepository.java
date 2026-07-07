package kepco.prorject.ictyb.back.ictyb_back.work_part.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.ItWorkReportVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.ItWorkReportPk;

@Repository
public interface WorkPartRepository extends JpaRepository<ItWorkReportVo, ItWorkReportPk> {

    // [장기 미처리 알림 페이징 조회] ///////////////////////////////////////////////////////////
    @Query(value = """
        WITH
        target_reports AS (
            SELECT r.INST_ID, r.REQ_ID, r.CHANGE_TITLE, r.EXPECTED_FINISHED_DT, r.WORK_START_DT, r.WORKER_SABUN
            FROM its_it_work_report r
            WHERE LEFT(r.WORK_START_DT, 4) = :year
              AND r.ACT_ID <> '800'
              AND r.EXPECTED_FINISHED_DT IS NOT NULL AND r.EXPECTED_FINISHED_DT <> ''
              AND DATE(STR_TO_DATE(r.EXPECTED_FINISHED_DT, '%Y%m%d%H%i%s')) < DATE_SUB(CURDATE(), INTERVAL 3 MONTH)
        ),
        handler_calc AS (
            SELECT r.INST_ID, r.REQ_ID, r.CHANGE_TITLE, r.EXPECTED_FINISHED_DT, r.WORK_START_DT,
                   CASE 
                       WHEN r.WORKER_SABUN IS NOT NULL AND r.WORKER_SABUN <> '' THEN r.WORKER_SABUN
                       ELSE (
                           SELECT h.REG_SABUN 
                           FROM its_work_history h 
                           WHERE h.INST_ID = r.INST_ID 
                             AND h.ACT_ID = '108'
                           ORDER BY CAST(h.SEQ AS UNSIGNED) DESC 
                           LIMIT 1
                       )
                   END AS handler_sabun
            FROM target_reports r
        ),
        final_filtered AS (
            SELECT hc.INST_ID, hc.REQ_ID, hc.CHANGE_TITLE, hc.EXPECTED_FINISHED_DT,
                   IFNULL(pi.DEP_TITLE, '부서없음') AS dep_title,
                   DATEDIFF(CURDATE(), DATE(STR_TO_DATE(hc.EXPECTED_FINISHED_DT, '%Y%m%d%H%i%s'))) AS overdue_days
            FROM handler_calc hc
            LEFT JOIN ybict_user_info ui 
                   ON ui.EMPNO = hc.handler_sabun 
                  AND DATE(STR_TO_DATE(hc.WORK_START_DT, '%Y%m%d%H%i%s')) BETWEEN ui.PART_START_DT AND ui.PART_END_DT
            LEFT JOIN ybict_part_info pi 
                   ON pi.PART_ID = ui.PART_ID 
                  AND pi.USE_YN = 'Y'
            WHERE 
                (
                    :type = '마이' AND 
                    :sabun IS NOT NULL AND
                    EXISTS (
                        SELECT 1 FROM its_work_history h2
                        WHERE h2.INST_ID = hc.INST_ID AND h2.REG_SABUN = :sabun
                    ) AND
                    IFNULL((
                        SELECT h.ACT_SIGN
                        FROM its_work_history h
                        WHERE h.INST_ID = hc.INST_ID
                        AND h.REG_SABUN = :sabun
                        ORDER BY CAST(h.SEQ AS UNSIGNED) DESC
                        LIMIT 1
                    ), 'S') = 'S'
                )
                OR
                (
                    :type <> '마이' AND 
                    ui.PART_ID IS NOT NULL AND
                    (
                        :type = '전체' OR 
                        (:type = '영업' AND pi.DEP_TITLE LIKE '%영업%') OR
                        (:type = '배전' AND pi.DEP_TITLE LIKE '%배전%') OR
                        (:type = '기술' AND pi.DEP_TITLE LIKE '%기술%')
                    )
                )
        )
        SELECT 
            INST_ID, REQ_ID, CHANGE_TITLE, dep_title, 
            CONCAT(FLOOR(overdue_days / 30), '개월 경과') AS overdue_label
        FROM final_filtered
        ORDER BY overdue_days DESC
        """,
        countQuery = """
        WITH
        target_reports AS (
            SELECT r.INST_ID, r.WORK_START_DT, r.WORKER_SABUN
            FROM its_it_work_report r
            WHERE LEFT(r.WORK_START_DT, 4) = :year
              AND r.ACT_ID <> '800'
              AND r.EXPECTED_FINISHED_DT IS NOT NULL AND r.EXPECTED_FINISHED_DT <> ''
              AND DATE(STR_TO_DATE(r.EXPECTED_FINISHED_DT, '%Y%m%d%H%i%s')) < DATE_SUB(CURDATE(), INTERVAL 3 MONTH)
        ),
        handler_calc AS (
            SELECT r.INST_ID, r.WORK_START_DT,
                   CASE 
                       WHEN r.WORKER_SABUN IS NOT NULL AND r.WORKER_SABUN <> '' THEN r.WORKER_SABUN
                       ELSE (
                           SELECT h.REG_SABUN FROM its_work_history h 
                           WHERE h.INST_ID = r.INST_ID AND h.ACT_ID = '108'
                           ORDER BY CAST(h.SEQ AS UNSIGNED) DESC LIMIT 1
                       )
                   END AS handler_sabun
            FROM target_reports r
        ),
        final_filtered AS (
            SELECT hc.INST_ID
            FROM handler_calc hc
            LEFT JOIN ybict_user_info ui ON ui.EMPNO = hc.handler_sabun AND DATE(STR_TO_DATE(hc.WORK_START_DT, '%Y%m%d%H%i%s')) BETWEEN ui.PART_START_DT AND ui.PART_END_DT
            LEFT JOIN ybict_part_info pi ON pi.PART_ID = ui.PART_ID AND pi.USE_YN = 'Y'
            WHERE 
            (
                (:type = '마이' AND :sabun IS NOT NULL AND
                 EXISTS (
                    SELECT 1 FROM its_work_history h2
                    WHERE h2.INST_ID = hc.INST_ID AND h2.REG_SABUN = :sabun
                 ) AND
                 IFNULL((
                    SELECT h.ACT_SIGN
                    FROM its_work_history h
                    WHERE h.INST_ID = hc.INST_ID
                    AND h.REG_SABUN = :sabun
                    ORDER BY CAST(h.SEQ AS UNSIGNED) DESC
                    LIMIT 1
                 ), 'S') = 'S')
            )
            OR
            (
                (:type <> '마이' AND ui.PART_ID IS NOT NULL AND 
                 (:type = '전체' OR (:type = '영업' AND pi.DEP_TITLE LIKE '%영업%') OR (:type = '배전' AND pi.DEP_TITLE LIKE '%배전%') OR (:type = '기술' AND pi.DEP_TITLE LIKE '%기술%')))
            )
        )
        SELECT COUNT(*) FROM final_filtered
        """, nativeQuery = true)
    Page<Object[]> getLongUnresolvedAlerts(@Param("year") String year, @Param("type") String type, @Param("sabun") String sabun, Pageable pageable);


    // [마감 임박 알림 페이징 조회] ///////////////////////////////////////////////////////////
    @Query(value = """
        WITH
        target_reports AS (
            SELECT r.INST_ID, r.REQ_ID, r.CHANGE_TITLE, r.EXPECTED_FINISHED_DT, r.WORK_START_DT, r.WORKER_SABUN
            FROM its_it_work_report r
            WHERE LEFT(r.WORK_START_DT, 4) = :year
              AND r.ACT_ID <> '800'
              AND r.EXPECTED_FINISHED_DT IS NOT NULL AND r.EXPECTED_FINISHED_DT <> ''
              AND DATEDIFF(DATE(STR_TO_DATE(r.EXPECTED_FINISHED_DT, '%Y%m%d%H%i%s')), CURDATE()) BETWEEN 0 AND 3
        ),
        handler_calc AS (
            SELECT r.INST_ID, r.REQ_ID, r.CHANGE_TITLE, r.EXPECTED_FINISHED_DT, r.WORK_START_DT,
                   CASE 
                       WHEN r.WORKER_SABUN IS NOT NULL AND r.WORKER_SABUN <> '' THEN r.WORKER_SABUN
                       ELSE (
                           SELECT h.REG_SABUN 
                           FROM its_work_history h 
                           WHERE h.INST_ID = r.INST_ID 
                             AND h.ACT_ID = '108'
                           ORDER BY CAST(h.SEQ AS UNSIGNED) DESC 
                           LIMIT 1
                       )
                   END AS handler_sabun
            FROM target_reports r
        ),
        final_filtered AS (
            SELECT hc.INST_ID, hc.REQ_ID, hc.CHANGE_TITLE, hc.EXPECTED_FINISHED_DT,
                   IFNULL(pi.DEP_TITLE, '부서없음') AS dep_title,
                   DATEDIFF(DATE(STR_TO_DATE(hc.EXPECTED_FINISHED_DT, '%Y%m%d%H%i%s')), CURDATE()) AS remain_days
            FROM handler_calc hc
            LEFT JOIN ybict_user_info ui 
                   ON ui.EMPNO = hc.handler_sabun 
                  AND DATE(STR_TO_DATE(hc.WORK_START_DT, '%Y%m%d%H%i%s')) BETWEEN ui.PART_START_DT AND ui.PART_END_DT
            LEFT JOIN ybict_part_info pi 
                   ON pi.PART_ID = ui.PART_ID 
                  AND pi.USE_YN = 'Y'
            WHERE 
                (
                    :type = '마이' AND 
                    :sabun IS NOT NULL AND
                    EXISTS (
                        SELECT 1 FROM its_work_history h2
                        WHERE h2.INST_ID = hc.INST_ID AND h2.REG_SABUN = :sabun
                    ) AND
                    IFNULL((
                        SELECT h.ACT_SIGN
                        FROM its_work_history h
                        WHERE h.INST_ID = hc.INST_ID
                        AND h.REG_SABUN = :sabun
                        ORDER BY CAST(h.SEQ AS UNSIGNED) DESC
                        LIMIT 1
                    ), 'S') = 'S'
                )
                OR
                (
                    :type <> '마이' AND 
                    ui.PART_ID IS NOT NULL AND
                    (
                        :type = '전체' OR 
                        (:type = '영업' AND pi.DEP_TITLE LIKE '%영업%') OR
                        (:type = '배전' AND pi.DEP_TITLE LIKE '%배전%') OR
                        (:type = '기술' AND pi.DEP_TITLE LIKE '%기술%')
                    )
                )
        )
        SELECT 
            INST_ID, REQ_ID, CHANGE_TITLE, dep_title, 
            CASE 
                WHEN remain_days = 0 THEN '오늘 마감'
                ELSE CONCAT(remain_days, '일 후 마감') 
            END AS due_label
        FROM final_filtered
        ORDER BY remain_days ASC
        """,
        countQuery = """
        WITH
        target_reports AS (
            SELECT r.INST_ID, r.WORK_START_DT, r.WORKER_SABUN
            FROM its_it_work_report r
            WHERE LEFT(r.WORK_START_DT, 4) = :year
              AND r.ACT_ID <> '800'
              AND r.EXPECTED_FINISHED_DT IS NOT NULL AND r.EXPECTED_FINISHED_DT <> ''
              AND DATEDIFF(DATE(STR_TO_DATE(r.EXPECTED_FINISHED_DT, '%Y%m%d%H%i%s')), CURDATE()) BETWEEN 0 AND 3
        ),
        handler_calc AS (
            SELECT r.INST_ID, r.WORK_START_DT,
                   CASE 
                       WHEN r.WORKER_SABUN IS NOT NULL AND r.WORKER_SABUN <> '' THEN r.WORKER_SABUN
                       ELSE (
                           SELECT h.REG_SABUN FROM its_work_history h 
                           WHERE h.INST_ID = r.INST_ID AND h.ACT_ID = '108'
                           ORDER BY CAST(h.SEQ AS UNSIGNED) DESC LIMIT 1
                       )
                   END AS handler_sabun
            FROM target_reports r
        ),
        final_filtered AS (
            SELECT hc.INST_ID
            FROM handler_calc hc
            LEFT JOIN ybict_user_info ui ON ui.EMPNO = hc.handler_sabun AND DATE(STR_TO_DATE(hc.WORK_START_DT, '%Y%m%d%H%i%s')) BETWEEN ui.PART_START_DT AND ui.PART_END_DT
            LEFT JOIN ybict_part_info pi ON pi.PART_ID = ui.PART_ID AND pi.USE_YN = 'Y'
            WHERE 
                (
                    :type = '마이' AND 
                    :sabun IS NOT NULL AND
                    EXISTS (
                        SELECT 1 FROM its_work_history h2
                        WHERE h2.INST_ID = hc.INST_ID AND h2.REG_SABUN = :sabun
                    ) AND
                    IFNULL((
                        SELECT h.ACT_SIGN
                        FROM its_work_history h
                        WHERE h.INST_ID = hc.INST_ID
                        AND h.REG_SABUN = :sabun
                        ORDER BY CAST(h.SEQ AS UNSIGNED) DESC
                        LIMIT 1
                    ), 'S') = 'S'
                OR
                ( :type <> '마이' AND ui.PART_ID IS NOT NULL AND (:type = '전체' OR (:type = '영업' AND pi.DEP_TITLE LIKE '%영업%') OR (:type = '배전' AND pi.DEP_TITLE LIKE '%배전%') OR (:type = '기술' AND pi.DEP_TITLE LIKE '%기술%')) )
        )
        SELECT COUNT(*) FROM final_filtered
        """, nativeQuery = true)
    Page<Object[]> getDueAlerts(@Param("year") String year, @Param("type") String type, @Param("sabun") String sabun, Pageable pageable);



   
    // [부서/파트별 완료·미완료 집계] //////////////////////////////////////////////
    // 0단계(접수: 104/105/106/107)는 제외하고, 담당자 사번 -> ybict_user_info -> ybict_part_info 로
    // 부서/파트를 매칭한 뒤 ACT_ID=800(완료) 여부로 집계한다.
    @Query(value = """
        WITH
        target_reports AS (
            SELECT r.INST_ID, r.ACT_ID, r.WORK_START_DT, r.WORKER_SABUN
            FROM its_it_work_report r
            WHERE LEFT(r.WORK_START_DT, 4) = :year
              AND r.ACT_ID NOT IN ('104','105','106','107')
        ),
        handler_calc AS (
            SELECT r.INST_ID, r.ACT_ID, r.WORK_START_DT,
                   CASE 
                       WHEN r.WORKER_SABUN IS NOT NULL AND r.WORKER_SABUN <> '' THEN r.WORKER_SABUN
                       ELSE (
                           SELECT h.REG_SABUN 
                           FROM its_work_history h 
                           WHERE h.INST_ID = r.INST_ID 
                             AND h.ACT_ID = '108'
                           ORDER BY CAST(h.SEQ AS UNSIGNED) DESC 
                           LIMIT 1
                       )
                   END AS handler_sabun
            FROM target_reports r
        ),
        matched AS (
            SELECT hc.INST_ID, hc.ACT_ID, pi.DEP_TITLE AS dep_title, pi.PART_NM AS part_nm
            FROM handler_calc hc
            JOIN ybict_user_info ui 
                 ON ui.EMPNO = hc.handler_sabun 
                AND DATE(STR_TO_DATE(hc.WORK_START_DT, '%Y%m%d%H%i%s')) BETWEEN ui.PART_START_DT AND ui.PART_END_DT
            JOIN ybict_part_info pi 
                 ON pi.PART_ID = ui.PART_ID 
                AND pi.USE_YN = 'Y'
            WHERE pi.DEP_TITLE LIKE '%영업%' OR pi.DEP_TITLE LIKE '%배전%' OR pi.DEP_TITLE LIKE '%기술%'
        )
        SELECT 
            dep_title,
            part_nm,
            SUM(CASE WHEN ACT_ID = '800' THEN 1 ELSE 0 END) AS done_cnt,
            SUM(CASE WHEN ACT_ID <> '800' THEN 1 ELSE 0 END) AS notdone_cnt
        FROM matched
        GROUP BY dep_title, part_nm
        ORDER BY dep_title, part_nm
        """, nativeQuery = true)
    List<Object[]> getPartBreakdown(@Param("year") String year);


    // [접수 건수 (act_id 104~107)] ////////////////////////////////////////////
    @Query(value = """
        SELECT COUNT(*)
        FROM its_it_work_report r
        WHERE LEFT(r.WORK_START_DT, 4) = :year
          AND r.ACT_ID IN ('104','105','106','107')
        """, nativeQuery = true)
    long getReceivedTotal(@Param("year") String year);


    // [마이 완료/미완료 집계] //////////////////////////////////////////////////////
    // its_work_history에 내 사번(REG_SABUN)으로 등록된 이력이 존재하고,
    // 그 중 SEQ가 가장 큰 행의 ACT_SIGN이 'S'이거나 NULL이면 "내 작업"으로 본다.
    @Query(value = """
        WITH
        target_reports AS (
            SELECT r.INST_ID, r.ACT_ID
            FROM its_it_work_report r
            WHERE LEFT(r.WORK_START_DT, 4) = :year
        ),
        my_reports AS (
            SELECT tr.INST_ID, tr.ACT_ID
            FROM target_reports tr
            WHERE EXISTS (
                SELECT 1 FROM its_work_history h2 
                WHERE h2.INST_ID = tr.INST_ID AND h2.REG_SABUN = :sabun
            )
            AND IFNULL((
                SELECT h.ACT_SIGN
                FROM its_work_history h
                WHERE h.INST_ID = tr.INST_ID
                  AND h.REG_SABUN = :sabun
                ORDER BY CAST(h.SEQ AS UNSIGNED) DESC
                LIMIT 1
            ), 'S') = 'S'
        )
        SELECT 
            SUM(CASE WHEN ACT_ID = '800' THEN 1 ELSE 0 END) AS done_cnt,
            SUM(CASE WHEN ACT_ID <> '800' THEN 1 ELSE 0 END) AS notdone_cnt
        FROM my_reports
        """, nativeQuery = true)
    List<Object[]> getMySummary(@Param("year") String year, @Param("sabun") String sabun);
}