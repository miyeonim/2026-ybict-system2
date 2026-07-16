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

export interface WorksMyCreateOptions {
  serviceTypeOptions: WorksMyCodeOption[];
  workTypeOptions: WorksMyCodeOption[];
  workGubunOptions: WorksMyCodeOption[];
  workLevelOptions: WorksMyCodeOption[];
  drsImptOptions: WorksMyCodeOption[];
}

export interface WorksMyCreateRequest {
  workOrderNo: string; // 등록 다이얼로그를 열 때 미리 예약해둔 등록번호(INST_ID)
  changeTitle: string;
  changeReason: string;
  serviceType: string;
  workType: string;
  workGubun: string;
  workLevel: string;
  workPeriod: string;
  expectedFinishedDt: string; // yyyy-MM-dd
  drsImptYn: string; // DRS(재해복구시스템) 영향 여부 - Y/N
  initialApproverSabun: string;
  initialApproverName: string;
}

// ─── 업무지시서 수정 (104/106 단계, 한전 담당자 전용) ────────────────────
export interface WorksMyUpdateRequest {
  changeTitle: string;
  changeReason: string;
  serviceType: string;
  workType: string;
  workGubun: string;
  workLevel: string;
  workPeriod: string;
  expectedFinishedDt: string; // yyyy-MM-dd
  drsImptYn: string;
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
}

export interface WorksMyDetail {
  workOrderNo: string;
  changeTitle: string;
  changeReason: string;
  serviceType: string | null;      // 서비스유형 코드 (수정 폼 프리필용)
  serviceTypeLabel: string | null;
  workType: string | null;         // 작업유형 코드 (수정 폼 프리필용)
  workTypeLabel: string | null;
  workGubun: string | null;        // 작업구분 코드 (수정 폼 프리필용)
  workGubunLabel: string | null;
  workLevel: string | null;
  workPeriod: string | null;
  expectedFinishedDt: string | null; // yyyy-MM-dd
  targetDepNm: string | null;
  drsImptYn: string | null;    // DRS영향 코드 (수정 폼 프리필용)
  drsImptLabel: string | null; // DRS영향 - "영향 있음"/"영향 없음"
  attachments: WorksMyAttachmentItem[];
  currentActId: string | null; // 완료 건은 null
  myTurn: boolean;             // 로그인 사용자가 현재 결재 대기자인지
  canEdit: boolean;            // 작업지시서를 수정할 수 있는지 (104/106 단계의 현재 결재자, 한전 담당자만 해당)
  workResult: WorksMyWorkResult | null;
  approvalHistory: WorksMyApprovalHistoryItem[]; // 결재이력 (지금까지 거쳐간 승인/반려 처리자 + 시각)
}
