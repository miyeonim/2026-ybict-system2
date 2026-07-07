import apiClient from '@hooks/common/service/clientService';
import type { BaseResponse } from '../common/base';
import type { DeptSection } from '@hooks/report_total/types'; // 프로젝트 내 타입 정의 위치

/**
 * 부서별/파트별 완료율 통계 데이터를 조회합니다.
 * @param {string} year - 조회할 연도
 * @returns {Promise<DeptSection[]>} 부서별 데이터 배열
 */
export const fetchDeptCompletionStats = async (year: string): Promise<DeptSection[]> => {
  // 백엔드 컨트롤러와 매핑되는 URL: /api/report_total/partcompletion
  const response = await apiClient.get<BaseResponse<DeptSection[]>>(`/api/report_total/v1.0/partcompletion`, {
    params: { year }
  });
  return response.data.data; // BaseResponse 구조에 맞춰 data 필드 반환
};