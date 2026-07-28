package kepco.prorject.ictyb.back.ictyb_back.work_all.service;

import kepco.prorject.ictyb.back.ictyb_back.work_all.model.WorkAllDto;
import kepco.prorject.ictyb.back.ictyb_back.work_all.repository.WorkAllRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WorkAllServiceImpl implements WorkAllService {

    private static final List<String> NO_SEEN_SENTINEL = List.of("__NONE__");

    private final WorkAllRepository workAllRepository;

    @Override
    public Page<WorkAllDto.ListItem> getWorksAllList(
            String dept,
            String part,
            String status,
            String startDueDt,
            String endDueDt,
            List<String> seenNegotiations,
            int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> rows = workAllRepository.getWorksAllList(
                dept, part, status, startDueDt, endDueDt, normalizeSeen(seenNegotiations), pageable);

        return rows.map(this::toListItem);
    }

    @Override
    public WorkAllDto.Counts getCounts(
            String dept,
            String part,
            String startDueDt,
            String endDueDt,
            List<String> seenNegotiations) {
        List<String> seen = normalizeSeen(seenNegotiations);

        Map<String, Long> deptCounts = new LinkedHashMap<>();
        deptCounts.put("영업", 0L);
        deptCounts.put("배전", 0L);
        deptCounts.put("기술", 0L);
        for (Object[] row : workAllRepository.getDeptCounts(startDueDt, endDueDt)) {
            String department = (String) row[0];
            if (deptCounts.containsKey(department)) {
                deptCounts.put(department, toLong(row[1]));
            }
        }

        Map<String, Long> partCounts = new LinkedHashMap<>();
        for (Object[] row : workAllRepository.getPartCounts(dept, startDueDt, endDueDt)) {
            partCounts.put((String) row[0], toLong(row[1]));
        }

        List<Object[]> statusRows = workAllRepository.getStatusCounts(dept, part, startDueDt, endDueDt, seen);
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        statusCounts.put("접수", 0L);
        statusCounts.put("처리 중", 0L);
        statusCounts.put("협의", 0L);
        statusCounts.put("완료", 0L);
        long totalCount = 0L;
        if (!statusRows.isEmpty() && statusRows.get(0) != null) {
            Object[] row = statusRows.get(0);
            statusCounts.put("접수", toLong(row[0]));
            statusCounts.put("처리 중", toLong(row[1]));
            statusCounts.put("협의", toLong(row[2]));
            statusCounts.put("완료", toLong(row[3]));
            totalCount = toLong(row[4]);
        }

        return WorkAllDto.Counts.builder()
                .deptCounts(deptCounts)
                .partCounts(partCounts)
                .statusCounts(statusCounts)
                .totalCount(totalCount)
                .build();
    }

    private WorkAllDto.ListItem toListItem(Object[] row) {
        return WorkAllDto.ListItem.builder()
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
                .build();
    }

    // MySQL의 "IN ()" 빈 컬렉션은 문법 오류이므로, 비어있으면 매칭될 수 없는 더미 값으로 대체한다.
    private List<String> normalizeSeen(List<String> seenNegotiations) {
        if (seenNegotiations == null || seenNegotiations.isEmpty()) {
            return NO_SEEN_SENTINEL;
        }
        return seenNegotiations;
    }

    private long toLong(Object o) {
        if (o == null) return 0L;
        if (o instanceof Number) return ((Number) o).longValue();
        return Long.parseLong(o.toString());
    }
}
