// 업무 협의 댓글 첨부파일
export interface AttachmentItem {
  seqNo: number;
  realFileName: string;
  fileSize: number;
}

// 업무 협의 댓글
export interface CommentItem {
  cmntId: string;
  opnId: string;
  cmntCtt: string;
  wrtrEmpno: string;
  wrtrNm: string;
  wrtrRoleNm: string;
  regDt: string;
  attachments: AttachmentItem[];
}

// 업무 협의 스레드 (댓글 포함)
export interface DiscussionItem {
  opnId: string;
  instrNo: string;
  opnTitle: string;
  wrtrEmpno: string;
  wrtrNm: string;
  regDt: string;
  comments: CommentItem[];
}

// 협의 생성 요청
export interface CreateDiscussionReq {
  instrNo: string;
  opnTitle: string;
  wrtrEmpno: string;
  wrtrNm: string;
  wrtrRoleNm?: string; // 첨부파일이 있어 최초 댓글을 함께 생성할 때만 사용
}

// 댓글 등록 요청
export interface CreateCommentReq {
  opnId: string;
  cmntCtt: string;
  wrtrEmpno: string;
  wrtrNm: string;
  wrtrRoleNm: string;
}
