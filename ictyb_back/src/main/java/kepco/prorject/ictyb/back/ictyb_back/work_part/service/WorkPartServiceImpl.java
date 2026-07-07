package kepco.prorject.ictyb.back.ictyb_back.work_part.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import kepco.prorject.ictyb.back.ictyb_back.work_part.repository.WorkPartRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_part.model.AlertDto;
import kepco.prorject.ictyb.back.ictyb_back.work_part.model.WorkPartSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkPartServiceImpl implements WorkPartService {
    
    private final WorkPartRepository repository;

    // [장기 미처리 건수]
    @Override
    public Page<AlertDto> getLongUnresolvedAlerts(String year, String type, String sabun, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> results = repository.getLongUnresolvedAlerts(year, type, sabun, pageable);

        return results.map(row -> new AlertDto(
            "long",                  // type
            "장기 미처리",             // tag
            (String) row[3],         // dept (dep_title)
            (String) row[2],         // title (CHANGE_TITLE)
            (String) row[4],         // date (overdue_label)
            (String) row[0],         // instId
            (String) row[1]          // reqId
        ));
    }

    // [마감 임박 건수]
    @Override
    public Page<AlertDto> getDueAlerts(String year, String type, String sabun, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> results = repository.getDueAlerts(year, type, sabun, pageable);

        return results.map(row -> new AlertDto(
            "due",                   // type
            "마감 임박",               // tag
            (String) row[3],         // dept (dep_title)
            (String) row[2],         // title (CHANGE_TITLE)
            (String) row[4],         // date (due_label)
            (String) row[0],         // instId
            (String) row[1]          // reqId
        ));
    }

    // [작업지시서 처리 현황 요약]
    @Override
    public WorkPartSummaryDto getSummary(String year, String type, String sabun) {
        if ("마이".equals(type)) {
            return buildMySummary(year, sabun);
        }
        if ("전체".equals(type)) {
            return buildAllSummary(year);
        }
        return buildDeptSummary(year, type);
    }

    private WorkPartSummaryDto buildAllSummary(String year) {
        List<Object[]> rows = repository.getPartBreakdown(year);
        long receivedTotal = repository.getReceivedTotal(year);

        Map<String, long[]> deptAgg = new LinkedHashMap<>();
        deptAgg.put("영업", new long[]{0, 0});
        deptAgg.put("배전", new long[]{0, 0});
        deptAgg.put("기술", new long[]{0, 0});

        for (Object[] row : rows) {
            String depTitle = (String) row[0];
            String key = matchDeptKey(depTitle);
            if (key == null) continue;
            long[] acc = deptAgg.get(key);
            acc[0] += toLong(row[2]); // done
            acc[1] += toLong(row[3]); // notDone
        }

        long totalDone = 0, totalNotDone = 0;
        List<WorkPartSummaryDto.BarRow> barRows = new ArrayList<>();
        for (Map.Entry<String, long[]> e : deptAgg.entrySet()) {
            long d = e.getValue()[0];
            long n = e.getValue()[1];
            totalDone += d;
            totalNotDone += n;
            barRows.add(WorkPartSummaryDto.BarRow.builder()
                    .name(e.getKey()).done(d).notDone(n).build());
        }

        return WorkPartSummaryDto.builder()
                .done(totalDone)
                .notDone(totalNotDone)
                .receivedTotal(receivedTotal)
                .barRows(barRows)
                .build();
    }

    private WorkPartSummaryDto buildDeptSummary(String year, String type) {
        List<Object[]> rows = repository.getPartBreakdown(year);

        long totalDone = 0, totalNotDone = 0;
        List<WorkPartSummaryDto.BarRow> barRows = new ArrayList<>();

        for (Object[] row : rows) {
            String depTitle = (String) row[0];
            String partNm = (String) row[1];
            if (depTitle == null || !depTitle.contains(type)) continue;

            long d = toLong(row[2]);
            long n = toLong(row[3]);
            totalDone += d;
            totalNotDone += n;
            barRows.add(WorkPartSummaryDto.BarRow.builder()
                    .name(partNm).done(d).notDone(n).build());
        }

        return WorkPartSummaryDto.builder()
                .done(totalDone)
                .notDone(totalNotDone)
                .receivedTotal(null)
                .barRows(barRows)
                .build();
    }

    private WorkPartSummaryDto buildMySummary(String year, String sabun) {
        List<Object[]> result = repository.getMySummary(year, sabun);
        long done = 0, notDone = 0;
        if (!result.isEmpty() && result.get(0) != null) {
            Object[] row = result.get(0);
            done = toLong(row[0]);
            notDone = toLong(row[1]);
        }

        return WorkPartSummaryDto.builder()
                .done(done)
                .notDone(notDone)
                .receivedTotal(null)
                .barRows(List.of(WorkPartSummaryDto.BarRow.builder()
                        .name("나의 작업").done(done).notDone(notDone).build()))
                .build();
    }

    private String matchDeptKey(String depTitle) {
        if (depTitle == null) return null;
        if (depTitle.contains("영업")) return "영업";
        if (depTitle.contains("배전")) return "배전";
        if (depTitle.contains("기술")) return "기술";
        return null;
    }

    private long toLong(Object o) {
        if (o == null) return 0L;
        if (o instanceof Number) return ((Number) o).longValue();
        return Long.parseLong(o.toString());
    }
}