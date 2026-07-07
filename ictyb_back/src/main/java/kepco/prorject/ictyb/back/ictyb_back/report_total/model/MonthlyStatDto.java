package kepco.prorject.ictyb.back.ictyb_back.report_total.model;

public record MonthlyStatDto(
    int month, 
    Long currentYY, 
    Long prevYY
) {}