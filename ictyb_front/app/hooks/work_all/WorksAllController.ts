import type { BaseResponse } from '@hooks/common/base';
import type { WorksAllListItem } from '@routes/works_all/WorksAllDto';
import apiClient from '@hooks/common/service/clientService';

/**
 * 업무지시서(ALL) 목록을 전체 조회합니다.
 * @returns {Promise<WorksAllListItem[]>} 업무지시서 목록 배열
 */
export const fetchWorksAllList = async (): Promise<WorksAllListItem[]> => {
  const response = await apiClient.get<BaseResponse<WorksAllListItem[]>>('/api/work_all/v1.0/list');
  return response.data.data;
};
