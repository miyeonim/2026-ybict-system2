import type { BaseResponse } from '@hooks/common/base';
import type { JwtUserDto } from '@hooks/common/jwt/useauthDto';
import type {
  WorksMyListItem,
  WorksMyCandidate,
  WorksMyCreateOptions,
  WorksMyCreateRequest,
  WorksMyCreateWorkRequestReq,
  WorksMyUpdateRequest,
  WorksMyNextCandidatesResponse,
  WorksMyReturnTargetsResponse,
  WorksMyDetail,
  WorksMySecretInfo,
} from '@routes/works_my/WorksMyDto';
import apiClient from '@hooks/common/service/clientService';

/**
 * 업무지시서(MY) 목록을 조회합니다. (로그인 사용자 본인 관련 건)
 * 서버가 토큰으로 사용자를 검증하지 않으므로, 프론트(useAuthContext)의 사번을 쿼리로 실어 보낸다.
 */
export const fetchWorksMyList = async (userEmpno: string): Promise<WorksMyListItem[]> => {
  const res = await apiClient.get<BaseResponse<WorksMyListItem[]>>(
    '/api/work_my/v1.0/list',
    { params: { userEmpno } },
  );
  return res.data.data ?? [];
};

/**
 * 결재대기 건에 대해 다음 단계로 지정할 수 있는 담당자 후보를 조회합니다. (현재 대기 단계 코드 포함)
 * 로그인 사용자가 현재 결재자가 아닌 경우 서버에서 거부됩니다.
 */
export const fetchNextCandidates = async (
  workOrderNo: string,
  userEmpno: string,
): Promise<WorksMyNextCandidatesResponse> => {
  const res = await apiClient.get<BaseResponse<WorksMyNextCandidatesResponse>>(
    `/api/work_my/v1.0/${workOrderNo}/next-candidates`,
    { params: { userEmpno } },
  );
  return res.data.data ?? { currentActId: '', candidates: [] };
};

/**
 * 현재 단계를 승인하고, 지정한 다음 단계 담당자에게 결재를 넘깁니다.
 * @param actor 승인 처리자(로그인 사용자)
 * @param workResult 조치사항 (109단계 "작업결과 보고" 승인 시 필수)
 * @param files 조치사항 첨부파일 (109단계에서만 사용, 선택, 0개 이상)
 * @param secretInfo 조치사항 자체의 개인정보 포함 여부 (109단계에서만 사용)
 */
