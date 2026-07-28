package kepco.prorject.ictyb.back.ictyb_back.work_all.service;

import kepco.prorject.ictyb.back.ictyb_back.work_all.model.WorkAllDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface WorkAllService {

    /**
     * 업무지시서(ALL) 목록을 부서/파트/상태/마감일 범위로 필터링해 페이지 단위로 조회한다.
     */
    Page<WorkAllDto.ListItem> getWorksAllList(
            String dept,
            String part,
            String status,
            String startDueDt,
            String endDueDt,
            List<String> seenNegotiations,
            int page,
            int size);

    /**
     * 부서/파트/상태 탭에 표시할 건수 배지를 조회한다.
     */
    WorkAllDto.Counts getCounts(
            String dept,
            String part,
            String startDueDt,
            String endDueDt,
            List<String> seenNegotiations);
}
