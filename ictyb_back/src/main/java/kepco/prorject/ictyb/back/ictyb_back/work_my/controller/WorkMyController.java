package kepco.prorject.ictyb.back.ictyb_back.work_my.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import kepco.prorject.ictyb.back.ictyb_back.common.BaseResponse;
import kepco.prorject.ictyb.back.ictyb_back.common.enums.ResultCodeEnum;
import kepco.prorject.ictyb.back.ictyb_back.common.enums.StatusEnum;
import kepco.prorject.ictyb.back.ictyb_back.work_my.model.WorkMyDto;
import kepco.prorject.ictyb.back.ictyb_back.work_my.service.WorkMyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    private final ObjectMapper objectMapper;

    /**
     * 업무지시서(MY) 목록 조회
     * GET /api/work_my/v1.0/list
     * 로그인 사용자(userEmpno)를 프론트에서 쿼리 파라미터로 전달받아 본인이 관련자로 판단되는 건만 조회한다.
     */
    @GetMapping("/v1.0/list")
    public ResponseEntity<BaseResponse<?>> getWorksMyList(
            @RequestParam String userEmpno) {

        try {
            List<WorkMyDto.ListItem> data = workMyService.getWorksMyList(userEmpno);

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
            @RequestParam String userEmpno) {

        try {
            WorkMyDto.NextCandidatesResponse data = workMyService.getNextCandidates(instId, userEmpno);

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
            MultipartHttpServletRequest multipartRequest) {

        try {
            WorkMyDto.ApproveRequest request =
                    objectMapper.readValue(approveDataJson, WorkMyDto.ApproveRequest.class);
            List<MultipartFile> files = multipartRequest.getMultiFileMap().get("files");
            workMyService.approve(instId, request.getActorSabun(), request.getActorName(),
                    request.getNextSabun(), request.getNextName(), request.getWorkResult(),
                    request.getIsSecret(), request.getOppbClYn(), request.getUserSecretContent(),
                    request.getAttachExpireDate(), files);

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
     * 반송 대상 이전 결재 단계 후보 조회 (직전 단계부터 처음 작성 단계까지)
     * GET /api/work_my/v1.0/{instId}/return-targets
     * 로그인 사용자가 해당 건의 현재 결재자가 아니면 FORBIDDEN을 반환한다.
     */
    @GetMapping("/v1.0/{instId}/return-targets")
    public ResponseEntity<BaseResponse<?>> getReturnTargets(
            @PathVariable String instId,
            @RequestParam String userEmpno) {

        try {
            WorkMyDto.ReturnTargetsResponse data = workMyService.getReturnTargets(instId, userEmpno);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(data)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("정상적으로 반송 대상 단계를 조회하였습니다.")
                    .build());

        } catch (IllegalStateException e) {
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.FORBIDDEN)
                    .resultMsg(e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("반송 대상 단계 조회 오류: instId={}", instId, e);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("반송 대상 단계 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 결재 반송 (지정한 이전 단계 처리자에게 반려, 사유 필수)
     * POST /api/work_my/v1.0/{instId}/return
     * targetActId 미지정 시 직전 단계로 반송한다.
     * 로그인 사용자가 해당 건의 현재 결재자가 아니면 FORBIDDEN을 반환한다.
     */
    @PostMapping("/v1.0/{instId}/return")
    public ResponseEntity<BaseResponse<?>> returnToPrevious(
            @PathVariable String instId,
            @RequestBody WorkMyDto.ReturnRequest request) {

        try {
            workMyService.returnToPrevious(instId, request.getActorSabun(), request.getActorName(),
                    request.getReason(), request.getTargetActId());

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
    public ResponseEntity<BaseResponse<?>> getCreateOptions() {

        try {
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
    public ResponseEntity<BaseResponse<?>> getInitialApproverCandidates() {

        try {
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
     * 작업 요청서(100→102) 최초 결재자 후보 조회 - 요청자와 같은 소속(SOSOK_CD)인 한전 담당자만 후보로 준다.
     * GET /api/work_my/v1.0/request/initial-approver-candidates
     */
    @GetMapping("/v1.0/request/initial-approver-candidates")
    public ResponseEntity<BaseResponse<?>> getRequestInitialApproverCandidates(
            @RequestParam String depId) {

        try {
            List<WorkMyDto.Candidate> data =
                    workMyService.getRequestInitialApproverCandidates(depId);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(data)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("정상적으로 최초 결재자 후보를 조회하였습니다.")
                    .build());

        } catch (Exception e) {
            log.error("요청서 최초 결재자 후보 조회 오류", e);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("최초 결재자 후보 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 등록 다이얼로그를 열 때 미리 보여줄 등록번호 예약
     * GET /api/work_my/v1.0/create/reserve-no
     * 반환된 번호를 등록 요청(createData.workOrderNo)에 그대로 실어 보내면 그 번호가 실제로 저장된다.
     */
    @GetMapping("/v1.0/create/reserve-no")
    public ResponseEntity<BaseResponse<?>> reserveWorkOrderNo() {

        try {
            String workOrderNo = workMyService.reserveWorkOrderNo();

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(workOrderNo)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("정상적으로 등록번호를 예약하였습니다.")
                    .build());

        } catch (Exception e) {
            log.error("등록번호 예약 오류", e);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("등록번호 예약 중 오류가 발생했습니다.")
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
            MultipartHttpServletRequest multipartRequest) {

        try {
            WorkMyDto.CreateRequest request = objectMapper.readValue(createDataJson, WorkMyDto.CreateRequest.class);
            List<MultipartFile> files = multipartRequest.getMultiFileMap().get("files");
            String instId = workMyService.createWorkOrder(
                    request.getRegUserSabun(), request.getRegUserName(),
                    request.getRegUserDepId(), request.getRegUserDepTitle(),
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
     * 작업 요청서 등록 (요청서 작성(100) 완료 + 요청서 승인(102) 결재 대기 생성)
     * POST /api/work_my/v1.0/request/create (multipart/form-data)
     * - createData: WorkMyDto.CreateWorkRequestReq (JSON)
     * - files: 첨부파일 (선택, 0개 이상)
     * 102(요청서 승인)/103(요청서 접수) 승인·반송 화면은 별도 작업이며, 이 엔드포인트는 등록만 처리한다.
     */
    @PostMapping(value = "/v1.0/request/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<?>> createWorkRequest(
            @RequestPart("createData") String createDataJson,
            MultipartHttpServletRequest multipartRequest) {

        try {
            WorkMyDto.CreateWorkRequestReq request =
                    objectMapper.readValue(createDataJson, WorkMyDto.CreateWorkRequestReq.class);
            List<MultipartFile> files = multipartRequest.getMultiFileMap().get("files");
            String instId = workMyService.createWorkRequest(
                    request.getRegUserSabun(), request.getRegUserName(),
                    request.getRegUserDepId(), request.getRegUserDepTitle(),
                    request, files);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(instId)
                    .resultCode(ResultCodeEnum.SUCCESS_CREATE)
                    .resultMsg("정상적으로 작업 요청서를 등록하였습니다.")
                    .build());

        } catch (IllegalStateException e) {
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.BAD_REQUEST)
                    .resultMsg(e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("작업 요청서 등록 오류", e);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("작업 요청서 등록 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 작업 요청서(MY) 목록 조회
     * GET /api/work_my/v1.0/request/list
     */
    @GetMapping("/v1.0/request/list")
    public ResponseEntity<BaseResponse<?>> getWorkRequestsMyList(
            @RequestParam String userEmpno) {

        try {
            List<WorkMyDto.RequestListItem> data = workMyService.getWorkRequestsMyList(userEmpno);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(data)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("정상적으로 작업 요청서(MY) 목록을 조회하였습니다.")
                    .build());

        } catch (Exception e) {
            log.error("작업 요청서(MY) 목록 조회 오류", e);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("작업 요청서(MY) 목록 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 요청서의 다음 단계(103, 한전 IT부서 직원) 담당자 후보 조회
     * GET /api/work_my/v1.0/request/{instId}/next-candidates
     */
    @GetMapping("/v1.0/request/{instId}/next-candidates")
    public ResponseEntity<BaseResponse<?>> getRequestNextCandidates(
            @PathVariable String instId,
            @RequestParam String userEmpno) {

        try {
            WorkMyDto.NextCandidatesResponse data =
                    workMyService.getRequestNextCandidates(instId, userEmpno);

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
            log.error("요청서 다음 단계 담당자 후보 조회 오류: instId={}", instId, e);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("다음 단계 담당자 후보 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 요청서 결재 승인 (102 승인 시 103 담당자 지정, IT담당자가 이때 확정된다)
     * POST /api/work_my/v1.0/request/{instId}/approve
     */
    @PostMapping("/v1.0/request/{instId}/approve")
    public ResponseEntity<BaseResponse<?>> approveWorkRequest(
            @PathVariable String instId,
            @RequestBody WorkMyDto.RequestApproveRequest request) {

        try {
            workMyService.approveWorkRequest(instId, request.getActorSabun(), request.getActorName(),
                    request.getNextSabun(), request.getNextName());

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
            log.error("요청서 결재 승인 처리 오류: instId={}", instId, e);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("결재 승인 처리 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 요청서 반송 대상 이전 결재 단계 후보 조회
     * GET /api/work_my/v1.0/request/{instId}/return-targets
     */
    @GetMapping("/v1.0/request/{instId}/return-targets")
    public ResponseEntity<BaseResponse<?>> getRequestReturnTargets(
            @PathVariable String instId,
            @RequestParam String userEmpno) {

        try {
            WorkMyDto.ReturnTargetsResponse data =
                    workMyService.getRequestReturnTargets(instId, userEmpno);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(data)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("정상적으로 반송 대상 단계를 조회하였습니다.")
                    .build());

        } catch (IllegalStateException e) {
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.FORBIDDEN)
                    .resultMsg(e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("요청서 반송 대상 단계 조회 오류: instId={}", instId, e);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("반송 대상 단계 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 요청서 결재 반송 (지정한 이전 단계 처리자에게 반려, 사유 필수)
     * POST /api/work_my/v1.0/request/{instId}/return
     */
    @PostMapping("/v1.0/request/{instId}/return")
    public ResponseEntity<BaseResponse<?>> returnWorkRequestToPrevious(
            @PathVariable String instId,
            @RequestBody WorkMyDto.ReturnRequest request) {

        try {
            workMyService.returnWorkRequestToPrevious(instId, request.getActorSabun(), request.getActorName(),
                    request.getReason(), request.getTargetActId());

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
            log.error("요청서 반송 처리 오류: instId={}", instId, e);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("반송 처리 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 작업 요청서 상세 조회 (등록 정보 + 첨부파일 + 결재이력)
     * GET /api/work_my/v1.0/request/{instId}/detail
     */
    @GetMapping("/v1.0/request/{instId}/detail")
    public ResponseEntity<BaseResponse<?>> getRequestDetail(
            @PathVariable String instId,
            @RequestParam String userEmpno) {

        try {
            WorkMyDto.RequestDetail data = workMyService.getRequestDetail(instId, userEmpno);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(data)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("정상적으로 작업 요청서 상세를 조회하였습니다.")
                    .build());

        } catch (IllegalStateException e) {
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.NOT_FOUND)
                    .resultMsg(e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("작업 요청서 상세 조회 오류: instId={}", instId, e);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("작업 요청서 상세 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 작업 요청서 첨부파일 다운로드
     * GET /api/work_my/v1.0/request/attach/download?instId=&seq=
     */
    @GetMapping("/v1.0/request/attach/download")
    public ResponseEntity<?> downloadRequestAttach(
            @RequestParam String instId,
            @RequestParam String seq) {
        try {
            WorkMyDto.DownloadFile file = workMyService.downloadRequestAttach(instId, seq);
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
            log.error("작업 요청서 첨부파일 다운로드 오류 instId={} seq={}", instId, seq, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 업무지시서 수정 (지시서 작성(104)/지시서 승인(106) 단계의 현재 결재자만 가능 - 항상 한전 담당자)
     * POST /api/work_my/v1.0/{instId}/update (multipart/form-data)
     * - updateData: WorkMyDto.UpdateRequest (JSON)
     * - files: 새로 추가할 첨부파일 (선택, 0개 이상)
     * 로그인 사용자가 해당 건의 현재 결재자가 아니거나 현재 단계가 104/106이 아니면 FORBIDDEN을 반환한다.
     */
    @PostMapping(value = "/v1.0/{instId}/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<?>> updateWorkOrder(
            @PathVariable String instId,
            @RequestPart("updateData") String updateDataJson,
            MultipartHttpServletRequest multipartRequest) {

        try {
            WorkMyDto.UpdateRequest request =
                    objectMapper.readValue(updateDataJson, WorkMyDto.UpdateRequest.class);
            List<MultipartFile> files = multipartRequest.getMultiFileMap().get("files");
            workMyService.updateWorkOrder(instId, request.getActorSabun(), request, files);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("정상적으로 업무지시서를 수정하였습니다.")
                    .build());

        } catch (IllegalStateException e) {
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.FORBIDDEN)
                    .resultMsg(e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("업무지시서 수정 오류: instId={}", instId, e);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("업무지시서 수정 중 오류가 발생했습니다.")
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
            @RequestParam String userEmpno) {

        try {
            WorkMyDto.Detail data = workMyService.getDetail(instId, userEmpno);

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
     * 처리기간 연장 (결과 보고(109, KDN 대리) 단계 결재대기 중, 지시서 작성자 본인만)
     * POST /api/work_my/v1.0/{instId}/extend-period
     * 처리예정일은 늘어난 일수만큼 자동으로 함께 밀린다.
     * 로그인 사용자가 작성자가 아니거나, 현재 단계가 109가 아니거나, 새 처리기간이 기존 이하이면 FORBIDDEN을 반환한다.
     */
    @PostMapping("/v1.0/{instId}/extend-period")
    public ResponseEntity<BaseResponse<?>> extendWorkPeriod(
            @PathVariable String instId,
            @RequestBody WorkMyDto.ExtendPeriodRequest request) {

        try {
            WorkMyDto.Detail data = workMyService.extendWorkPeriod(
                    instId, request.getActorSabun(), request.getNewWorkPeriod());

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(data)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("정상적으로 처리기간을 연장하였습니다.")
                    .build());

        } catch (IllegalStateException e) {
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.FORBIDDEN)
                    .resultMsg(e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("처리기간 연장 처리 오류: instId={}", instId, e);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("처리기간 연장 처리 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 업무지시서 삭제 (등록자 본인만, 진행 단계 무관)
     * DELETE /api/work_my/v1.0/{instId}
     * 로그인 사용자가 등록자가 아니면 FORBIDDEN을 반환한다.
     */
    @DeleteMapping("/v1.0/{instId}")
    public ResponseEntity<BaseResponse<?>> deleteWorkOrder(
            @PathVariable String instId,
            @RequestParam String userEmpno) {

        try {
            workMyService.deleteWorkOrder(instId, userEmpno);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("정상적으로 업무지시서를 삭제하였습니다.")
                    .build());

        } catch (IllegalStateException e) {
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.FORBIDDEN)
                    .resultMsg(e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("업무지시서 삭제 오류: instId={}", instId, e);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("업무지시서 삭제 중 오류가 발생했습니다.")
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
