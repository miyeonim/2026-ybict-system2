package kepco.prorject.ictyb.back.ictyb_back.report_daily.service;

import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import kepco.prorject.ictyb.back.ictyb_back.report_daily.model.ReportDailyDto;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.util.List;

public interface ReportDailyService {

    /**
     * 영업 점검일지에서 사용 가능한 파트 목록 (ictyb_part_info 기준)
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
     * 점검일지 상세 조회 (reportId 기준, 과거 호환용 - 호출자 권한 정보는 채우지 않는다)
     */
    ReportDailyDto.Detail getReportDetail(Long reportId);

    /**
     * 날짜 기준 점검일지 조회. 아직 아무도 제출하지 않은 날짜라면 파트 목록만 채운
     * 빈 스켈레톤을 반환한다. 각 파트에 호출자(callerSabun) 기준 권한 정보를 채워 내려준다.
     */
    ReportDailyDto.Detail getReportByDate(LocalDate reportDate, String callerSabun);

    /**
     * 파트 내용 제출 (파트 소속 직원이 자기 파트 내용을 작성/재작성 후 제출).
     * 날짜에 해당하는 헤더가 없으면 새로 만든다.
     */
    void submitPart(LocalDate reportDate, String partId, ReportDailyDto.SubmitPartRequest req,
                     String sabun, String name, MultiValueMap<String, MultipartFile> fileMap) throws IOException;

    /**
     * 파트 결재 승인. 상태가 SUBMITTED면 파트장 승인(PART_APPROVED), PART_APPROVED면
     * 부장 최종승인(FINAL_APPROVED)으로 처리한다. 호출자가 해당 권한이 없으면 예외.
     */
    void approvePart(Long reportId, String partId, String sabun, String name);

    /**
     * 파트 결재 반려 (파트장 또는 부장 단계에서). 사유와 함께 REJECTED로 전환한다.
     */
    void rejectPart(Long reportId, String partId, String sabun, String name, String reason);

    /**
     * 첨부파일 다운로드
     */
    ReportDailyDto.DownloadFile downloadAttach(Long reportId, String partId, String seq) throws MalformedURLException;
}
