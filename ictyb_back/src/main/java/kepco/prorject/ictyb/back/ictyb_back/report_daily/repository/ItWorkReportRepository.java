package kepco.prorject.ictyb.back.ictyb_back.report_daily.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.ItWorkReportVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.ItWorkReportPk;

import java.util.List;

@Repository("reportDailyItWorkReportRepository")
public interface ItWorkReportRepository extends JpaRepository<ItWorkReportVo, ItWorkReportPk> {

    /**
     * 파트 소속 인원별 진행중/일정지연 작업지시서 건수 집계.
     * 담당자 사번 결정 규칙은 work_all.repository.WorkAllRepository.getWorksAllList()와 동일.
     * 완료(ACT_ID='800') 건은 집계 대상에서 제외하고, 완료예정일(EXPECTED_FINISHED_DT)이
     * 오늘보다 이전이면 일정지연, 그 외(미정 포함)는 진행중으로 분류한다.
     */
    @Query(value = """
        WITH
        resolved AS (
            SELECT
                r.INST_ID, r.ACT_ID, r.WORKER_NAME, r.EXPECTED_FINISHED_DT,
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
            WHERE r.ACT_ID <> '800'
        ),
        matched AS (
            SELECT
                rv.EXPECTED_FINISHED_DT,
                ui.EMPNO AS sabun,
                COALESCE(rv.WORKER_NAME, ui.USER_NM) AS person_name
            FROM resolved rv
            INNER JOIN ybict_user_info ui
                ON ui.EMPNO = rv.sabun
               AND rv.work_date BETWEEN ui.PART_START_DT AND ui.PART_END_DT
            WHERE ui.PART_ID = :partId
        )
        SELECT
            sabun,
            person_name,
            SUM(CASE
                WHEN EXPECTED_FINISHED_DT IS NULL OR EXPECTED_FINISHED_DT = ''
                     OR DATE(STR_TO_DATE(EXPECTED_FINISHED_DT, '%Y%m%d%H%i%s')) >= CURDATE()
                THEN 1 ELSE 0 END) AS in_progress_cnt,
            SUM(CASE
                WHEN EXPECTED_FINISHED_DT IS NOT NULL AND EXPECTED_FINISHED_DT <> ''
                     AND DATE(STR_TO_DATE(EXPECTED_FINISHED_DT, '%Y%m%d%H%i%s')) < CURDATE()
                THEN 1 ELSE 0 END) AS delayed_cnt
        FROM matched
        GROUP BY sabun, person_name
        ORDER BY person_name
        """, nativeQuery = true)
    List<Object[]> getPersonStatsByPart(@Param("partId") String partId);
}
