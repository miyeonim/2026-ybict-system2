package kepco.prorject.ictyb.back.ictyb_back.work_part.model;

public record AlertDto(
    String type,
    String tag,
    String dept,
    String title,
    String date,
    String instId,
    String reqId
) {}