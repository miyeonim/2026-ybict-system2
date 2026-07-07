import apiClient from '@hooks/common/service/clientService';
import type { BaseResponse } from '../common/base';
import type { DeptKey, WorkPartBarRow, WorkPartSummaryResponse } from '@/hooks/work_part/type';
import type { AlertItem } from '@hooks/report_total/types';

interface FetchSummaryParams {
  year: number;
  type: DeptKey;
  sabun?: string;
}

export interface AlertPageResponse {
  content: AlertItem[];
  totalElements: number;
  totalPages: number;
}

interface AlertQueryParams {
  year: number;
  type: DeptKey;
  sabun?: string;
  page: number; // 1-based (백엔드가 1-based로 받음)
  size: number;
}

/**
 * 작업지시서 처리 현황 요약 조회
 * GET /api/work_part/v1.0/summary?year=&type=&sabun=
 */
export const fetchWorkPartSummary = async ({
  year,
  type,
  sabun,
}: FetchSummaryParams): Promise<WorkPartSummaryResponse | null> => {
  try {
    const response = await apiClient.get<BaseResponse<WorkPartSummaryResponse>>(
      '/api/work_part/v1.0/summary',
      { params: { year, type, sabun } },
    );
    return response.data.data;
  } catch (error) {
    console.error('작업지시서 처리 현황 조회 실패', error);
    return null;
  }
};

/**
 * 장기 미처리 알림 조회
 * GET /api/work_part/v1.0/alerts/long?year=&type=&sabun=&page=&size=
 */
export const fetchLongAlerts = async ({
  year,
  type,
  sabun,
  page,
  size,
}: AlertQueryParams): Promise<AlertPageResponse | null> => {
  try {
    const response = await apiClient.get<BaseResponse<AlertPageResponse>>(
      '/api/work_part/v1.0/alerts/long',
      { params: { year, type, sabun, page, size } },
    );
    return response.data.data;
  } catch (error) {
    console.error('장기 미처리 알림 조회 실패', error);
    return null;
  }
};

/**
 * 마감 임박 알림 조회
 * GET /api/work_part/v1.0/alerts/due?year=&type=&sabun=&page=&size=
 */
export const fetchDueAlerts = async ({
  year,
  type,
  sabun,
  page,
  size,
}: AlertQueryParams): Promise<AlertPageResponse | null> => {
  try {
    const response = await apiClient.get<BaseResponse<AlertPageResponse>>(
      '/api/work_part/v1.0/alerts/due',
      { params: { year, type, sabun, page, size } },
    );
    return response.data.data;
  } catch (error) {
    console.error('마감 임박 알림 조회 실패', error);
    return null;
  }
};