package kepco.prorject.ictyb.back.ictyb_back.report_detail.service;

import java.util.List;

import kepco.prorject.ictyb.back.ictyb_back.report_detail.model.ReportDetailDto;

public interface ReportDetailService {
    /**
     * 작업지시서 목록 조회
     *
     * @param startDt 조회 시작일 (yyyyMMdd, null 이면 전체)
     * @param endDt   조회 종료일 (yyyyMMdd, null 이면 전체)
     * @return ReportDetailDto 목록
     */
    List<ReportDetailDto> getReportDetail(String startDt, String endDt);
}
