package kepco.prorject.ictyb.back.ictyb_back.qna.controller;
import com.fasterxml.jackson.databind.ObjectMapper;

import kepco.prorject.ictyb.back.ictyb_back.common.BaseResponse;
import kepco.prorject.ictyb.back.ictyb_back.common.enums.ResultCodeEnum;
import kepco.prorject.ictyb.back.ictyb_back.common.enums.StatusEnum;
import kepco.prorject.ictyb.back.ictyb_back.qna.model.QnaDto;
import kepco.prorject.ictyb.back.ictyb_back.qna.service.QnaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/qna")
@RequiredArgsConstructor
public class QnaController {
    private final QnaService   qnaService;
    private final ObjectMapper objectMapper;

    /**
     * Q&A 목록 조회
     * GET /api/qna/list
     */
    @GetMapping("/list")
    public ResponseEntity<BaseResponse<?>> getList() {
        try {
            List<QnaDto.ListItem> list = qnaService.getQnaList();

            // 빌더 패턴으로 응답 객체 생성
            BaseResponse<?> responseResult = BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(list) 
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("정상처리 되었습니다.")
                    .build();

            
            System.out.println("list의 개수 : "+ list.size()+"전송");
            return ResponseEntity.ok(responseResult);

        } catch (Exception e) {
            log.error("Q&A 목록 조회 오류", e);
            
            // 에러 시에도 통일된 응답 구조 반환
            BaseResponse<?> errorResult = BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("Q&A 목록 조회 중 오류가 발생했습니다.")
                    .build();

            return ResponseEntity.ok(errorResult);
        }
    }

    /* 

    /**
     * Q&A 상세 조회
     */
    @GetMapping("/{noticeNo}")
    public ResponseEntity<BaseResponse<?>> getDetail(@PathVariable Long noticeNo) {
        try {
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(qnaService.getQnaDetail(noticeNo))
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("정상처리 되었습니다.")
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.NOT_FOUND) // 혹은 적절한 에러 코드
                    .resultMsg(e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("Q&A 상세 조회 오류 noticeNo={}", noticeNo, e);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("Q&A 상세 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * Q&A 등록
     */
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<?>> register(
            @RequestPart("qnaData") String qnaDataJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        try {
            QnaDto.RegisterRequest req = objectMapper.readValue(qnaDataJson, QnaDto.RegisterRequest.class);
            qnaService.registerQna(req, files);
            
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("Q&A가 등록되었습니다.")
                    .build());
        } catch (Exception e) {
            log.error("Q&A 등록 오류", e);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("Q&A 등록 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 첨부파일 다운로드
     * GET /api/qna/attach/download?noticeNo=&seq=
     */
    @GetMapping("/attach/download")
    public ResponseEntity<Resource> downloadAttach(
            @RequestParam String noticeNo,
            @RequestParam String seq
    ) {
        try {
            Resource resource = qnaService.downloadFile(noticeNo, seq);
            String encoded = URLEncoder.encode(
                    resource.getFilename() != null ? resource.getFilename() : "file",
                    StandardCharsets.UTF_8
            ).replace("+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename*=UTF-8''" + encoded)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            log.error("첨부파일 다운로드 오류 noticeNo={} seq={}", noticeNo, seq, e);
            return ResponseEntity.notFound().build();
        }
    }
}
