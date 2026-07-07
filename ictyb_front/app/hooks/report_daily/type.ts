// 영업 점검일지에서 사용 가능한 파트 (ybict_part_info 기준, 백엔드에서 동적 조회)
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
  file?: File;   // 신규 첨부(등록 화면)에서 업로드할 실제 파일
  seq?: string;  // 서버에 저장된 기존 첨부파일(상세 화면) 식별자, 다운로드에 사용
}

// 파트 1개에 해당하는 점검일지 작성 내용
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
}

// 점검일지 목록 1건 (백엔드에서 집계된 값 그대로 사용)
export interface SalesDailyReportListItem {
  reportId: number;
  reportDate: string; // yyyy-MM-dd
  authorName: string;
  partNames: string[];
  totalInProgress: number;
  totalDelayed: number;
  totalDistributed: number;
  attachmentCount: number;
}

// 점검일지 상세 (모든 파트를 한 번에 취합해서 등록된 내용)
export interface SalesDailyReportDetail {
  reportId: number;
  reportDate: string;
  authorSabun: string;
  authorName: string;
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
