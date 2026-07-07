package kepco.prorject.ictyb.back.ictyb_back.work_all.service;

import kepco.prorject.ictyb.back.ictyb_back.work_all.model.WorkAllDto;

import java.util.List;

public interface WorkAllService {

    /**
     * 업무지시서(ALL) 목록을 전체 조회한다.
     */
    List<WorkAllDto.ListItem> getWorksAllList();
}
