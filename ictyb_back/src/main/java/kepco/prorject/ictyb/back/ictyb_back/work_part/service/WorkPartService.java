package kepco.prorject.ictyb.back.ictyb_back.work_part.service;

import org.springframework.data.domain.Page;
import kepco.prorject.ictyb.back.ictyb_back.work_part.model.AlertDto;
import kepco.prorject.ictyb.back.ictyb_back.work_part.model.WorkPartSummaryDto;

public interface WorkPartService {

    /**
     * 작업지시서 처리 현황 요약 (완료/미완료/접수/부서·파트별 breakdown)
     */
    WorkPartSummaryDto getSummary(String year, String type, String sabun);

    /**
     * 장기 미처리 알림 조회 (페이징 지원)
     */
    Page<AlertDto> getLongUnresolvedAlerts(String year, String type, String sabun, int page, int size);

    /**
     * 마감 임박 알림 조회 (페이징 지원)
     */
    Page<AlertDto> getDueAlerts(String year, String type, String sabun, int page, int size);
}