package kepco.prorject.ictyb.back.ictyb_back.work_all.service;

import kepco.prorject.ictyb.back.ictyb_back.work_all.model.WorkAllDto;
import kepco.prorject.ictyb.back.ictyb_back.work_all.repository.WorkAllRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkAllServiceImpl implements WorkAllService {

    private final WorkAllRepository workAllRepository;

    @Override
    public List<WorkAllDto.ListItem> getWorksAllList() {
        List<Object[]> rows = workAllRepository.getWorksAllList();

        return rows.stream().map(row -> WorkAllDto.ListItem.builder()
                .workOrderNo((String) row[0])
                .title((String) row[1])
                .workType((String) row[2])
                .department((String) row[3])
                .part((String) row[4])
                .managerName((String) row[5])
                .approvalStatus((String) row[6])
                .status((String) row[7])
                .regDt((String) row[8])
                .dueDt((String) row[9])
                .build()
        ).collect(Collectors.toList());
    }
}