export const submitApproval = async (
  workOrderNo: string,
  actor: Pick<JwtUserDto, 'userEmpno' | 'empNm'>,
  next: WorksMyCandidate,
  workResult?: string,
  files: File[] = [],
  secretInfo?: WorksMySecretInfo,
): Promise<void> => {
  const formData = new FormData();
  formData.append(
    'approveData',
    new Blob(
      [JSON.stringify({
        actorSabun: actor.userEmpno,
        actorName: actor.empNm,
        nextSabun: next.sabun,
        nextName: next.name,
        workResult,
        ...secretInfo,
      })],
      { type: 'application/json' },
    ),
  );
  files.forEach((f) => formData.append('files', f));

  await apiClient.post(`/api/work_my/v1.0/${workOrderNo}/approve`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

/**
 * 작업지시서를 수정합니다. 지시서 작성(104)/지시서 승인(106) 단계의 현재 결재자만 가능합니다
 * (이 두 단계는 항상 한전 담당자가 처리하므로, 곧 한전 담당자만 수정할 수 있다는 뜻입니다).
 * @param actorSabun 수정 처리자(로그인 사용자) 사번
 * @param newFiles 새로 추가할 첨부파일 (선택, 0개 이상)
 */
export const updateWorkOrder = async (
  workOrderNo: string,
  actorSabun: string,
  req: WorksMyUpdateRequest,
  newFiles: File[] = [],
): Promise<void> => {
  const formData = new FormData();
  formData.append(
    'updateData',
    new Blob([JSON.stringify({ ...req, actorSabun })], { type: 'application/json' }),
  );
  newFiles.forEach((f) => formData.append('files', f));

  await apiClient.post(`/api/work_my/v1.0/${workOrderNo}/update`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
};

/**
 * 업무지시서 상세를 조회합니다. (등록 정보 + 첨부파일 + 작업결과/조치사항)
 */
export const fetchWorkDetail = async (workOrderNo: string, userEmpno: string): Promise<WorksMyDetail> => {
  const res = await apiClient.get<BaseResponse<WorksMyDetail>>(
    `/api/work_my/v1.0/${workOrderNo}/detail`,
    { params: { userEmpno } },
  );
  return res.data.data;
};

/**
 * 업무지시서를 삭제합니다. 등록자 본인만 가능하며, 서버에서 거부되면 에러가 던져집니다.
 */
export const deleteWorkOrder = async (workOrderNo: string, userEmpno: string): Promise<void> => {
  await apiClient.delete(`/api/work_my/v1.0/${workOrderNo}`, { params: { userEmpno } });
};

/**
 * 처리기간을 연장합니다. 결과 보고(109, KDN 대리) 단계 결재대기 중, 지시서 작성자 본인만 가능합니다.
 * 처리예정일은 늘어난 일수만큼 자동으로 함께 밀립니다. 서버에서 거부되면 에러가 던져집니다.
 * @param newWorkPeriod 새 처리기간 (일), 기존 처리기간보다 커야 합니다.
 */
export const extendWorkPeriod = async (
  workOrderNo: string,
  actorSabun: string,
  newWorkPeriod: string,
): Promise<WorksMyDetail> => {
  const res = await apiClient.post<BaseResponse<WorksMyDetail>>(
    `/api/work_my/v1.0/${workOrderNo}/extend-period`,
    { actorSabun, newWorkPeriod },
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
  userEmpno: string,
): Promise<WorksMyReturnTargetsResponse> => {
  const res = await apiClient.get<BaseResponse<WorksMyReturnTargetsResponse>>(
    `/api/work_my/v1.0/${workOrderNo}/return-targets`,
    { params: { userEmpno } },
  );
  return res.data.data ?? { currentActId: '', targets: [] };
};

/**
 * 현재 단계를 지정한 이전 단계 처리자에게 반송합니다. (사유 필수)
 * @param actor 반송 처리자(로그인 사용자)
 * @param targetActId 반송 대상 단계 코드. 생략 시 직전 단계로 반송합니다.
 */
export const submitReturn = async (
  workOrderNo: string,
  actor: Pick<JwtUserDto, 'userEmpno' | 'empNm'>,
  reason: string,
  targetActId?: string,
): Promise<void> => {
  await apiClient.post(`/api/work_my/v1.0/${workOrderNo}/return`, {
    actorSabun: actor.userEmpno,
    actorName: actor.empNm,
    reason,
    targetActId,
  });
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
 * 작업 요청서(100→102) 최초 결재자 후보를 조회합니다.
 * 요청자와 같은 소속(SOSOK_CD)인 한전 담당자만 후보로 내려옵니다.
 */
export const fetchRequestInitialApproverCandidates = async (
  depId: string,
): Promise<WorksMyCandidate[]> => {
  const res = await apiClient.get<BaseResponse<WorksMyCandidate[]>>(
    '/api/work_my/v1.0/request/initial-approver-candidates',
    { params: { depId } },
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
  regUser: Pick<JwtUserDto, 'userEmpno' | 'empNm' | 'depId' | 'depTitle'>,
  req: WorksMyCreateRequest,
  files: File[] = [],
): Promise<string> => {
  const formData = new FormData();
  formData.append(
    'createData',
    new Blob([JSON.stringify({
      ...req,
      regUserSabun: regUser.userEmpno,
      regUserName: regUser.empNm,
      regUserDepId: regUser.depId,
      regUserDepTitle: regUser.depTitle,
    })], { type: 'application/json' }),
  );
  files.forEach((f) => formData.append('files', f));

  const res = await apiClient.post<BaseResponse<string>>(
    '/api/work_my/v1.0/create',
    formData,
    { headers: { 'Content-Type': 'multipart/form-data' } },
  );
  return res.data.data;
};

/**
 * 작업 요청서를 신규 등록합니다. (요청서 작성(100) 완료 + 요청서 승인(102) 결재 대기 생성, 첨부파일 포함)
 * 102/103 승인·반송 화면은 아직 없으며, 이 함수는 등록까지만 처리합니다.
 * @returns 생성된 요청서 처리번호(INST_ID)
 */
export const createWorkRequest = async (
  regUser: Pick<JwtUserDto, 'userEmpno' | 'empNm' | 'depId' | 'depTitle'>,
  req: WorksMyCreateWorkRequestReq,
  files: File[] = [],
): Promise<string> => {
  const formData = new FormData();
  formData.append(
    'createData',
    new Blob([JSON.stringify({
      ...req,
      regUserSabun: regUser.userEmpno,
      regUserName: regUser.empNm,
      regUserDepId: regUser.depId,
      regUserDepTitle: regUser.depTitle,
    })], { type: 'application/json' }),
  );
  files.forEach((f) => formData.append('files', f));

  const res = await apiClient.post<BaseResponse<string>>(
    '/api/work_my/v1.0/request/create',
    formData,
    { headers: { 'Content-Type': 'multipart/form-data' } },
  );
  return res.data.data;
};
