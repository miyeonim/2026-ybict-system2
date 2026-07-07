import apiClient from '@hooks/common/service/clientService';
import type { BaseResponse } from '../common/base';
import type { RankItem } from '@/hooks/report_total/types'; // 프로젝트 내 타입 정의 위치

/**
 * 월별 작업지시서 등록 현황 데이터를 조회합니다.
 * @param {string} year - 조회할 연도
 * @param {string} depTitle - 조회할 부서명
 * @returns {Promise<RankItem[]>} 월별 차트 데이터 배열
 */
export const fetchPartRankStats = async (
  year: string, 
  depTitle: string = "전체"
): Promise<RankItem[]> => {
  // 이제 백엔드에서 ChartData 형태({month, currentYY, prevYY})로 보내주므로 
  // 타입을 ChartData[]로 명시합니다.
  const response = await apiClient.get<BaseResponse<RankItem[]>>(`/api/report_total/v1.0/partrank`, {
    params: { 
      year, 
      depTitle 
    }
  });
  
  return response.data.data;
};