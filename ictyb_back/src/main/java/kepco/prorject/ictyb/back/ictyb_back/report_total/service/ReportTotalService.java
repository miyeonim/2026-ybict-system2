package kepco.prorject.ictyb.back.ictyb_back.report_total.service;

import java.util.List;

import org.springframework.data.domain.Page;

import kepco.prorject.ictyb.back.ictyb_back.report_total.model.AlertDto;
import kepco.prorject.ictyb.back.ictyb_back.report_total.model.DeptSectionDto;
import kepco.prorject.ictyb.back.ictyb_back.report_total.model.MonthlyStatDto;
import kepco.prorject.ictyb.back.ictyb_back.report_total.model.PartRankDto;

public interface ReportTotalService {
    List<DeptSectionDto> getDeptStats(String year);

    List<MonthlyStatDto> getMonthlyStats(String year, String depTitle);

     /**
     * 파트별 완료율 랭킹 조회
     *
     * @param year     조회 연도 (예: "2026")
     * @param depTitle 부서 구분: "전체" | "영업" | "배전" | "기술"
     * @return 순위별 파트 완료 통계
     */
    List<PartRankDto> getPartRankStats(String year, String depTitle);


    /**
     * 장기 미처리 알림 조회 (페이징 지원)
     */
    Page<AlertDto> getLongUnresolvedAlerts(int page, int size);


    /**
     * 마감 임박 알림 조회 (페이징 지원)
     */
    Page<AlertDto> getDueAlerts(int page, int size);
}
