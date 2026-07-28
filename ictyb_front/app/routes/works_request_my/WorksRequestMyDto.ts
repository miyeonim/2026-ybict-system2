// 작업 요청서(MY) 타입 - 한전 사람 전용 화면. its_real_work_report 기반이라
// its_it_work_report(작업지시서) 목록과 달리 부서·파트, 작업결과, 협의 개념이 아직 없다.
import type { WorksMyApprovalHistoryItem, WorksMyAttachmentItem } from "@routes/works_my/WorksMyDto";

// 102(요청서 접수 전) 승인만 구현되어 있어 '결재 완료'는 아직 나오지 않지만, 프론트 표시 형식은
// 작업지시서(MY)와 동일하게 맞춰둔다.
export type WorksRequestMyApprovalStatus = "결재 대기" | "결재 완료" | "미요청";

// 협의/진행중처럼 요청서에 아직 없는 개념의 탭은 만들지 않고, 실제로 의미 있는 2개만 둔다.
export type WorksRequestMyTabKey = "결재대기" | "전체";

export interface WorksRequestMyListItem {
  workRequestNo: string;
  title: string;
  approvalStatus: WorksRequestMyApprovalStatus;
  status: string;
  dueDt: string; // 희망완료일 (yyyyMMdd... 형식, 프론트에서 앞 8자리만 잘라 표시)
  approvalHistory: WorksMyApprovalHistoryItem[];
}

export interface WorksRequestMyDetail {
  workRequestNo: string;
  changeTitle: string; // 제목
  chgRsnCtt: string;   // 목적 및 근거
  changeReason: string; // 요청사항
  serviceType: string | null;
  serviceTypeLabel: string | null;
  systemCd: string | null;
  systemCdLabel: string | null;
  businessField: string | null;
  expectedDt: string | null; // 희망완료일 yyyy-MM-dd
  sendItSabun: string | null; // IT담당자 (103 접수 전엔 null)
  sendItName: string | null;
  workSabun: string | null;   // 작업책임자 (아직 자동 지정 로직 전이라 항상 null)
  workName: string | null;
  regUserSabun: string;
  regUserName: string;
  regUserDepNm: string | null;
  regDt: string; // 요청 일시 yyyyMMddHHmmss
  isSecret: string | null;          // 개인정보 포함 여부 - Y/N
  isSecretLabel: string | null;     // 개인정보 포함 여부 - "포함"/"미포함"
  oppbClYn: string | null;          // 공개구분 - Y(대외 게시용)/N(기타)
  userSecretContent: string | null; // 개인정보 포함 항목
  attachExpireDate: string | null;  // 파기일 (yyyy-MM-dd)
  attachments: WorksMyAttachmentItem[];
  currentActId: string | null;
  myTurn: boolean;
  approvalHistory: WorksMyApprovalHistoryItem[];
}
