package kepco.prorject.ictyb.back.ictyb_back.report_total.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kepco.prorject.ictyb.back.ictyb_back.common.BaseResponse;
import kepco.prorject.ictyb.back.ictyb_back.common.enums.ResultCodeEnum;
import kepco.prorject.ictyb.back.ictyb_back.common.enums.StatusEnum;
import kepco.prorject.ictyb.back.ictyb_back.report_total.model.AlertDto;
import kepco.prorject.ictyb.back.ictyb_back.report_total.model.DeptSectionDto;
import kepco.prorject.ictyb.back.ictyb_back.report_total.model.MonthlyStatDto;
import kepco.prorject.ictyb.back.ictyb_back.report_total.model.PartRankDto;
import kepco.prorject.ictyb.back.ictyb_back.report_total.service.ReportTotalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/report_total")
@RequiredArgsConstructor
public class ReportTotalController {
    private final ReportTotalService reportTotalService;

     /**
     * 부서 완료 건수 조회
     */
    @GetMapping("/v1.0/partcompletion")
    public ResponseEntity<BaseResponse<?>> getDeptCompletion(@RequestParam String year) {
        try {
            List<DeptSectionDto> data = reportTotalService.getDeptStats(year);

            BaseResponse<?> responseResult = BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(data)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("정상적으로 부서별 통계를 조회하였습니다.")
                    .build();

            return ResponseEntity.ok(responseResult);

        } catch (Exception e) {
            log.error("부서별 통계 조회 오류: year={}", year, e);
            
            BaseResponse<?> errorResult = BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("통계 조회 중 오류가 발생했습니다.")
                    .build();

            return ResponseEntity.ok(errorResult);
        }
    }

    /**
     * 월별 조회 건수
     */
    @GetMapping("/v1.0/monthlycompletion")
    public ResponseEntity<BaseResponse<?>> getMonthlyCompletion(
            @RequestParam String year,
            @RequestParam(defaultValue = "전체") String depTitle) {
        
        List<MonthlyStatDto> data = reportTotalService.getMonthlyStats(year, depTitle);
        
        return ResponseEntity.ok(BaseResponse.builder()
                .status(StatusEnum.SUCCESS)
                .data(data)
                .resultCode(ResultCodeEnum.SUCCESS)
                .resultMsg("정상 조회 완료")
                .build());
    }


    /**
     * 파트별 완료율 랭킹 조회
     * GET /api/report_total/v1.0/partrank?year=2026&depTitle=전체
     *
     * @param year     조회 연도
     * @param depTitle 부서 구분 (전체 / 영업 / 배전 / 기술), 기본값: 전체
     */
    @GetMapping("/v1.0/partrank")
    public ResponseEntity<BaseResponse<?>> getPartRank(
            @RequestParam String year,
            @RequestParam(defaultValue = "전체") String depTitle) {
        try {
            List<PartRankDto> data = reportTotalService.getPartRankStats(year, depTitle);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(data)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("파트별 완료율 랭킹 조회 완료")
                    .build());

        } catch (Exception e) {
            log.error("파트별 랭킹 조회 오류: year={}, depTitle={}", year, depTitle, e);
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("파트별 랭킹 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }



    /**
     * 장기 미처리 알림 리스트 페이징 조회
     * GET /api/report_total/v1.0/alerts/long?page=1&size=2
     */
    @GetMapping("/v1.0/alerts/long")
    public ResponseEntity<BaseResponse<?>> getLongAlerts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "2") int size) {
        try {
            // 프론트엔드의 1페이지를 JPA의 0페이지로 매핑
            int targetPage = Math.max(0, page - 1);
            Page<AlertDto> data = reportTotalService.getLongUnresolvedAlerts(targetPage, size);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(data)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("장기 미처리 알림 조회 완료")
                    .build());

        } catch (Exception e) {
            log.error("장기 미처리 알림 조회 오류: page={}, size={}", page, size, e);
            
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("장기 미처리 알림 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }


    /**
     * 마감 임박 알림 리스트 페이징 조회
     * GET /api/report_total/v1.0/alerts/due?page=1&size=2
     */
    @GetMapping("/v1.0/alerts/due")
    public ResponseEntity<BaseResponse<?>> getDueAlerts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "2") int size) {
        try {
            // 프론트의 1페이지를 백엔드의 0페이지로 변환
            int targetPage = Math.max(0, page - 1);
            Page<AlertDto> data = reportTotalService.getDueAlerts(targetPage, size);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(data)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("마감 임박 알림 조회 완료")
                    .build());

        } catch (Exception e) {
            log.error("마감 임박 알림 조회 오류: page={}, size={}", page, size, e);
            
            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("마감 임박 알림 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }
}
