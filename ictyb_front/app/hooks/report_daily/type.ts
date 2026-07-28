// 영업 점검일지에서 사용 가능한 파트 (ictyb_part_info 기준, 백엔드에서 동적 조회)
export interface PartOption {
  partId: string;
  partNm: string;
  partOrder: number;
}

export interface PersonCount {
  name: string;
  inProgress: number;  // 진행중 (DB 작업지시서 건수)
  delayed: number;     // 일정지연 (DB 작업지시서 건수)
  distributed: number; // 배포건수 (DB 미연동, 직접 입력/조정)
}

export interface SalesDailyReportAttachment {
  name: string;
  size: number;
  file?: File;   // 신규 첨부(작성 화면)에서 업로드할 실제 파일
  seq?: string;  // 서버에 저장된 기존 첨부파일 식별자, 다운로드에 사용
}

// 파트별 결재 상태
export type ReportPartStatus = "DRAFT" | "SUBMITTED" | "PART_APPROVED" | "FINAL_APPROVED" | "REJECTED";

// 호출자(로그인 사용자) 기준, 서버가 계산해서 내려주는 이 파트에 대한 권한
export type PartMyRole = "AUTHOR" | "LEADER" | "HEAD" | "VIEWER";

// 파트 1개에 해당하는 점검일지 작성 내용 + 결재 상태
export interface PartSectionContent {
  partId: string;
  partNm: string;
  people: PersonCount[];
  efficiencyContent: string;      // 유지보수 관리 효율화 및 주요 배포내용
  mainInstructionContent: string; // 주요 작업지시 내용
  wasErrorContent: string;        // WAS 오류 내역 (10건 이상 발생건)
  meetingSchedule: string;        // 회의예정
  specialNotes: string;           // 특이사항
  attachments: SalesDailyReportAttachment[];

  status: ReportPartStatus;
  authorSabun: string | null;
  authorName: string | null;
  submittedDt: string | null;
  partLeaderName: string | null;
  partLeaderApprovedDt: string | null;
  headName: string | null;
  headApprovedDt: string | null;
  rejectedByName: string | null;
  rejectedByRole: "PART_LEADER" | "HEAD" | null;
  rejectReason: string | null;
  rejectedDt: string | null;

  myRole: PartMyRole;
  canEdit: boolean;
  canApprove: boolean;
  canReject: boolean;
}

// 점검일지 목록 1건 (백엔드에서 집계된 값 그대로 사용)
export interface SalesDailyReportListItem {
  reportId: number;
  reportDate: string; // yyyy-MM-dd
  authorName: string; // 최종 수정자
  partNames: string[];
  totalInProgress: number;
  totalDelayed: number;
  totalDistributed: number;
  attachmentCount: number;
  overallStatus: string; // 작성중/파트장결재대기/부장결재대기/승인완료/반려
}

// 점검일지 상세 (날짜 기준, 파트별로 작성자/결재 상태가 다를 수 있음)
export interface SalesDailyReportDetail {
  reportId: number | null; // 아직 아무도 제출하지 않은 날짜라면 null
  reportDate: string;
  authorSabun: string | null; // 최종 수정자
  authorName: string | null;
  parts: PartSectionContent[];
}

export interface PersonCountTotals {
  totalInProgress: number;
  totalDelayed: number;
  totalSum: number;
  totalDistributed: number;
}

export function calcTotals(people: PersonCount[]): PersonCountTotals {
  const totalInProgress = people.reduce((s, p) => s + p.inProgress, 0);
  const totalDelayed = people.reduce((s, p) => s + p.delayed, 0);
  const totalDistributed = people.reduce((s, p) => s + p.distributed, 0);
  return {
    totalInProgress,
    totalDelayed,
    totalSum: totalInProgress + totalDelayed,
    totalDistributed,
  };
}

export function createEmptyPartSection(part: PartOption): PartSectionContent {
  return {
    partId: part.partId,
    partNm: part.partNm,
    people: [],
    efficiencyContent: "",
    mainInstructionContent: "",
    wasErrorContent: "",
    meetingSchedule: "",
    specialNotes: "",
    attachments: [],
    status: "DRAFT",
    authorSabun: null,
    authorName: null,
    submittedDt: null,
    partLeaderName: null,
    partLeaderApprovedDt: null,
    headName: null,
    headApprovedDt: null,
    rejectedByName: null,
    rejectedByRole: null,
    rejectReason: null,
    rejectedDt: null,
    myRole: "VIEWER",
    canEdit: false,
    canApprove: false,
    canReject: false,
  };
}

export function isPartSectionFilled(section: PartSectionContent): boolean {
  return (
    section.people.length > 0 ||
    !!section.efficiencyContent.trim() ||
    !!section.mainInstructionContent.trim() ||
    !!section.wasErrorContent.trim() ||
    !!section.meetingSchedule.trim() ||
    !!section.specialNotes.trim() ||
    section.attachments.length > 0
  );
}
