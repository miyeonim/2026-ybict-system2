// hooks/report_total/AlertController.ts
import apiClient from '@hooks/common/service/clientService';
import type { BaseResponse } from '../common/base';
import type { AlertItem } from '@/hooks/report_total/types';

// Spring Boot의 Page 객체 타입 정의
export interface PageData<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

/**
 * 장기 미처리 알림 데이터를 페이징 조회합니다.
 */
export const fetchLongAlerts = async (page: number, size: number): Promise<PageData<AlertItem>> => {
  const response = await apiClient.get<BaseResponse<PageData<AlertItem>>>(`/api/report_total/v1.0/alerts/long`, {
    params: { page, size }
  });
  return response.data.data;
};

/**
 * 마감 임박 알림 데이터를 페이징 조회합니다. 
 * (백엔드에 해당 API가 동일한 구조로 구현되어 있다고 가정)
 */
export const fetchDueAlerts = async (page: number, size: number): Promise<PageData<AlertItem>> => {
  const response = await apiClient.get<BaseResponse<PageData<AlertItem>>>(`/api/report_total/v1.0/alerts/due`, {
    params: { page, size }
  });
  return response.data.data;
};