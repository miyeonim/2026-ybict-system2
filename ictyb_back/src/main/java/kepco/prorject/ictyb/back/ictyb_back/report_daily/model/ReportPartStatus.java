package kepco.prorject.ictyb.back.ictyb_back.report_daily.model;

/**
 * 영업 점검일지 파트별 결재 상태.
 * DRAFT/REJECTED -> (직원 제출) -> SUBMITTED -> (파트장 승인) -> PART_APPROVED -> (부장 최종승인) -> FINAL_APPROVED
 * SUBMITTED/PART_APPROVED 단계에서 반려되면 파트장/부장 구분 없이 REJECTED로 떨어지고,
 * 재제출 시 항상 파트장 승인 단계부터 다시 거친다.
 */
public enum ReportPartStatus {
    DRAFT,
    SUBMITTED,
    PART_APPROVED,
    FINAL_APPROVED,
    REJECTED
}
