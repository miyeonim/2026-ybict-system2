package kepco.prorject.ictyb.back.ictyb_back.work_all.controller;

import kepco.prorject.ictyb.back.ictyb_back.common.BaseResponse;
import kepco.prorject.ictyb.back.ictyb_back.common.enums.ResultCodeEnum;
import kepco.prorject.ictyb.back.ictyb_back.common.enums.StatusEnum;
import kepco.prorject.ictyb.back.ictyb_back.work_all.model.WorkAllDto;
import kepco.prorject.ictyb.back.ictyb_back.work_all.service.WorkAllService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/work_all")
@RequiredArgsConstructor
public class WorkAllController {

    private final WorkAllService workAllService;

    /**
     * 업무지시서(ALL) 목록 조회
     * GET /api/work_all/v1.0/list
     */
    @GetMapping("/v1.0/list")
    public ResponseEntity<BaseResponse<?>> getWorksAllList() {
        try {
            List<WorkAllDto.ListItem> data = workAllService.getWorksAllList();

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.SUCCESS)
                    .data(data)
                    .resultCode(ResultCodeEnum.SUCCESS)
                    .resultMsg("정상적으로 업무지시서 목록을 조회하였습니다.")
                    .build());

        } catch (Exception e) {
            log.error("업무지시서(ALL) 목록 조회 오류", e);

            return ResponseEntity.ok(BaseResponse.builder()
                    .status(StatusEnum.FAIL)
                    .resultCode(ResultCodeEnum.INTERNAL_SERVER_ERROR)
                    .resultMsg("업무지시서 목록 조회 중 오류가 발생했습니다.")
                    .build());
        }
    }
}
