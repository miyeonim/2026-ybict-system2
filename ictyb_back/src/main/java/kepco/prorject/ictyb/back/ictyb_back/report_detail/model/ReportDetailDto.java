package kepco.prorject.ictyb.back.ictyb_back.report_detail.model;
import lombok.Builder;
import lombok.Getter;

/**
 * 작업지시서 목록 응답 DTO
 * 프론트엔드 WorkOrder 인터페이스 (type 제외) 에 대응
 *
 *  code       → its_it_work_report.INST_ID
 *  name       → its_it_work_report.CHANGE_TITLE
 *  startDate  → its_it_work_report.WORK_START_DT (yyyyMMddHHmmss → yyyy-MM-dd)
 *  endDate    → its_it_work_report.WORK_END_DT
 *  duration   → endDate - startDate (일 수)
 *  department → PART_ID 앞 2자리로 판단 (YY=영업, BJ=배전, GS=기술)
 *  status     → ACT_ID: 800=완료 / 104~107=접수 / 그 외=미완료
 *  assignee   → its_it_work_report.APPROVE1_NAME
 */
@Getter
@Builder
public class ReportDetailDto {
    private String code;        // 작업지시서 ID
    private String name;        // 작업명
    private String startDate;   // "yyyy-MM-dd"
    private String endDate;     // "yyyy-MM-dd"
    private long   duration;    // 일 수
    private String department;  // 영업 / 배전 / 기술 / 미분류
    private String status;      // 완료 / 접수 / 미완료
    private String approval;    // APPROVE1_NAME
}
