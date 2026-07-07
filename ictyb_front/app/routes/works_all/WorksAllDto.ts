// 업무지시서(ALL) 타입

// 대분류 (상단 탭)
export type WorksAllDepartment = '영업' | '배전' | '기술';

// 처리 상태 (상태 필터 탭)
export type WorksAllStatus = '접수' | '처리 중' | '완료' | '협의';

// 결재 상태
export type WorksAllApprovalStatus = '결재 완료' | '미요청';

// ─── 업무지시서(ALL) 목록 조회 ──────────────────────────────────────
export interface WorksAllListItem {
  workOrderNo: string;        // 예: WO-2024-001
  title: string;               // 제목
  workType: string;            // 유형 (데이터 작업 / 시스템 개발 / 장애 처리 / 기타 등)
  department: WorksAllDepartment; // 대분류
  part: string;                 // 소분류 파트 (신증설 / 영업 일반 / 고객센터 / 검침 관리 / 요금 / 수금관리 / 한전on 등)
  managerName: string;          // 담당자
  approvalStatus: WorksAllApprovalStatus; // 결재
  status: WorksAllStatus;          // 상태
  regDt: string;                // 등록일 YYYYMMDD
  dueDt: string;                 // 마감일 YYYYMMDD
}
