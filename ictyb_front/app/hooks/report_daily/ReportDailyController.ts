import apiClient from '@hooks/common/service/clientService';
import type { BaseResponse } from '@hooks/common/base';
import type {
  PartOption,
  PartSectionContent,
  PersonCount,
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
}

interface RawReportDetail {
  reportId: number;
  reportDate: string;
  authorSabun: string;
  authorName: string;
  parts: RawPartDetail[];
}

/**
 * 영업 점검일지에서 사용 가능한 파트 목록을 조회합니다. (ybict_part_info 기준)
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
 * 영업 점검일지 상세를 조회합니다.
 */
export const fetchReportDetail = async (reportId: number): Promise<SalesDailyReportDetail> => {
  const response = await apiClient.get<BaseResponse<RawReportDetail>>(`/api/report_daily/v1.0/${reportId}`);
  const raw = response.data.data;
  return {
    reportId: raw.reportId,
    reportDate: raw.reportDate,
    authorSabun: raw.authorSabun,
    authorName: raw.authorName,
    parts: raw.parts.map((p) => ({
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
    })),
  };
};

/**
 * 영업 점검일지를 등록합니다. (파트별 내용 + 첨부파일 일괄 저장)
 */
export const registerReport = async (
  reportDate: string,
  parts: PartSectionContent[]
): Promise<void> => {
  const reportData = {
    reportDate,
    parts: parts.map((p) => ({
      partId: p.partId,
      people: p.people.map((person) => ({
        personNm: person.name,
        inProgressCnt: person.inProgress,
        delayedCnt: person.delayed,
        distributedCnt: person.distributed,
      })),
      efficiencyContent: p.efficiencyContent,
      mainInstructionContent: p.mainInstructionContent,
      wasErrorContent: p.wasErrorContent,
      meetingSchedule: p.meetingSchedule,
      specialNotes: p.specialNotes,
    })),
  };

  const formData = new FormData();
  formData.append('reportData', new Blob([JSON.stringify(reportData)], { type: 'application/json' }));
  parts.forEach((p) => {
    p.attachments.forEach((a) => {
      if (a.file) formData.append(`files_${p.partId}`, a.file);
    });
  });

  await apiClient.post<BaseResponse<null>>('/api/report_daily/v1.0/register', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
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
