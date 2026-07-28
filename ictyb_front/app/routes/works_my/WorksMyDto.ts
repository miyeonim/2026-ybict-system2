// 업무지시서(MY) 타입

// 부서 (work_all과 동일한 분류 재사용)
export type WorksMyDepartment = '영업' | '배전' | '기술';

// 처리 상태 (work_all과 동일)
export type WorksMyStatus = '접수' | '처리 중' | '완료' | '협의';

// 결재 상태 - MY 화면은 본인이 결재해야 하는 건을 구분하기 위해 '결재 대기'를 추가로 둔다
export type WorksMyApprovalStatus = '결재 대기' | '결재 완료' | '미요청';

// 마이 워크스페이스 탭(워크플로우 단계) - approvalStatus/status 조합으로부터 파생되는 분류
export type WorksMyTabKey = '결재대기' | '협의' | '진행중' | '처리내역';

// 결재이력 항목 (승인/반려 처리자 + 시각, 시분초 포함)
// '결재대기'는 아직 결재하지 않고 현재 대기 중인 사람 - regDt 없음(처리 전이라 시각이 없음)
export interface WorksMyApprovalHistoryItem {
  sabun: string;
  name: string;
  actIdNm: string;         // 처리한(대기 중인) 단계명 (예: 지시서 승인)
  signLabel: '승인' | '반려' | '결재대기';
  regDt: string | null;    // yyyyMMddHHmmss, 결재대기 항목은 null
  reason: string | null;   // 반려(반송) 사유, 반려 건이 아니면 null
}

// ─── 업무지시서(MY) 목록 조회 ──────────────────────────────────────
export interface WorksMyListItem {
  workOrderNo: string;             // 예: WO-2025-006
  title: string;                   // 제목
  department: WorksMyDepartment;   // 부서
  part: string;                    // 파트명
  approvalStatus: WorksMyApprovalStatus; // 결재
  status: WorksMyStatus;            // 상태
  dueDt: string;                    // 마감일 YYYYMMDD
  approvalHistory: WorksMyApprovalHistoryItem[]; // 결재이력 (승인/반려 처리자 + 시각)
  returnedToMe: boolean;            // 반송되어 로그인 사용자가 다시 처리해야 하는 건인지
  hasUnreadDiscussion: boolean;     // 아직 읽지 않은 협의(신규 등록/댓글)가 있는지 - 협의 탭 new! 배지 판단용
}

// ─── 다음 단계 담당자 후보 ──────────────────────────────────────────
export interface WorksMyCandidate {
  sabun: string;   // 사번
  name: string;    // 이름
  roleNm: string;  // 역할 표시명 (예: KDN 부장)
}

export interface WorksMyNextCandidatesResponse {
  currentActId: string;         // 현재 대기 중인 단계 코드 (예: "109")
  candidates: WorksMyCandidate[];
}

// ─── 반송 대상 이전 결재 단계 후보 (직전 단계부터 오래된 순) ───────────────
export interface WorksMyReturnTarget {
  actId: string;           // 단계 코드 (예: "107")
  actIdNm: string;         // 단계명 (예: 지시서 접수)
  sabun: string | null;    // 반송 시 재배정될 처리자 사번
  name: string | null;     // 반송 시 재배정될 처리자 이름
}

export interface WorksMyReturnTargetsResponse {
  currentActId: string;
  targets: WorksMyReturnTarget[];
}

// ─── 업무지시서 등록 ────────────────────────────────────────────────
// code/label 쌍 (SERVICE_TYPE/WORK_TYPE/WORK_GUBUN은 실제 코드표 확인 전 임시 코드)
export interface WorksMyCodeOption {
  code: string;
  label: string;
}

// 단위시스템 옵션 (its_system_info 기반) - 선택 시 업무분야/DRS영향여부를 함께 자동 완성하는 데 쓰인다
export interface WorksMyUnitSystemOption {
  code: string;          // SYSTEM_CD
  label: string;         // CMS_NAME (단위시스템명)
  businessField: string; // 업무분야 - DOMAIN_NAME + "-" + SUBDOMAIN_NAME
  drsImptYn: string;     // DRS영향여부 - Y(영향 있음)/N(영향 없음), drsImptOptions와 동일한 코드체계
}

export interface WorksMyCreateOptions {
  serviceTypeOptions: WorksMyCodeOption[];
  workTypeOptions: WorksMyCodeOption[];
  workGubunOptions: WorksMyCodeOption[];
  workLevelOptions: WorksMyCodeOption[];
  drsImptOptions: WorksMyCodeOption[];
  unitSystemOptions: WorksMyUnitSystemOption[];
}

