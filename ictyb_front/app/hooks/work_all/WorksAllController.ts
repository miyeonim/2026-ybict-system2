import type { BaseResponse } from '@hooks/common/base';
import type { WorksAllListItem } from '@routes/works_all/WorksAllDto';
import apiClient from '@hooks/common/service/clientService';

export interface WorksAllPageResponse {
  content: WorksAllListItem[];
  totalElements: number;
  totalPages: number;
}

export interface WorksAllCounts {
  deptCounts: Record<string, number>;
  partCounts: Record<string, number>;
  statusCounts: Record<string, number>;
  totalCount: number;
}

interface FetchWorksAllListParams {
  dept: string;
  part: string;
  status: string;
  startDueDt?: string;
  endDueDt?: string;
  seenNegotiations?: string[];
  page: number; // 1-based
  size: number;
}

interface FetchWorksAllCountsParams {
  dept: string;
  part: string;
  startDueDt?: string;
  endDueDt?: string;
  seenNegotiations?: string[];
}

/**
 * 업무지시서(ALL) 목록을 부서/파트/상태/마감일 범위 조건으로 페이지 단위 조회합니다.
 * @returns {Promise<WorksAllPageResponse>} 현재 페이지 목록 + 전체 건수/페이지 수
 */
export const fetchWorksAllList = async ({
  dept,
  part,
  status,
  startDueDt,
  endDueDt,
  seenNegotiations,
  page,
  size,
}: FetchWorksAllListParams): Promise<WorksAllPageResponse> => {
  const response = await apiClient.get<BaseResponse<WorksAllPageResponse>>('/api/work_all/v1.0/list', {
    params: {
      dept,
      part,
      status,
      startDueDt,
      endDueDt,
      seenNegotiations: seenNegotiations?.join(',') || undefined,
      page,
      size,
    },
  });
  return response.data.data;
};

/**
 * 업무지시서(ALL) 부서/파트/상태 탭에 표시할 건수 배지를 조회합니다.
 * @returns {Promise<WorksAllCounts>} 부서별/파트별/상태별 건수
 */
export const fetchWorksAllCounts = async ({
  dept,
  part,
  startDueDt,
  endDueDt,
  seenNegotiations,
}: FetchWorksAllCountsParams): Promise<WorksAllCounts> => {
  const response = await apiClient.get<BaseResponse<WorksAllCounts>>('/api/work_all/v1.0/counts', {
    params: {
      dept,
      part,
      startDueDt,
      endDueDt,
      seenNegotiations: seenNegotiations?.join(',') || undefined,
    },
  });
  return response.data.data;
};
