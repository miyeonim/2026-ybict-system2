package kepco.prorject.ictyb.back.ictyb_back.report_total.model;

public record DeptPartDto(
    String label,   // 파트 이름 (예: 신증설)
    long total,     // 총 건수
    long done,      // 완료 건수
    long pending,   // 미처리 건수
    long pct        // 완료율(%)
) {}