export interface WorksMyCreateRequest {
  workOrderNo: string; // 등록 다이얼로그를 열 때 미리 예약해둔 등록번호(INST_ID)
  changeTitle: string;
  changeReason: string;
  systemCd: string; // 단위시스템 코드 (its_system_info.SYSTEM_CD)
  serviceType: string;
  workType: string;
  workGubun: string;
  workLevel: string;
  workPeriod: string;
  workDuration: string; // 작업기간 (일) - 처리기간과 별개
  expectedFinishedDt: string; // yyyy-MM-dd
  drsImptYn: string; // DRS(재해복구시스템) 영향 여부 - Y/N
  isSecret: string; // 개인정보 포함 여부 - Y/N
  oppbClYn: string; // 공개구분 - Y(대외 게시용)/N(기타), isSecret=Y일 때만 유효
  userSecretContent: string; // 개인정보 포함 항목, isSecret=Y일 때만 유효
  attachExpireDate: string; // 파기일 (yyyy-MM-dd), isSecret=Y일 때만 유효
  initialApproverSabun: string;
  initialApproverName: string;
  sourceRequestNo?: string; // 작업 요청서(RealWorkReportVo) 기반 작성 시 그 요청서 번호. 일반 등록이면 빈 문자열.
}

// 작업 요청서(MY) 상세에서 "작업지시서 작성" 클릭 시 작업지시서(MY) 등록 다이얼로그로 넘기는 사전 입력값.
// works_request_my/WorksRequestMyMain.tsx가 navigate(state)로 전달하고, works_my/WorksMyMain.tsx가 받는다.
// 주의: hopeFinishedDt(요청서의 희망완료일)는 지시서의 처리예정일(expectedFinishedDt)과 서로 다른 값이다 -
// 처리예정일에 자동으로 채워 넣지 말고, 참고용으로 별도 표시만 한다.
export interface WorksMyCreateRequestPrefill {
  sourceRequestNo: string;
  changeTitle: string;
  changeReason: string;
  systemCd: string;
  serviceType: string;
  hopeFinishedDt: string; // 요청서 희망완료일 yyyy-MM-dd, 참고 표시 전용
}

// ─── 작업 요청서 등록 (요청서 작성(100) 완료 + 요청서 승인(102) 결재 대기 생성) ────────
// 102(요청서 승인)/103(요청서 접수) 승인·반송 화면과 103 접수 후 지시서 자동 생성 전환은
// 별도 작업이며, 이 요청은 등록(생성)까지만 처리한다.
export interface WorksMyCreateWorkRequestReq {
  workRequestNo: string; // 등록 다이얼로그를 열 때 미리 예약해둔 요청번호(INST_ID)
  changeTitle: string;   // 제목
  chgRsnCtt: string;     // 목적 및 근거
  changeReason: string;  // 요청사항
  serviceType: string;   // 서비스유형 코드
  systemCd: string;      // 단위시스템 코드 (its_system_info.SYSTEM_CD, 업무분야 파생용)
  expectedDt: string;    // 희망완료일 (yyyy-MM-dd)
  isSecret: string; // 개인정보 포함 여부 - Y/N
  oppbClYn: string; // 공개구분 - Y(대외 게시용)/N(기타), isSecret=Y일 때만 유효
  userSecretContent: string; // 개인정보 포함 항목, isSecret=Y일 때만 유효
  attachExpireDate: string; // 파기일 (yyyy-MM-dd), isSecret=Y일 때만 유효
  initialApproverSabun: string; // 요청서 승인(102) 결재자 사번
  initialApproverName: string;  // 요청서 승인(102) 결재자 이름
}

// ─── 업무지시서 수정 (104/106 단계, 한전 담당자 전용) ────────────────────
export interface WorksMyUpdateRequest {
  changeTitle: string;
  changeReason: string;
  systemCd: string; // 단위시스템 코드 (its_system_info.SYSTEM_CD)
  serviceType: string;
  workType: string;
  workGubun: string;
  workLevel: string;
  workPeriod: string;
  workDuration: string; // 작업기간 (일) - 처리기간과 별개
  expectedFinishedDt: string; // yyyy-MM-dd
  drsImptYn: string;
  isSecret: string; // 개인정보 포함 여부 - Y/N
  oppbClYn: string; // 공개구분 - Y(대외 게시용)/N(기타), isSecret=Y일 때만 유효
  userSecretContent: string; // 개인정보 포함 항목, isSecret=Y일 때만 유효
  attachExpireDate: string; // 파기일 (yyyy-MM-dd), isSecret=Y일 때만 유효
  removeAttachSeqs: string[]; // 삭제할 기존 첨부파일 순번
}

// 등록 폼에서 선택한 첨부파일 (서버 전송 전 로컬 상태)
export interface WorksMyAttachmentDraft {
  name: string;
  size: number;
  file: File;
}

