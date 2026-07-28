package kepco.prorject.ictyb.back.ictyb_back.report_daily.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import kepco.prorject.ictyb.back.ictyb_back.common.BaseResponse;
import kepco.prorject.ictyb.back.ictyb_back.common.enums.ResultCodeEnum;
import kepco.prorject.ictyb.back.ictyb_back.common.enums.StatusEnum;
import kepco.prorject.ictyb.back.ictyb_back.report_daily.model.ReportDailyDto;
import kepco.prorject.ictyb.back.ictyb_back.report_daily.service.ReportDailyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/report_daily")
@RequiredArgsConstructor
public class ReportDailyController {

    private final ReportDailyService reportDailyService;
    private final ObjectMapper objectMapper;

    /**
     * 영업 점검일지 파트 목록 조회
     * GET /api/report_daily/v1.0/parts
     */
    @GetMapping("/v1.0/parts")
    public ResponseEntity<BaseResponse<?>> getPartOptions() {
        try {
            List<ReportDailyDto.PartOption> data = reportDailyService.getPartOptions();
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(data)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("정상처리 되었습니다.")
                    .build());
        } catch (Exception e) {
            log.error("영업 점검일지 파트 목록 조회 오류", e);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("파트 목록 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 파트 소속 인원별 진행중/일정지연 작업지시서 건수 (DB 집계)
     * GET /api/report_daily/v1.0/parts/{partId}/person-stats
     */
    @GetMapping("/v1.0/parts/{partId}/person-stats")
    public ResponseEntity<BaseResponse<?>> getPersonStats(@PathVariable String partId) {
        try {
            List<ReportDailyDto.PersonStat> data = reportDailyService.getPersonStats(partId);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(data)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("정상처리 되었습니다.")
                    .build());
        } catch (Exception e) {
            log.error("영업 점검일지 인원별 작업지시 현황 조회 오류 partId={}", partId, e);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("인원별 작업지시 현황 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 영업 점검일지 목록 조회
     * GET /api/report_daily/v1.0/list
     */
    @GetMapping("/v1.0/list")
    public ResponseEntity<BaseResponse<?>> getReportList() {
        try {
            List<ReportDailyDto.ListItem> data = reportDailyService.getReportList();
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(data)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("정상처리 되었습니다.")
                    .build());
        } catch (Exception e) {
            log.error("영업 점검일지 목록 조회 오류", e);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("영업 점검일지 목록 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 영업 점검일지 상세 조회 (reportId 기준, 과거 호환용)
     * GET /api/report_daily/v1.0/{reportId}
     */
    @GetMapping("/v1.0/{reportId}")
    public ResponseEntity<BaseResponse<?>> getReportDetail(@PathVariable Long reportId) {
        try {
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(reportDailyService.getReportDetail(reportId))
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("정상처리 되었습니다.")
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.NOT_FOUND)
                    .resultMsg(e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("영업 점검일지 상세 조회 오류 reportId={}", reportId, e);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("영업 점검일지 상세 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 영업 점검일지 날짜 기준 조회 (신규 작성/결재 화면용). 아직 아무도 제출하지 않은
     * 날짜라면 파트 목록만 채운 빈 스켈레톤을 반환하고, 각 파트에 호출자 기준
     * 권한(myRole/canEdit/canApprove/canReject)을 채워 내려준다.
     * GET /api/report_daily/v1.0/by-date/{date}
     */
    @GetMapping("/v1.0/by-date/{date}")
    public ResponseEntity<BaseResponse<?>> getReportByDate(
            @RequestParam String userEmpno,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        try {
            ReportDailyDto.Detail data = reportDailyService.getReportByDate(date, userEmpno);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(data)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("정상처리 되었습니다.")
                    .build());
        } catch (Exception e) {
            log.error("영업 점검일지 날짜 기준 조회 오류 date={}", date, e);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("영업 점검일지 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 파트 내용 제출 (파트 소속 직원 작성/재작성 후 제출)
     * POST /api/report_daily/v1.0/parts/{partId} (multipart/form-data)
     * - reportData: ReportDailyDto.SubmitPartRequest (JSON, reportDate 포함)
     * - files: 첨부파일 (선택, 0개 이상)
     */
    @PostMapping(value = "/v1.0/parts/{partId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<?>> submitPart(
            @PathVariable String partId,
            @RequestPart("reportData") String reportDataJson,
            MultipartHttpServletRequest request
    ) {
        try {
            ReportDailyDto.SubmitPartRequest req =
                    objectMapper.readValue(reportDataJson, ReportDailyDto.SubmitPartRequest.class);
            MultiValueMap<String, MultipartFile> fileMap = request.getMultiFileMap();

            reportDailyService.submitPart(req.getReportDate(), partId, req,
                    req.getActorSabun(), req.getActorName(), fileMap);

            return ResponseEntity.ok(BaseResponse.actionCreateSuccess());
        } catch (SecurityException e) {
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.FORBIDDEN)
                    .resultMsg(e.getMessage())
                    .build());
        } catch (IllegalStateException e) {
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.CONFLICT)
                    .resultMsg(e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("영업 점검일지 파트 제출 오류 partId={}", partId, e);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("영업 점검일지 제출 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 파트 결재 승인 (파트장/부장)
     * POST /api/report_daily/v1.0/{reportId}/parts/{partId}/approve
     */
    @PostMapping("/v1.0/{reportId}/parts/{partId}/approve")
    public ResponseEntity<BaseResponse<?>> approvePart(
            @PathVariable Long reportId,
            @PathVariable String partId,
            @RequestBody ReportDailyDto.ApproveRequest req
    ) {
        try {
            reportDailyService.approvePart(reportId, partId, req.getActorSabun(), req.getActorName());
            return ResponseEntity.ok(BaseResponse.actionSuccess());
        } catch (SecurityException e) {
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.FORBIDDEN)
                    .resultMsg(e.getMessage())
                    .build());
        } catch (IllegalStateException e) {
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.CONFLICT)
                    .resultMsg(e.getMessage())
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.NOT_FOUND)
                    .resultMsg(e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("영업 점검일지 파트 승인 오류 reportId={} partId={}", reportId, partId, e);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("파트 승인 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 파트 결재 반려 (파트장/부장, 사유 필수)
     * POST /api/report_daily/v1.0/{reportId}/parts/{partId}/reject
     */
    @PostMapping("/v1.0/{reportId}/parts/{partId}/reject")
    public ResponseEntity<BaseResponse<?>> rejectPart(
            @PathVariable Long reportId,
            @PathVariable String partId,
            @RequestBody ReportDailyDto.RejectRequest req
    ) {
        if (req.getReason() == null || req.getReason().isBlank()) {
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.BAD_REQUEST)
                    .resultMsg("반려 사유를 입력해주세요.")
                    .build());
        }
        try {
            reportDailyService.rejectPart(reportId, partId, req.getActorSabun(), req.getActorName(), req.getReason());
            return ResponseEntity.ok(BaseResponse.actionSuccess());
        } catch (SecurityException e) {
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.FORBIDDEN)
                    .resultMsg(e.getMessage())
                    .build());
        } catch (IllegalStateException e) {
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.CONFLICT)
                    .resultMsg(e.getMessage())
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.NOT_FOUND)
                    .resultMsg(e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("영업 점검일지 파트 반려 오류 reportId={} partId={}", reportId, partId, e);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("파트 반려 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 첨부파일 다운로드
     * GET /api/report_daily/v1.0/attach/download?reportId=&partId=&seq=
     */
    @GetMapping("/v1.0/attach/download")
    public ResponseEntity<?> downloadAttach(
            @RequestParam Long reportId,
            @RequestParam String partId,
            @RequestParam String seq
    ) {
        try {
            ReportDailyDto.DownloadFile file = reportDailyService.downloadAttach(reportId, partId, seq);
            String encoded = URLEncoder.encode(
                    file.getRealFileName() != null ? file.getRealFileName() : "file",
                    StandardCharsets.UTF_8
            ).replace("+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(file.getResource());
        } catch (Exception e) {
            log.error("첨부파일 다운로드 오류 reportId={} partId={} seq={}", reportId, partId, seq, e);
            return ResponseEntity.notFound().build();
        }
    }
}
