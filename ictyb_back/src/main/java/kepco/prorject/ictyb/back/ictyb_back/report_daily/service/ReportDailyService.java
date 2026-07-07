package kepco.prorject.ictyb.back.ictyb_back.report_daily.service;

import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import kepco.prorject.ictyb.back.ictyb_back.report_daily.model.ReportDailyDto;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

public interface ReportDailyService {

    /**
     * 영업 점검일지에서 사용 가능한 파트 목록 (ybict_part_info 기준)
     */
    List<ReportDailyDto.PartOption> getPartOptions();

    /**
     * 파트 소속 인원별 진행중/일정지연 작업지시서 건수 (DB 집계)
     */
    List<ReportDailyDto.PersonStat> getPersonStats(String partId);

    /**
     * 점검일지 목록 조회
     */
    List<ReportDailyDto.ListItem> getReportList();

    /**
     * 점검일지 상세 조회
     */
    ReportDailyDto.Detail getReportDetail(Long reportId);

    /**
     * 점검일지 등록 (파트별 내용 + 첨부파일 일괄 저장)
     */
    void registerReport(ReportDailyDto.RegisterRequest req, String authorSabun, String authorName,
                         MultiValueMap<String, MultipartFile> fileMap) throws IOException;

    /**
     * 첨부파일 다운로드
     */
    ReportDailyDto.DownloadFile downloadAttach(Long reportId, String partId, String seq) throws MalformedURLException;
}