// ─── 업무지시서 상세 (등록 정보 + 첨부파일 + 작업결과/조치사항) ─────────
export interface WorksMyAttachmentItem {
  seq: string;
  realFileName: string;
  fileSize: string;
  regDt: string;
}

export interface WorksMyWorkResult {
  result: string;      // 조치사항 내용
  workerName: string;  // 작성자(작업자)
  regDt: string;       // 등록일 yyyyMMddHHmmss
  attachments: WorksMyAttachmentItem[];
  isSecret: string | null;          // 조치사항 개인정보 포함 여부 - Y/N
  isSecretLabel: string | null;     // 조치사항 개인정보 포함 여부 - "포함"/"미포함"
  oppbClYn: string | null;          // 공개구분 - Y(대외 게시용)/N(기타)
  userSecretContent: string | null; // 개인정보 포함 항목
  attachExpireDate: string | null;  // 파기일 (yyyy-MM-dd)
}

// 조치사항 작성 시 함께 전송하는 개인정보 포함 여부 정보
export interface WorksMySecretInfo {
  isSecret: string;          // 개인정보 포함 여부 - Y/N
  oppbClYn: string;          // 공개구분 - Y(대외 게시용)/N(기타), isSecret=Y일 때만 유효
  userSecretContent: string; // 개인정보 포함 항목, isSecret=Y일 때만 유효
  attachExpireDate: string;  // 파기일 (yyyy-MM-dd), isSecret=Y일 때만 유효
}

export interface WorksMyDetail {
  workOrderNo: string;
  sourceRequestNo: string | null; // 요청서 기반 전환 지시서인 경우 원본 요청서 번호, 아니면 null
  regUserDepNm: string | null;    // IT담당자(지시서 최초 등록자)의 소속
  requesterDepNm: string | null;  // 요청자(원본 작업 요청서 작성자)의 소속. 요청서 기반이 아니면 null
  requesterName: string | null;   // 요청자 이름. 요청서 기반이 아니면 null
  requesterSabun: string | null;  // 요청자 사번. 요청서 기반이 아니면 null
  requesterTel: string | null;    // 요청자 전화번호(유선 우선, 없으면 휴대폰). 요청서 기반이 아니면 null
  changeTitle: string;
  changeReason: string;
  systemCd: string | null;      // 단위시스템 코드 (수정 폼 프리필용, its_system_info.SYSTEM_CD)
  systemCdLabel: string | null; // 단위시스템명 (CMS_NAME)
  businessField: string | null; // 업무분야 - DOMAIN_NAME + "-" + SUBDOMAIN_NAME
  serviceType: string | null;      // 서비스유형 코드 (수정 폼 프리필용)
  serviceTypeLabel: string | null;
  workType: string | null;         // 작업유형 코드 (수정 폼 프리필용)
  workTypeLabel: string | null;
  workGubun: string | null;        // 작업구분 코드 (수정 폼 프리필용)
  workGubunLabel: string | null;
  workLevel: string | null;
  workPeriod: string | null;
  workDuration: string | null; // 작업기간 (일) - 처리기간과 별개
  expectedFinishedDt: string | null; // yyyy-MM-dd
  hopeFinishedDt: string | null; // 희망완료일 (읽기 전용, its_real_work_report.EXPECTED_DT 조인)
  targetDepNm: string | null;
  drsImptYn: string | null;    // DRS영향 코드 (수정 폼 프리필용)
  drsImptLabel: string | null; // DRS영향 - "영향 있음"/"영향 없음"
  isSecret: string | null;      // 개인정보 포함 여부 - Y/N (수정 폼 프리필용)
  isSecretLabel: string | null; // 개인정보 포함 여부 - "포함"/"미포함"
  oppbClYn: string | null;          // 공개구분 - Y(대외 게시용)/N(기타) (수정 폼 프리필용)
  userSecretContent: string | null; // 개인정보 포함 항목
  attachExpireDate: string | null;  // 파기일 (yyyy-MM-dd)
  attachments: WorksMyAttachmentItem[];
  currentActId: string | null; // 완료 건은 null
  myTurn: boolean;             // 로그인 사용자가 현재 결재 대기자인지
  canEdit: boolean;            // 작업지시서를 수정할 수 있는지 (104/106 단계의 현재 결재자, 한전 담당자만 해당)
  canDelete: boolean;          // 작업지시서를 삭제할 수 있는지 (등록자 본인만, 진행 단계 무관)
  canExtendPeriod: boolean;    // 처리기간을 연장할 수 있는지 (109단계 결재대기 중 + 등록자 본인만 해당)
  workResult: WorksMyWorkResult | null;
  approvalHistory: WorksMyApprovalHistoryItem[]; // 결재이력 (지금까지 거쳐간 승인/반려 처리자 + 시각)
}
