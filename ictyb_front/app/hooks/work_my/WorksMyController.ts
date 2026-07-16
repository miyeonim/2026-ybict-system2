import type { BaseResponse } from '@hooks/common/base';
import type {
  WorksMyListItem,
  WorksMyCandidate,
  WorksMyCreateOptions,
  WorksMyCreateRequest,
  WorksMyUpdateRequest,
  WorksMyNextCandidatesResponse,
  WorksMyReturnTargetsResponse,
  WorksMyDetail,
} from '@routes/works_my/WorksMyDto';
import apiClient from '@hooks/common/service/clientService';

/**
 * 업무지시서(MY) 목록을 조회합니다. (로그인 사용자 본인 관련 건)
 */
export const fetchWorksMyList = async (): Promise<WorksMyListItem[]> => {
  const res = await apiClient.get<BaseResponse<WorksMyListItem[]>>(
    '/api/work_my/v1.0/list',
  );
  return res.data.data ?? [];
};

/**
 * 결재대기 건에 대해 다음 단계로 지정할 수 있는 담당자 후보를 조회합니다. (현재 대기 단계 코드 포함)
 * 로그인 사용자가 현재 결재자가 아닌 경우 서버에서 거부됩니다.
 */
export const fetchNextCandidates = async (
  workOrderNo: string,
): Promise<WorksMyNextCandidatesResponse> => {
  const res = await apiClient.get<BaseResponse<WorksMyNextCandidatesResponse>>(
    `/api/work_my/v1.0/${workOrderNo}/next-candidates`,
  );
  return res.data.data ?? { currentActId: '', candidates: [] };
};

/**
 * 현재 단계를 승인하고, 지정한 다음 단계 담당자에게 결재를 넘깁니다.
 * @param workResult 조치사항 (109단계 "작업결과 보고" 승인 시 필수)
 * @param files 조치사항 첨부파일 (109단계에서만 사용, 선택, 0개 이상)
 */
export const submitApproval = async (
  workOrderNo: string,
  next: WorksMyCandidate,
  workResult?: string,
  files: File[] = [],
): Promise<void> => {
  const formData = new FormData();
  formData.append(
    'approveData',
    new Blob([JSON.stringify({ nextSabun: next.sabun, nextName: next.name, workResult })], {
      type: 'application/json',
    }),
  );
  files.forEach((f) => formData.append('files', f));

  await apiClient.post(`/api/work_my/v1.0/${workOrderNo}/approve`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

/**
 * 작업지시서를 수정합니다. 지시서 작성(104)/지시서 승인(106) 단계의 현재 결재자만 가능합니다
 * (이 두 단계는 항상 한전 담당자가 처리하므로, 곧 한전 담당자만 수정할 수 있다는 뜻입니다).
 * @param newFiles 새로 추가할 첨부파일 (선택, 0개 이상)
 */
export const updateWorkOrder = async (
  workOrderNo: string,
  req: WorksMyUpdateRequest,
  newFiles: File[] = [],
): Promise<void> => {
  const formData = new FormData();
  formData.append('updateData', new Blob([JSON.stringify(req)], { type: 'application/json' }));
  newFiles.forEach((f) => formData.append('files', f));

  await apiClient.post(`/api/work_my/v1.0/${workOrderNo}/update`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

/**
 * 업무지시서 상세를 조회합니다. (등록 정보 + 첨부파일 + 작업결과/조치사항)
 */
export const fetchWorkDetail = async (workOrderNo: string): Promise<WorksMyDetail> => {
  const res = await apiClient.get<BaseResponse<WorksMyDetail>>(
    `/api/work_my/v1.0/${workOrderNo}/detail`,
  );
  return res.data.data;
};

/**
 * 첨부파일을 브라우저를 통해 즉시 다운로드합니다.
 */
export const downloadWorkAttach = (instId: string, seq: string, realFileName: string) => {
  const url = `http://localhost:8082/api/work_my/v1.0/attach/download?instId=${instId}&seq=${seq}`;
  const link = document.createElement('a');
  link.href = url;
  link.download = realFileName;
  link.click();
};

/**
 * 작업결과(조치사항) 첨부파일을 브라우저를 통해 즉시 다운로드합니다.
 */
export const downloadWorkResultAttach = (instId: string, seq: string, realFileName: string) => {
  const url = `http://localhost:8082/api/work_my/v1.0/work-result-attach/download?instId=${instId}&seq=${seq}`;
  const link = document.createElement('a');
  link.href = url;
  link.download = realFileName;
  link.click();
};

/**
 * 반송 대상으로 선택할 수 있는 이전 결재 단계 목록을 조회합니다. (직전 단계부터 오래된 순)
 * 로그인 사용자가 현재 결재자가 아닌 경우 서버에서 거부됩니다.
 */
export const fetchReturnTargets = async (
  workOrderNo: string,
): Promise<WorksMyReturnTargetsResponse> => {
  const res = await apiClient.get<BaseResponse<WorksMyReturnTargetsResponse>>(
    `/api/work_my/v1.0/${workOrderNo}/return-targets`,
  );
  return res.data.data ?? { currentActId: '', targets: [] };
};

/**
 * 현재 단계를 지정한 이전 단계 처리자에게 반송합니다. (사유 필수)
 * @param targetActId 반송 대상 단계 코드. 생략 시 직전 단계로 반송합니다.
 */
export const submitReturn = async (
  workOrderNo: string,
  reason: string,
  targetActId?: string,
): Promise<void> => {
  await apiClient.post(`/api/work_my/v1.0/${workOrderNo}/return`, { reason, targetActId });
};

/**
 * 업무지시서 등록 폼의 코드성 드롭다운 옵션을 조회합니다. (임시 코드)
 */
export const fetchCreateOptions = async (): Promise<WorksMyCreateOptions> => {
  const res = await apiClient.get<BaseResponse<WorksMyCreateOptions>>(
    '/api/work_my/v1.0/create/options',
  );
  return res.data.data;
};

/**
 * 최초 결재자(한전 파트장) 후보를 조회합니다.
 */
export const fetchInitialApproverCandidates = async (): Promise<
  WorksMyCandidate[]
> => {
  const res = await apiClient.get<BaseResponse<WorksMyCandidate[]>>(
    '/api/work_my/v1.0/create/initial-approver-candidates',
  );
  return res.data.data ?? [];
};

/**
 * 등록 다이얼로그를 열 때 미리 보여줄 등록번호를 예약합니다.
 * 반환된 번호를 그대로 createWorkOrder 요청에 실어 보내면 그 번호가 실제로 저장됩니다.
 */
export const fetchReservedWorkOrderNo = async (): Promise<string> => {
  const res = await apiClient.get<BaseResponse<string>>(
    '/api/work_my/v1.0/create/reserve-no',
  );
  return res.data.data ?? '';
};

/**
 * 업무지시서를 신규 등록합니다. (첨부파일 포함)
 * @returns 생성된 업무지시서 처리번호(INST_ID)
 */
export const createWorkOrder = async (
  req: WorksMyCreateRequest,
  files: File[] = [],
): Promise<string> => {
  const formData = new FormData();
  formData.append('createData', new Blob([JSON.stringify(req)], { type: 'application/json' }));
  files.forEach((f) => formData.append('files', f));

  const res = await apiClient.post<BaseResponse<string>>(
    '/api/work_my/v1.0/create',
    formData,
    { headers: { 'Content-Type': 'multipart/form-data' } },
  );
  return res.data.data;
};
