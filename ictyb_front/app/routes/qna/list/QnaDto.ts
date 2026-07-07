// Q&A (공지사항 테이블 기반) 타입
export interface QnaItem {
  noticeNo: number;
  noticeTitle: string;
  noticeDepCd: string;
  noticeContents: string;
  priority: number;
  regUserSabun: string;
  regUserDepCd: string;
  regUserName: string;
  regDt: string;
  endDt: string;
  delYn: string;
  viewCnt: number;
  noticeType: string; // 'Q' = Q&A
}

// ─── Q&A List 조회 ────────────────────────────────────────────────────
export interface QnaListItem {
  noticeNo: number;
  noticeTitle: string;
  regUserName: string;
  regDt: string;
  viewCnt: number;
  attachCount: number;
}


// ─── Q&A 등록 폼 ─────────────────────────────────────────────────
export interface QnaRegisterForm {
  noticeTitle: string;
  noticeContents: string;
  noticeDepCd: string;
  regUserSabun: string;
  regUserDepCd: string;
  regUserName: string;
  endDt: string;
  priority: number;
}


// ─── Q&A 상세 ────────────────────────────────────────────────────
export interface QnaAttach {
  noticeNo: string;
  seq: string;
  realFileName: string;
  fileName: string;
  fileLocation: string;
  regDt: string;
  attachType: string;
  fileSize: string;
}

export interface QnaDetail {
  noticeNo: number;
  noticeTitle: string;
  noticeDepCd: string;
  noticeContents: string;
  priority: number;
  regUserSabun: string;
  regUserDepCd: string;
  regUserName: string;
  regDt: string;
  endDt: string;
  delYn: string;
  viewCnt: number;
  noticeType: string;
  attachList: QnaAttach[];
}


export interface QnaDetail extends QnaItem {
  attachList: QnaAttach[];
}

