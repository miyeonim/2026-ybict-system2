package kepco.prorject.ictyb.back.ictyb_back.report_total.model;

public record PartRankDto(
    int num,          // 순위
    String name,      // 파트명
    String sub,       // 부서 구분 (영업/배전/기술)
    long total,       // 총건수
    long done,        // 완료건수
    long pending,     // 미완료건수
    long pct          // 완료율(%)
) {}