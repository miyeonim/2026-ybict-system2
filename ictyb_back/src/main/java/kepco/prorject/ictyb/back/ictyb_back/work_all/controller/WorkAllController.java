package kepco.prorject.ictyb.back.ictyb_back.work_all.controller;

import kepco.prorject.ictyb.back.ictyb_back.common.BaseResponse;
import kepco.prorject.ictyb.back.ictyb_back.common.enums.ResultCodeEnum;
import kepco.prorject.ictyb.back.ictyb_back.common.enums.StatusEnum;
import kepco.prorject.ictyb.back.ictyb_back.work_all.model.WorkAllDto;
import kepco.prorject.ictyb.back.ictyb_back.work_all.service.WorkAllService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/work_all")
@RequiredArgsConstructor
public class WorkAllController {

    private final WorkAllService workAllService;

    /**
     * 업무지시서(ALL) 목록 조회 (부서/파트/상태/마감일 범위 필터 + 페이지네이션)
     * GET /api/work_all/v1.0/list?dept=영업&part=전체&status=전체&startDueDt=&endDueDt=&seenNegotiations=&page=1&size=10
     */
    @GetMapping("/v1.0/list")
    public ResponseEntity<BaseResponse<?>> getWorksAllList(
            @RequestParam String dept,
            @RequestParam(defaultValue = "전체") String part,
            @RequestParam(defaultValue = "전체") String status,
            @RequestParam(required = false) String startDueDt,
            @RequestParam(required = false) String endDueDt,
            @RequestParam(required = false) String seenNegotiations,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            int targetPage = Math.max(0, page - 1);
            Page<WorkAllDto.ListItem> data = workAllService.getWorksAllList(
                    dept, part, status, startDueDt, endDueDt, splitCsv(seenNegotiations), targetPage, size);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(data)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("정상적으로 업무지시서 목록을 조회하였습니다.")
                    .build());

        } catch (Exception e) {
            log.error("업무지시서(ALL) 목록 조회 오류: dept={}, part={}, status={}, page={}, size={}",
                    dept, part, status, page, size, e);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("업무지시서 목록 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 업무지시서(ALL) 부서/파트/상태 탭 건수 배지 조회
     * GET /api/work_all/v1.0/counts?dept=영업&part=전체&startDueDt=&endDueDt=&seenNegotiations=
     */
    @GetMapping("/v1.0/counts")
    public ResponseEntity<BaseResponse<?>> getWorksAllCounts(
            @RequestParam String dept,
            @RequestParam(defaultValue = "전체") String part,
            @RequestParam(required = false) String startDueDt,
            @RequestParam(required = false) String endDueDt,
            @RequestParam(required = false) String seenNegotiations) {
        try {
            WorkAllDto.Counts data = workAllService.getCounts(
                    dept, part, startDueDt, endDueDt, splitCsv(seenNegotiations));

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(data)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("정상적으로 업무지시서 건수를 조회하였습니다.")
                    .build());

        } catch (Exception e) {
            log.error("업무지시서(ALL) 건수 조회 오류: dept={}, part={}", dept, part, e);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("업무지시서 건수 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }

    private List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(",")).filter(s -> !s.isBlank()).toList();
    }
}
