import type { BaseResponse } from '@hooks/common/base';
import type { JwtUserDto } from '@hooks/common/jwt/useauthDto';
import type {
  WorksMyCandidate,
  WorksMyNextCandidatesResponse,
  WorksMyReturnTargetsResponse,
} from '@routes/works_my/WorksMyDto';
import type {
  WorksRequestMyListItem,
  WorksRequestMyDetail,
} from '@routes/works_request_my/WorksRequestMyDto';
import apiClient from '@hooks/common/service/clientService';

/**
 * 작업 요청서(MY) 목록을 조회합니다. (로그인 사용자 본인 관련 건 - 등록자/이력상 처리자/현재 결재대기자)
 */
export const fetchWorkRequestsMyList = async (userEmpno: string): Promise<WorksRequestMyListItem[]> => {
  const res = await apiClient.get<BaseResponse<WorksRequestMyListItem[]>>(
    '/api/work_my/v1.0/request/list',
    { params: { userEmpno } },
  );
  return res.data.data ?? [];
};

/**
 * 요청서 결재대기 건에 대해 다음 단계(103, 한전 IT부서 직원)로 지정할 수 있는 담당자 후보를 조회합니다.
 */
export const fetchRequestNextCandidates = async (
  workRequestNo: string,
  userEmpno: string,
): Promise<WorksMyNextCandidatesResponse> => {
  const res = await apiClient.get<BaseResponse<WorksMyNextCandidatesResponse>>(
    `/api/work_my/v1.0/request/${workRequestNo}/next-candidates`,
    { params: { userEmpno } },
  );
  return res.data.data ?? { currentActId: '', candidates: [] };
};

/**
 * 요청서의 현재 단계(102)를 승인하고, 지정한 다음 단계(103) 담당자에게 결재를 넘깁니다.
 */
export const approveWorkRequest = async (
  workRequestNo: string,
  actor: Pick<JwtUserDto, 'userEmpno' | 'empNm'>,
  next: WorksMyCandidate,
): Promise<void> => {
  await apiClient.post(`/api/work_my/v1.0/request/${workRequestNo}/approve`, {
    actorSabun: actor.userEmpno,
    actorName: actor.empNm,
    nextSabun: next.sabun,
    nextName: next.name,
  });
};

/**
 * 요청서 반송 대상으로 선택할 수 있는 이전 결재 단계 목록을 조회합니다.
 */
export const fetchRequestReturnTargets = async (
  workRequestNo: string,
  userEmpno: string,
): Promise<WorksMyReturnTargetsResponse> => {
  const res = await apiClient.get<BaseResponse<WorksMyReturnTargetsResponse>>(
    `/api/work_my/v1.0/request/${workRequestNo}/return-targets`,
    { params: { userEmpno } },
  );
  return res.data.data ?? { currentActId: '', targets: [] };
};

/**
 * 요청서의 현재 단계를 지정한 이전 단계 처리자에게 반송합니다. (사유 필수)
 */
export const returnWorkRequest = async (
  workRequestNo: string,
  actor: Pick<JwtUserDto, 'userEmpno' | 'empNm'>,
  reason: string,
  targetActId?: string,
): Promise<void> => {
  await apiClient.post(`/api/work_my/v1.0/request/${workRequestNo}/return`, {
    actorSabun: actor.userEmpno,
    actorName: actor.empNm,
    reason,
    targetActId,
  });
};

/**
 * 작업 요청서 상세를 조회합니다. (등록 정보 + 첨부파일 + 결재이력)
 */
export const fetchWorkRequestDetail = async (
  workRequestNo: string,
  userEmpno: string,
): Promise<WorksRequestMyDetail> => {
  const res = await apiClient.get<BaseResponse<WorksRequestMyDetail>>(
    `/api/work_my/v1.0/request/${workRequestNo}/detail`,
    { params: { userEmpno } },
  );
  return res.data.data;
};

/**
 * 작업 요청서 첨부파일을 브라우저를 통해 즉시 다운로드합니다.
 */
export const downloadRequestAttach = (instId: string, seq: string, realFileName: string) => {
  const url = `http://localhost:8082/api/work_my/v1.0/request/attach/download?instId=${instId}&seq=${seq}`;
  const link = document.createElement('a');
  link.href = url;
  link.download = realFileName;
  link.click();
};
