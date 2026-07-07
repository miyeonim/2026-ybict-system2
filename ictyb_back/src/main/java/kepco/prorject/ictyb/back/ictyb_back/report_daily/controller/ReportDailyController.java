package kepco.prorject.ictyb.back.ictyb_back.report_daily.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import kepco.prorject.ictyb.back.ictyb_back.common.BaseResponse;
import kepco.prorject.ictyb.back.ictyb_back.common.enums.ResultCodeEnum;
import kepco.prorject.ictyb.back.ictyb_back.common.enums.StatusEnum;
import kepco.prorject.ictyb.back.ictyb_back.jwt.JwtTokenProvider;
import kepco.prorject.ictyb.back.ictyb_back.jwt.model.JwtUserDto;
import kepco.prorject.ictyb.back.ictyb_back.report_daily.model.ReportDailyDto;
import kepco.prorject.ictyb.back.ictyb_back.report_daily.service.ReportDailyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/report_daily")
@RequiredArgsConstructor
public class ReportDailyController {

    private final ReportDailyService reportDailyService;
    private final JwtTokenProvider jwtTokenProvider;
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
     * 영업 점검일지 상세 조회
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
     * 영업 점검일지 등록
     * POST /api/report_daily/v1.0/register (multipart/form-data)
     * - reportData: ReportDailyDto.RegisterRequest (JSON)
     * - files_{partId}: 파트별 첨부파일 (선택, partId마다 0개 이상)
     */
    @PostMapping(value = "/v1.0/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<?>> register(
            @CookieValue(value = "token", required = false) String token,
            @RequestPart("reportData") String reportDataJson,
            MultipartHttpServletRequest request
    ) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.ok(BaseResponse.actionUnAuthorized());
        }
        try {
            JwtUserDto userInfo = jwtTokenProvider.getUserInfo(token);
            ReportDailyDto.RegisterRequest req =
                    objectMapper.readValue(reportDataJson, ReportDailyDto.RegisterRequest.class);
            MultiValueMap<String, MultipartFile> fileMap = request.getMultiFileMap();

            reportDailyService.registerReport(req, userInfo.getUserEmpno(), userInfo.getEmpNm(), fileMap);

            return ResponseEntity.ok(BaseResponse.actionCreateSuccess());
        } catch (Exception e) {
            log.error("영업 점검일지 등록 오류", e);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("영업 점검일지 등록 중 오류가 발생했습니다.")
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
