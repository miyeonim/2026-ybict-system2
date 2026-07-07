package kepco.prorject.ictyb.back.ictyb_back.work_part.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kepco.prorject.ictyb.back.ictyb_back.common.BaseResponse;
import kepco.prorject.ictyb.back.ictyb_back.common.enums.ResultCodeEnum;
import kepco.prorject.ictyb.back.ictyb_back.common.enums.StatusEnum;
import kepco.prorject.ictyb.back.ictyb_back.work_part.model.AlertDto;
import kepco.prorject.ictyb.back.ictyb_back.work_part.model.WorkPartSummaryDto;
import kepco.prorject.ictyb.back.ictyb_back.work_part.service.WorkPartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/work_part")
@RequiredArgsConstructor
public class WorkPartController {
    
    private final WorkPartService workPartService;

    /**
     * 작업지시서 처리 현황 요약 조회
     * GET /api/work_part/v1.0/summary?year=2026&type=전체&sabun=123456
     */
    @GetMapping("/v1.0/summary")
    public ResponseEntity<BaseResponse<?>> getSummary(
            @RequestParam String year,
            @RequestParam String type,
            @RequestParam(required = false) String sabun) {
        try {
            WorkPartSummaryDto data = workPartService.getSummary(year, type, sabun);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(data)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("작업지시서 처리 현황 조회 완료")
                    .build());

        } catch (Exception e) {
            log.error("작업지시서 처리 현황 조회 오류: year={}, type={}", year, type, e);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("작업지시서 처리 현황 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }



    /**
     * 장기 미처리 알림 리스트 페이징 조회
     * GET /api/work_part/v1.0/alerts/long?year=2026&type=전체&sabun=123456&page=1&size=2
     */
    @GetMapping("/v1.0/alerts/long")
    public ResponseEntity<BaseResponse<?>> getLongAlerts(
            @RequestParam String year,
            @RequestParam String type,
            @RequestParam(required = false) String sabun,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "2") int size) {
        try {
            int targetPage = Math.max(0, page - 1);
            Page<AlertDto> data = workPartService.getLongUnresolvedAlerts(year, type, sabun, targetPage, size);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(data)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("장기 미처리 알림 조회 완료")
                    .build());

        } catch (Exception e) {
            log.error("장기 미처리 알림 조회 오류: year={}, type={}, page={}, size={}", year, type, page, size, e);
            
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("장기 미처리 알림 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 마감 임박 알림 리스트 페이징 조회
     * GET /api/work_part/v1.0/alerts/due?year=2026&type=마이&sabun=212102&page=1&size=2
     */
    @GetMapping("/v1.0/alerts/due")
    public ResponseEntity<BaseResponse<?>> getDueAlerts(
            @RequestParam String year,
            @RequestParam String type,
            @RequestParam(required = false) String sabun,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "2") int size) {
        try {
            int targetPage = Math.max(0, page - 1);
            Page<AlertDto> data = workPartService.getDueAlerts(year, type, sabun, targetPage, size);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(data)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("마감 임박 알림 조회 완료")
                    .build());

        } catch (Exception e) {
            log.error("마감 임박 알림 조회 오류: year={}, type={}, page={}, size={}", year, type, page, size, e);
            
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("마감 임박 알림 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }
}