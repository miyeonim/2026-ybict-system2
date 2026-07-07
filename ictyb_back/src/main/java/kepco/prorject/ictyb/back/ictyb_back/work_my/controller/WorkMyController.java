package kepco.prorject.ictyb.back.ictyb_back.work_my.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import kepco.prorject.ictyb.back.ictyb_back.common.BaseResponse;
import kepco.prorject.ictyb.back.ictyb_back.common.enums.ResultCodeEnum;
import kepco.prorject.ictyb.back.ictyb_back.common.enums.StatusEnum;
import kepco.prorject.ictyb.back.ictyb_back.jwt.JwtTokenProvider;
import kepco.prorject.ictyb.back.ictyb_back.jwt.model.JwtUserDto;
import kepco.prorject.ictyb.back.ictyb_back.work_my.model.WorkMyDto;
import kepco.prorject.ictyb.back.ictyb_back.work_my.service.WorkMyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/work_my")
@RequiredArgsConstructor
public class WorkMyController {

    private final WorkMyService workMyService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    /**
     * 업무지시서(MY) 목록 조회
     * GET /api/work_my/v1.0/list
     * 로그인 쿠키(token)에서 사번을 추출해 본인이 관련자로 판단되는 건만 조회한다.
     */
    @GetMapping("/v1.0/list")
    public ResponseEntity<BaseResponse<?>> getWorksMyList(
            @CookieValue(value = "token", required = false) String token) {

        if (token == null || token.isBlank()) {
            return ResponseEntity.ok(BaseResponse.actionUnAuthorized());
        }

        try {
            JwtUserDto userInfo = jwtTokenProvider.getUserInfo(token);
            List<WorkMyDto.ListItem> data = workMyService.getWorksMyList(userInfo.getUserEmpno());

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(data)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("정상적으로 업무지시서(MY) 목록을 조회하였습니다.")
                    .build());

        } catch (Exception e) {
            log.error("업무지시서(MY) 목록 조회 오류", e);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("업무지시서(MY) 목록 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 다음 결재 단계 담당자 후보 조회
     * GET /api/work_my/v1.0/{instId}/next-candidates
     * 로그인 사용자가 해당 건의 현재 결재자가 아니면 FORBIDDEN을 반환한다.
     */
    @GetMapping("/v1.0/{instId}/next-candidates")
    public ResponseEntity<BaseResponse<?>> getNextCandidates(
            @PathVariable String instId,
            @CookieValue(value = "token", required = false) String token) {

        if (token == null || token.isBlank()) {
            return ResponseEntity.ok(BaseResponse.actionUnAuthorized());
        }

        try {
            JwtUserDto userInfo = jwtTokenProvider.getUserInfo(token);
            WorkMyDto.NextCandidatesResponse data = workMyService.getNextCandidates(instId, userInfo.getUserEmpno());

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(data)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("정상적으로 다음 단계 담당자 후보를 조회하였습니다.")
                    .build());

        } catch (IllegalStateException e) {
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.FORBIDDEN)
                    .resultMsg(e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("다음 단계 담당자 후보 조회 오류: instId={}", instId, e);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("다음 단계 담당자 후보 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 결재 승인 (다음 단계 담당자 지정)
     * POST /api/work_my/v1.0/{instId}/approve (multipart/form-data)
     * - approveData: WorkMyDto.ApproveRequest (JSON)
     * - files: 조치사항 첨부파일 (109단계에서만 사용, 선택, 0개 이상)
     * 로그인 사용자가 해당 건의 현재 결재자가 아니면 FORBIDDEN을 반환한다.
     */
    @PostMapping(value = "/v1.0/{instId}/approve", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<?>> approve(
            @PathVariable String instId,
            @RequestPart("approveData") String approveDataJson,
            MultipartHttpServletRequest multipartRequest,
            @CookieValue(value = "token", required = false) String token) {

        if (token == null || token.isBlank()) {
            return ResponseEntity.ok(BaseResponse.actionUnAuthorized());
        }

        try {
            JwtUserDto userInfo = jwtTokenProvider.getUserInfo(token);
            WorkMyDto.ApproveRequest request =
                    objectMapper.readValue(approveDataJson, WorkMyDto.ApproveRequest.class);
            List<MultipartFile> files = multipartRequest.getMultiFileMap().get("files");
            workMyService.approve(instId, userInfo.getUserEmpno(), userInfo.getEmpNm(),
                    request.getNextSabun(), request.getNextName(), request.getWorkResult(), files);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("정상적으로 결재 승인 처리되었습니다.")
                    .build());

        } catch (IllegalStateException e) {
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.FORBIDDEN)
                    .resultMsg(e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("결재 승인 처리 오류: instId={}", instId, e);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("결재 승인 처리 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 결재 반송 (이전 단계 처리자에게 반려, 사유 필수)
     * POST /api/work_my/v1.0/{instId}/return
     * 로그인 사용자가 해당 건의 현재 결재자가 아니면 FORBIDDEN을 반환한다.
     */
    @PostMapping("/v1.0/{instId}/return")
    public ResponseEntity<BaseResponse<?>> returnToPrevious(
            @PathVariable String instId,
            @RequestBody WorkMyDto.ReturnRequest request,
            @CookieValue(value = "token", required = false) String token) {

        if (token == null || token.isBlank()) {
            return ResponseEntity.ok(BaseResponse.actionUnAuthorized());
        }

        try {
            JwtUserDto userInfo = jwtTokenProvider.getUserInfo(token);
            workMyService.returnToPrevious(instId, userInfo.getUserEmpno(), userInfo.getEmpNm(), request.getReason());

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("정상적으로 반송 처리되었습니다.")
                    .build());

        } catch (IllegalStateException e) {
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.FORBIDDEN)
                    .resultMsg(e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("결재 반송 처리 오류: instId={}", instId, e);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("결재 반송 처리 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 업무지시서 등록 폼의 코드성 드롭다운 옵션 조회 (임시 코드, WorkOrderCodeOptions 참고)
     * GET /api/work_my/v1.0/create/options
     */
    @GetMapping("/v1.0/create/options")
    public ResponseEntity<BaseResponse<?>> getCreateOptions(
            @CookieValue(value = "token", required = false) String token) {

        if (token == null || token.isBlank()) {
            return ResponseEntity.ok(BaseResponse.actionUnAuthorized());
        }

        try {
            jwtTokenProvider.getUserInfo(token);
            WorkMyDto.CreateOptions data = workMyService.getCreateOptions();

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(data)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("정상적으로 등록 폼 옵션을 조회하였습니다.")
                    .build());

        } catch (Exception e) {
            log.error("등록 폼 옵션 조회 오류", e);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("등록 폼 옵션 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 최초 결재자(한전 파트장) 후보 조회
     * GET /api/work_my/v1.0/create/initial-approver-candidates
     */
    @GetMapping("/v1.0/create/initial-approver-candidates")
    public ResponseEntity<BaseResponse<?>> getInitialApproverCandidates(
            @CookieValue(value = "token", required = false) String token) {

        if (token == null || token.isBlank()) {
            return ResponseEntity.ok(BaseResponse.actionUnAuthorized());
        }

        try {
            jwtTokenProvider.getUserInfo(token);
            List<WorkMyDto.Candidate> data = workMyService.getInitialApproverCandidates();

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(data)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("정상적으로 최초 결재자 후보를 조회하였습니다.")
                    .build());

        } catch (Exception e) {
            log.error("최초 결재자 후보 조회 오류", e);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("최초 결재자 후보 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 업무지시서 등록
     * POST /api/work_my/v1.0/create (multipart/form-data)
     * - createData: WorkMyDto.CreateRequest (JSON)
     * - files: 첨부파일 (선택, 0개 이상)
     */
    @PostMapping(value = "/v1.0/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<?>> createWorkOrder(
            @RequestPart("createData") String createDataJson,
            MultipartHttpServletRequest multipartRequest,
            @CookieValue(value = "token", required = false) String token) {

        if (token == null || token.isBlank()) {
            return ResponseEntity.ok(BaseResponse.actionUnAuthorized());
        }

        try {
            JwtUserDto userInfo = jwtTokenProvider.getUserInfo(token);
            WorkMyDto.CreateRequest request = objectMapper.readValue(createDataJson, WorkMyDto.CreateRequest.class);
            List<MultipartFile> files = multipartRequest.getMultiFileMap().get("files");
            String instId = workMyService.createWorkOrder(
                    userInfo.getUserEmpno(), userInfo.getEmpNm(), userInfo.getDepId(), userInfo.getDepTitle(),
                    request, files);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(instId)
                    .resultCode(ResultCodeEnum.SUCCESS_CREATE)
                    .resultMsg("정상적으로 업무지시서를 등록하였습니다.")
                    .build());

        } catch (IllegalStateException e) {
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.BAD_REQUEST)
                    .resultMsg(e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("업무지시서 등록 오류", e);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("업무지시서 등록 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 업무지시서 상세 조회 (등록 정보 + 첨부파일 + 작업결과/조치사항)
     * GET /api/work_my/v1.0/{instId}/detail
     */
    @GetMapping("/v1.0/{instId}/detail")
    public ResponseEntity<BaseResponse<?>> getDetail(
            @PathVariable String instId,
            @CookieValue(value = "token", required = false) String token) {

        if (token == null || token.isBlank()) {
            return ResponseEntity.ok(BaseResponse.actionUnAuthorized());
        }

        try {
            JwtUserDto userInfo = jwtTokenProvider.getUserInfo(token);
            WorkMyDto.Detail data = workMyService.getDetail(instId, userInfo.getUserEmpno());

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(data)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("정상적으로 업무지시서 상세를 조회하였습니다.")
                    .build());

        } catch (IllegalStateException e) {
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.NOT_FOUND)
                    .resultMsg(e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("업무지시서 상세 조회 오류: instId={}", instId, e);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("업무지시서 상세 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 첨부파일 다운로드
     * GET /api/work_my/v1.0/attach/download?instId=&seq=
     */
    @GetMapping("/v1.0/attach/download")
    public ResponseEntity<?> downloadAttach(
            @RequestParam String instId,
            @RequestParam String seq) {
        try {
            WorkMyDto.DownloadFile file = workMyService.downloadAttach(instId, seq);
            String encoded = java.net.URLEncoder.encode(
                    file.getRealFileName() != null ? file.getRealFileName() : "file",
                    java.nio.charset.StandardCharsets.UTF_8
            ).replace("+", "%20");

            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename*=UTF-8''" + encoded)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(file.getResource());
        } catch (Exception e) {
            log.error("첨부파일 다운로드 오류 instId={} seq={}", instId, seq, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 작업결과(조치사항) 첨부파일 다운로드
     * GET /api/work_my/v1.0/work-result-attach/download?instId=&seq=
     */
    @GetMapping("/v1.0/work-result-attach/download")
    public ResponseEntity<?> downloadWorkResultAttach(
            @RequestParam String instId,
            @RequestParam String seq) {
        try {
            WorkMyDto.DownloadFile file = workMyService.downloadWorkResultAttach(instId, seq);
            String encoded = java.net.URLEncoder.encode(
                    file.getRealFileName() != null ? file.getRealFileName() : "file",
                    java.nio.charset.StandardCharsets.UTF_8
            ).replace("+", "%20");

            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename*=UTF-8''" + encoded)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(file.getResource());
        } catch (Exception e) {
            log.error("작업결과 첨부파일 다운로드 오류 instId={} seq={}", instId, seq, e);
            return ResponseEntity.notFound().build();
        }
    }
}
