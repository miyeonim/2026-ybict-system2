package kepco.prorject.ictyb.back.ictyb_back.report_detail.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.ItWorkReportVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.ItWorkReportPk;
import kepco.prorject.ictyb.back.ictyb_back.report_detail.model.ReportDetailDto;

@Repository("reportDetailItWorkReportRepository")
public interface ItWorkReportRepository extends JpaRepository<ItWorkReportVo, ItWorkReportPk>{
    /**
     * 작업지시서 목록 조회
     *
     * [담당자 사번 결정 규칙 - 기존 프로젝트 패턴과 동일]
     *   1) WORKER_SABUN 존재 + WORK_START_DT가 ybict_user_info의 part_start_dt ~ part_end_dt 범위 내
     *      → WORKER_SABUN 사용
     *   2) 위 조건 불만족 → its_work_history 에서 동일 INST_ID 기준
     *      SEQ 내림차순, LENGTH(REG_SABUN) = 6 인 첫 번째 행의 REG_SABUN 사용
     *
     * [department 결정]
     *   담당자 사번 → ybict_user_info.PART_ID → PART_ID 앞 2자리
     *     YY → 영업 / BJ → 배전 / GS → 기술 / 그 외 → 미분류
     *
     * [status 결정]
     *   ACT_ID = '800'                    → 완료
     *   ACT_ID IN ('104','105','106') → 접수
     *   그 외                              → 미완료
     *
     * @param startDt 조회 시작일 (yyyyMMdd000000 형식, null 허용)
     * @param endDt   조회 종료일 (yyyyMMdd235959 형식, null 허용)
     * @return Object[] 배열 목록
     *   [0] instId                   VARCHAR  INST_ID
     *   [1] changeTitle              VARCHAR  CHANGE_TITLE
     *   [2] workStartDt              VARCHAR  WORK_START_DT (yyyyMMddHHmmss)
     *   [3] EXPECTED_FINISHED_DT     VARCHAR  WORK_END_DT   (yyyyMMddHHmmss)
     *   [4] actId                    VARCHAR  ACT_ID
     *   [5] approve1Name             VARCHAR  REG_USER_NAME
     *   [6] partId                   VARCHAR  담당자의 PART_ID (NULL 가능 → 미분류)
     */
    @Query(value = """
        WITH
        -- [STEP 1] 담당자 사번 결정
        --   WORKER_SABUN 존재 + 날짜 범위 일치 → WORKER_SABUN 사용
        --   그 외 → its_work_history 최신 6자리 사번
        resolved AS (
            SELECT
                r.INST_ID,
                r.REQ_ID,
                r.CHANGE_TITLE,
                r.WORK_START_DT,
                r.EXPECTED_FINISHED_DT,
                r.ACT_ID,
                r.REG_USER_NAME,
                DATE(STR_TO_DATE(r.WORK_START_DT, '%Y%m%d%H%i%s')) AS work_date,
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
                END AS sabun
            FROM its_it_work_report r
            WHERE (:startDt IS NULL OR r.WORK_START_DT >= :startDt)
              AND (:endDt   IS NULL OR r.WORK_START_DT   <= :endDt)
        ),
        -- [STEP 2] 담당자 사번 → ybict_user_info 매칭 → PART_ID 획득
        --   날짜 범위 조건 동일하게 적용
        matched AS (
            SELECT
                rv.INST_ID,
                rv.CHANGE_TITLE,
                rv.WORK_START_DT,
                rv.EXPECTED_FINISHED_DT,
                rv.ACT_ID,
                rv.REG_USER_NAME,
                ui.PART_ID,
                ROW_NUMBER() OVER (
                    PARTITION BY rv.INST_ID
                    ORDER BY ui.PART_START_DT DESC
                ) AS rn
            FROM resolved rv
            LEFT JOIN ybict_user_info ui
                ON ui.EMPNO = rv.sabun
               AND rv.work_date BETWEEN ui.PART_START_DT AND ui.PART_END_DT
        )
        -- [STEP 3] 최신 파트 1건만 취하여 최종 반환
        SELECT
            INST_ID                 AS instId,
            CHANGE_TITLE            AS changeTitle,
            WORK_START_DT           AS workStartDt,
            EXPECTED_FINISHED_DT    AS workEndDt,
            ACT_ID                  AS actId,
            REG_USER_NAME           AS approve1Name,
            CASE
                WHEN PART_ID IS NULL THEN NULL
                ELSE PART_ID
            END AS partId
        FROM matched
        WHERE rn = 1
        ORDER BY WORK_START_DT ASC
        """, nativeQuery = true)
    List<Object[]> getWorkOrders(
            @Param("startDt") String startDt,
            @Param("endDt")   String endDt
    );
}
