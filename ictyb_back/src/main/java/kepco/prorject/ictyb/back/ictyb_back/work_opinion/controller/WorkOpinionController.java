package kepco.prorject.ictyb.back.ictyb_back.work_opinion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import kepco.prorject.ictyb.back.ictyb_back.common.BaseResponse;
import kepco.prorject.ictyb.back.ictyb_back.common.enums.ResultCodeEnum;
import kepco.prorject.ictyb.back.ictyb_back.common.enums.StatusEnum;
import kepco.prorject.ictyb.back.ictyb_back.work_opinion.model.WorkOpinionDto;
import kepco.prorject.ictyb.back.ictyb_back.work_opinion.service.WorkOpinionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/work_opinion")
@RequiredArgsConstructor
public class WorkOpinionController {

    private final WorkOpinionService workOpinionService;
    private final ObjectMapper objectMapper;

    /**
     * 업무 협의 목록 조회 (지시번호 기준)
     * GET /api/work_opinion/v1.0/list/{instrNo}
     */
    @GetMapping("/v1.0/list/{instrNo}")
    public ResponseEntity<BaseResponse<?>> getDiscussions(@PathVariable String instrNo) {
        try {
            List<WorkOpinionDto.DiscussionItem> data = workOpinionService.getDiscussions(instrNo);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(data)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("협의 목록을 조회했습니다.")
                    .build());
        } catch (Exception e) {
            log.error("협의 목록 조회 오류 instrNo={}", instrNo, e);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("협의 목록 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 새 협의 스레드 생성
     * POST /api/work_opinion/v1.0 (multipart/form-data)
     * - discussionData: WorkOpinionDto.CreateDiscussionReq (JSON)
     * - files: 첨부파일 (선택, 0개 이상)
     */
    @PostMapping(value = "/v1.0", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<?>> createDiscussion(
            @RequestPart("discussionData") String discussionDataJson,
            MultipartHttpServletRequest multipartRequest) {
        try {
            WorkOpinionDto.CreateDiscussionReq req =
                    objectMapper.readValue(discussionDataJson, WorkOpinionDto.CreateDiscussionReq.class);
            List<MultipartFile> files = multipartRequest.getMultiFileMap().get("files");
            WorkOpinionDto.DiscussionItem data = workOpinionService.createDiscussion(req, files);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(data)
                    .resultCode(ResultCodeEnum.SUCCESS_CREATE)
                    .resultMsg("협의가 등록됐습니다.")
                    .build());
        } catch (Exception e) {
            log.error("협의 생성 오류", e);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("협의 등록 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 댓글 등록
     * POST /api/work_opinion/v1.0/comment (multipart/form-data)
     * - commentData: WorkOpinionDto.CreateCommentReq (JSON)
     * - files: 첨부파일 (선택, 0개 이상)
     */
    @PostMapping(value = "/v1.0/comment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<?>> addComment(
            @RequestPart("commentData") String commentDataJson,
            MultipartHttpServletRequest multipartRequest) {
        try {
            WorkOpinionDto.CreateCommentReq req =
                    objectMapper.readValue(commentDataJson, WorkOpinionDto.CreateCommentReq.class);
            List<MultipartFile> files = multipartRequest.getMultiFileMap().get("files");
            WorkOpinionDto.CommentItem data = workOpinionService.addComment(req, files);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(data)
                    .resultCode(ResultCodeEnum.SUCCESS_CREATE)
                    .resultMsg("댓글이 등록됐습니다.")
                    .build());
        } catch (Exception e) {
            log.error("댓글 등록 오류", e);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("댓글 등록 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 댓글 첨부파일 다운로드
     * GET /api/work_opinion/v1.0/attach/download?cmntId=&seqNo=
     */
    @GetMapping("/v1.0/attach/download")
    public ResponseEntity<?> downloadAttach(
            @RequestParam String cmntId,
            @RequestParam Long seqNo) {
        try {
            WorkOpinionDto.DownloadFile file = workOpinionService.downloadAttach(cmntId, seqNo);
            String encoded = URLEncoder.encode(
                    file.getRealFileName() != null ? file.getRealFileName() : "file",
                    StandardCharsets.UTF_8
            ).replace("+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(file.getResource());
        } catch (Exception e) {
            log.error("댓글 첨부파일 다운로드 오류 cmntId={} seqNo={}", cmntId, seqNo, e);
            return ResponseEntity.notFound().build();
        }
    }
}
