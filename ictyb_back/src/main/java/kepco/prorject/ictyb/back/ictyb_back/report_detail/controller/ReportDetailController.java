package kepco.prorject.ictyb.back.ictyb_back.report_detail.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kepco.prorject.ictyb.back.ictyb_back.common.BaseResponse;
import kepco.prorject.ictyb.back.ictyb_back.common.enums.ResultCodeEnum;
import kepco.prorject.ictyb.back.ictyb_back.common.enums.StatusEnum;
import kepco.prorject.ictyb.back.ictyb_back.report_detail.model.ReportDetailDto;
import kepco.prorject.ictyb.back.ictyb_back.report_detail.service.ReportDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/report_detail")
@RequiredArgsConstructor
public class ReportDetailController {

    private final ReportDetailService reportDetailService;

    /**
     * 작업지시서 목록 조회
     * GET /api/work_order/v1.0/list
     *
     * @param startDt 조회 시작일 (yyyyMMdd, 미입력 시 전체)
     * @param endDt   조회 종료일 (yyyyMMdd, 미입력 시 전체)
     */
    @GetMapping("/v1.0/list")
    public ResponseEntity<BaseResponse<?>> getWorkOrders(
            @RequestParam(required = false) String startDt,
            @RequestParam(required = false) String endDt,
            @RequestParam(required = false) String deptType
        ) {
        try {
            List<ReportDetailDto> data = reportDetailService.getReportDetail(startDt, endDt);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(data)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("정상적으로 작업지시서 목록을 조회하였습니다.")
                    .build());

        } catch (Exception e) {
            log.error("작업지시서 목록 조회 오류: startDt={}, endDt={}", startDt, endDt, e);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("작업지시서 목록 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }
    
}
