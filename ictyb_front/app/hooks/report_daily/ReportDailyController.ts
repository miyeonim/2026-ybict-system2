import apiClient from '@hooks/common/service/clientService';
import type { BaseResponse } from '@hooks/common/base';
import type { JwtUserDto } from '@hooks/common/jwt/useauthDto';
import type {
  PartOption,
  PartMyRole,
  PartSectionContent,
  PersonCount,
  ReportPartStatus,
  SalesDailyReportDetail,
  SalesDailyReportListItem,
} from '@hooks/report_daily/type';

// ── 백엔드 ReportDailyDto 응답 원본 형태 (필드명이 프론트 타입과 다름) ──
interface RawPerson {
  personNm: string;
  inProgressCnt: number;
  delayedCnt: number;
  distributedCnt: number;
}

interface RawAttach {
  seq: string;
  realFileName: string;
  fileSize: number;
  regDt: string;
}

interface RawPartDetail {
  partId: string;
  partNm: string;
  people: RawPerson[];
  efficiencyContent: string;
  mainInstructionContent: string;
  wasErrorContent: string;
  meetingSchedule: string;
  specialNotes: string;
  attachments: RawAttach[];
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

interface RawReportDetail {
  reportId: number | null;
  reportDate: string;
  authorSabun: string | null;
  authorName: string | null;
  parts: RawPartDetail[];
}

const mapPartDetail = (p: RawPartDetail): PartSectionContent => ({
  partId: p.partId,
  partNm: p.partNm,
  people: p.people.map((person) => ({
    name: person.personNm,
    inProgress: person.inProgressCnt,
    delayed: person.delayedCnt,
    distributed: person.distributedCnt,
  })),
  efficiencyContent: p.efficiencyContent,
  mainInstructionContent: p.mainInstructionContent,
  wasErrorContent: p.wasErrorContent,
  meetingSchedule: p.meetingSchedule,
  specialNotes: p.specialNotes,
  attachments: p.attachments.map((a) => ({
    name: a.realFileName,
    size: a.fileSize,
    seq: a.seq,
  })),
  status: p.status ?? "DRAFT",
  authorSabun: p.authorSabun,
  authorName: p.authorName,
  submittedDt: p.submittedDt,
  partLeaderName: p.partLeaderName,
  partLeaderApprovedDt: p.partLeaderApprovedDt,
  headName: p.headName,
  headApprovedDt: p.headApprovedDt,
  rejectedByName: p.rejectedByName,
  rejectedByRole: p.rejectedByRole,
  rejectReason: p.rejectReason,
  rejectedDt: p.rejectedDt,
  myRole: p.myRole ?? "VIEWER",
  canEdit: p.canEdit ?? false,
  canApprove: p.canApprove ?? false,
  canReject: p.canReject ?? false,
});

/**
 * 영업 점검일지에서 사용 가능한 파트 목록을 조회합니다. (ictyb_part_info 기준)
 */
export const fetchPartOptions = async (): Promise<PartOption[]> => {
  const response = await apiClient.get<BaseResponse<PartOption[]>>('/api/report_daily/v1.0/parts');
  return response.data.data;
};

/**
 * 파트 소속 인원별 진행중/일정지연 작업지시서 건수를 조회합니다. (DB 집계)
 */
export const fetchPersonStats = async (partId: string): Promise<PersonCount[]> => {
  const response = await apiClient.get<BaseResponse<{ personNm: string; inProgressCnt: number; delayedCnt: number }[]>>(
    `/api/report_daily/v1.0/parts/${partId}/person-stats`
  );
  return response.data.data.map((s) => ({
    name: s.personNm,
    inProgress: s.inProgressCnt,
    delayed: s.delayedCnt,
    distributed: 0,
  }));
};

/**
 * 영업 점검일지 목록을 조회합니다.
 */
export const fetchReportList = async (): Promise<SalesDailyReportListItem[]> => {
  const response = await apiClient.get<BaseResponse<SalesDailyReportListItem[]>>('/api/report_daily/v1.0/list');
  return response.data.data;
};

/**
 * 날짜 기준으로 영업 점검일지를 조회합니다. 아직 아무도 제출하지 않은 날짜라면
 * 파트 목록만 채운 빈 스켈레톤이 내려옵니다 (reportId=null).
 */
export const fetchReportByDate = async (
  reportDate: string,
  userEmpno: string,
): Promise<SalesDailyReportDetail> => {
  const response = await apiClient.get<BaseResponse<RawReportDetail>>(
    `/api/report_daily/v1.0/by-date/${reportDate}`,
    { params: { userEmpno } },
  );
  const raw = response.data.data;
  return {
    reportId: raw.reportId,
    reportDate: raw.reportDate,
    authorSabun: raw.authorSabun,
    authorName: raw.authorName,
    parts: raw.parts.map(mapPartDetail),
  };
};

/**
 * 파트 내용을 제출합니다 (파트 소속 직원이 자기 파트를 작성/재작성 후 제출).
 */
export const submitPart = async (
  reportDate: string,
  section: PartSectionContent,
  actor: Pick<JwtUserDto, 'userEmpno' | 'empNm'>,
): Promise<void> => {
  const reportData = {
    actorSabun: actor.userEmpno,
    actorName: actor.empNm,
    reportDate,
    people: section.people.map((person) => ({
      personNm: person.name,
      inProgressCnt: person.inProgress,
      delayedCnt: person.delayed,
      distributedCnt: person.distributed,
    })),
    efficiencyContent: section.efficiencyContent,
    mainInstructionContent: section.mainInstructionContent,
    wasErrorContent: section.wasErrorContent,
    meetingSchedule: section.meetingSchedule,
    specialNotes: section.specialNotes,
  };

  const formData = new FormData();
  formData.append('reportData', new Blob([JSON.stringify(reportData)], { type: 'application/json' }));
  section.attachments.forEach((a) => {
    if (a.file) formData.append('files', a.file);
  });

  await apiClient.post<BaseResponse<null>>(`/api/report_daily/v1.0/parts/${section.partId}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

/**
 * 파트 결재를 승인합니다 (파트장 또는 부장).
 */
export const approvePart = async (
  reportId: number,
  partId: string,
  actor: Pick<JwtUserDto, 'userEmpno' | 'empNm'>,
): Promise<void> => {
  await apiClient.post<BaseResponse<null>>(`/api/report_daily/v1.0/${reportId}/parts/${partId}/approve`, {
    actorSabun: actor.userEmpno,
    actorName: actor.empNm,
  });
};

/**
 * 파트 결재를 반려합니다 (파트장 또는 부장, 사유 필수).
 */
export const rejectPart = async (
  reportId: number,
  partId: string,
  actor: Pick<JwtUserDto, 'userEmpno' | 'empNm'>,
  reason: string,
): Promise<void> => {
  await apiClient.post<BaseResponse<null>>(`/api/report_daily/v1.0/${reportId}/parts/${partId}/reject`, {
    actorSabun: actor.userEmpno,
    actorName: actor.empNm,
    reason,
  });
};

/**
 * 첨부파일을 브라우저를 통해 즉시 다운로드합니다.
 */
export const downloadAttach = (reportId: number, partId: string, seq: string, realFileName: string) => {
  const url = `http://localhost:8082/api/report_daily/v1.0/attach/download?reportId=${reportId}&partId=${partId}&seq=${seq}`;
  const link = document.createElement('a');
  link.href = url;
  link.download = realFileName;
  link.click();
};
