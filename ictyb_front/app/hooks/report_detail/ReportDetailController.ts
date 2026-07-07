import apiClient from '@hooks/common/service/clientService';
import type { BaseResponse } from '../common/base';
import type { ReportDetailDto } from '@/hooks/report_detail/type'; 

/**
 * 작업지시서 전체 목록을 조회합니다.
 */
export const getReportDetail = async (
  startDt: string, 
  endDt: string
): Promise<ReportDetailDto[]> => {
  const response = await apiClient.get<BaseResponse<ReportDetailDto[]>>(`/api/report_detail/v1.0/list`, {
    params: { startDt, endDt }
  });
  return response.data.data;
};