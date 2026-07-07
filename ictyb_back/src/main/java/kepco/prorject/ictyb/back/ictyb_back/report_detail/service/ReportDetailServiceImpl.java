package kepco.prorject.ictyb.back.ictyb_back.report_detail.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import kepco.prorject.ictyb.back.ictyb_back.report_detail.model.ReportDetailDto;
import kepco.prorject.ictyb.back.ictyb_back.report_detail.repository.ItWorkReportRepository;

@Service
@RequiredArgsConstructor
public class ReportDetailServiceImpl implements ReportDetailService{


    @Qualifier("reportDetailItWorkReportRepository")
    private final ItWorkReportRepository repository;

    private static final DateTimeFormatter DATE_FMT  = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter OUT_FMT   = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** 접수 상태 ACT_ID */
    private static final Set<String> RECEIPT_ACTS = Set.of("104", "105", "106", "107");
    /** 완료 ACT_ID */
    private static final String COMPLETE_ACT = "800";

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public List<ReportDetailDto> getReportDetail(String startDt, String endDt) {
        // yyyyMMdd → yyyyMMddHHmmss 로 변환 (쿼리 조건용)
        String startParam = startDt != null ? startDt + "000000" : null;
        String endParam   = endDt   != null ? endDt   + "235959" : null;

        List<Object[]> rawList = repository.getWorkOrders(startParam, endParam);

        // 2. toDto를 사용하여 변환 (여기서 필수 변환 발생)
        List<ReportDetailDto> allData = rawList.stream()
            .map(this::toDto)
            .toList();
            
        return allData.stream().toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // private helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Object[] → ReportDetailDto 변환
     * 쿼리 컬럼 순서:
     *   [0] instId, [1] changeTitle, [2] workStartDt, [3] workEndDt,
     *   [4] actId,  [5] approve1Name, [6] partId
     */
    private ReportDetailDto toDto(Object[] row) {
        String workStartDt  = (String) row[2];
        String workEndDt    = (String) row[3];
        String actId        = (String) row[4];
        String partId       = (String) row[6];

        LocalDate start = parseDate(workStartDt);
        LocalDate end   = parseDate(workEndDt);

        long duration = (start != null && end != null)
                ? ChronoUnit.DAYS.between(start, end)
                : 0L;

        return ReportDetailDto.builder()
                .code((String) row[0])
                .name((String) row[1])
                .startDate(start != null ? start.format(OUT_FMT) : null)
                .endDate(end   != null ? end.format(OUT_FMT)   : null)
                .duration(duration)
                .department(resolveDepartment(partId))
                .status(resolveStatus(actId))
                .approval((String) row[5])
                .build();
    }

    /**
     * PART_ID 앞 2자리로 부서 판별
     *   YY → 영업 (영업시스템운영부)
     *   BJ → 배전 (배전시스템운영부)
     *   GS → 기술 (영배시스템기술부)
     */
    private String resolveDepartment(String partId) {
        if (partId == null || partId.isBlank()) return "접수";
        String prefix = partId.length() >= 2 ? partId.substring(0, 2) : "";
        return switch (prefix) {
            case "YY" -> "영업";
            case "BJ" -> "배전";
            case "GS" -> "기술";
            default   -> "접수";
        };
    }

    /**
     * ACT_ID → 진행 상태
     *   800        → 완료
     *   104~107    → 접수
     *   그 외      → 미완료
     */
    private String resolveStatus(String actId) {
        if (COMPLETE_ACT.equals(actId))                     return "완료";
        if (actId != null && RECEIPT_ACTS.contains(actId)) return "접수";
        return "미완료";
    }

    /**
     * yyyyMMddHHmmss → LocalDate (앞 8자리)
     */
    private LocalDate parseDate(String dt) {
        if (dt == null || dt.length() < 8) return null;
        try {
            return LocalDate.parse(dt.substring(0, 8), DATE_FMT);
        } catch (Exception e) {
            return null;
        }
    }
}
