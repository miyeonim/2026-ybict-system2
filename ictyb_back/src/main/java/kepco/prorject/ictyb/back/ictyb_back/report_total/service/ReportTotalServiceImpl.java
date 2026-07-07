package kepco.prorject.ictyb.back.ictyb_back.report_total.service;

import kepco.prorject.ictyb.back.ictyb_back.report_total.repository.ItWorkReportRepository;
import kepco.prorject.ictyb.back.ictyb_back.report_total.model.AlertDto;
import kepco.prorject.ictyb.back.ictyb_back.report_total.model.DeptPartDto;
import kepco.prorject.ictyb.back.ictyb_back.report_total.model.DeptSectionDto;
import kepco.prorject.ictyb.back.ictyb_back.report_total.model.MonthlyStatDto;
import kepco.prorject.ictyb.back.ictyb_back.report_total.model.PartRankDto;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportTotalServiceImpl implements ReportTotalService {
    
    @Qualifier("reportTotalItWorkReportRepository")
    private final ItWorkReportRepository repository;

    //[년도별 파트별 완료율]///////////////////////////////////////////////////////////////////////////

    @Override
    public List<DeptSectionDto> getDeptStats(String year) {
        List<Object[]> results = repository.getCompletionStats(year);
        Map<String, List<DeptPartDto>> map = new LinkedHashMap<>();

        // DB에서 받은 raw 데이터를 DTO로 변환하여 부서별 그룹화
        for (Object[] row : results) {
            String dept = (String) row[0]; // DEP_TITLE
            DeptPartDto part = new DeptPartDto(
                (String) row[2],                // PART_NM (label)
                ((Number) row[3]).longValue(),  // total
                ((Number) row[4]).longValue(),  // done
                ((Number) row[5]).longValue(),  // pending
                ((Number) row[6]).longValue()   // pct
            );
            map.computeIfAbsent(dept, k -> new ArrayList<>()).add(part);
        }

       List<DeptSectionDto> finalResult = map.entrySet().stream().map(entry -> {
            String title = entry.getKey();
            return new DeptSectionDto(title, getIconByDept(title), entry.getValue());
        }).collect(Collectors.toList());

        // --- 여기서 최종 DTO를 출력합니다 ---
        // --- 올바르게 수정된 출력 코드 ---
        finalResult.forEach(dto -> {
            // data()도 마찬가지로 호출합니다.
            dto.data().forEach(part -> 
                System.out.println("  -> 파트: " + part.label() + ", 완료율: " + part.pct() + "%")
            );
        });

        return finalResult;
    }

    private String getIconByDept(String deptTitle) {
        if (deptTitle.contains("영업")) return "💰";
        if (deptTitle.contains("배전")) return "⚡";
        if (deptTitle.contains("기술")) return "💻";
        return "💼";
    }


    //[월별 작업건수]///////////////////////////////////////////////////////////////////////////

    @Override
    public List<MonthlyStatDto> getMonthlyStats(String year, String depTitle) {
        String prevYear = String.valueOf(Integer.parseInt(year) - 1);

        //이름 세팅
        if(depTitle.equals("영업")){
            depTitle = "영업시스템운영부";
        }else if(depTitle.equals("배전")){
             depTitle = "배전시스템운영부";
        }else if(depTitle.equals("기술")){
             depTitle = "영배시스템기술부";
        }

        List<Map<String, Object>> results =
            repository.getMonthlyCompletionStats(year, prevYear, depTitle);

        return results.stream().map(row -> {

            Number currentYY = (Number) row.get("currentYY");
            Number prevYY = (Number) row.get("prevYY");

            return new MonthlyStatDto(
                ((Number) row.get("month")).intValue(),
                currentYY == null ? null : currentYY.longValue(),
                prevYY == null ? null : prevYY.longValue()
            );

        }).collect(Collectors.toList());
    }

    //[랭크 수]///////////////////////////////////////////////////////////////////////////
    @Override
    public List<PartRankDto> getPartRankStats(String year, String depTitle) {

        // 프론트 단축어 → DB 부서명 변환
        String resolvedDep = switch (depTitle) {
            case "영업" -> "영업시스템운영부";
            case "배전" -> "배전시스템운영부";
            case "기술" -> "영배시스템기술부";
            default    -> "전체";   // "전체" 그대로 전달
        };

        List<Object[]> rows = repository.getPartRankStats(year, resolvedDep);

        return rows.stream().map(row -> new PartRankDto(
                ((Number) row[0]).intValue(),   // num
                (String)  row[1],               // name
                (String)  row[2],               // sub
                ((Number) row[3]).longValue(),  // total
                ((Number) row[4]).longValue(),  // done
                ((Number) row[5]).longValue(),  // padding
                ((Number) row[6]).longValue()   // pct
        )).collect(Collectors.toList());
    }

    //[장기 미처리 건수 ]///////////////////////////////////////////////////////////////////////////
    @Override
    public Page<AlertDto> getLongUnresolvedAlerts(int page, int size) {
        // Spring Data JPA의 페이지는 0부터 시작하옵니다.
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> results = repository.getLongUnresolvedAlerts(pageable);

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

    //[ 마감 임박 건수 ]///////////////////////////////////////////////////////////////////////////
    @Override
    public Page<AlertDto> getDueAlerts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> results = repository.getDueAlerts(pageable);

        return results.map(row -> new AlertDto(
            "due",                   // type
            "마감 임박",               // tag
            (String) row[3],         // dept (dep_title)
            (String) row[2],         // title (CHANGE_TITLE)
            (String) row[4],         // date (due_label: "오늘 마감", "2일 후 마감" 등)
            (String) row[0],         // instId
            (String) row[1]          // reqId
        ));
    }

}