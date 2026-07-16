import type { BaseResponse } from '@hooks/common/base';
import type {
  DiscussionItem,
  CommentItem,
  CreateDiscussionReq,
  CreateCommentReq,
} from './type';
import apiClient from '@hooks/common/service/clientService';

/** 지시번호 기준 협의 목록 + 댓글 조회 */
export const fetchDiscussions = async (instrNo: string): Promise<DiscussionItem[]> => {
  const res = await apiClient.get<BaseResponse<DiscussionItem[]>>(
    `/api/work_opinion/v1.0/list/${instrNo}`,
  );
  return res.data.data ?? [];
};

/** 새 협의 스레드 생성 (첨부파일 포함) */
export const createDiscussion = async (
  req: CreateDiscussionReq,
  files: File[] = [],
): Promise<DiscussionItem> => {
  const formData = new FormData();
  formData.append('discussionData', new Blob([JSON.stringify(req)], { type: 'application/json' }));
  files.forEach((f) => formData.append('files', f));

  const res = await apiClient.post<BaseResponse<DiscussionItem>>(
    '/api/work_opinion/v1.0',
    formData,
    { headers: { 'Content-Type': 'multipart/form-data' } },
  );
  return res.data.data;
};

/** 댓글 등록 (첨부파일 포함) */
export const addComment = async (
  req: CreateCommentReq,
  files: File[] = [],
): Promise<CommentItem> => {
  const formData = new FormData();
  formData.append('commentData', new Blob([JSON.stringify(req)], { type: 'application/json' }));
  files.forEach((f) => formData.append('files', f));

  const res = await apiClient.post<BaseResponse<CommentItem>>(
    '/api/work_opinion/v1.0/comment',
    formData,
    { headers: { 'Content-Type': 'multipart/form-data' } },
  );
  return res.data.data;
};

/** 지시번호에 연결된 모든 협의 스레드를 로그인 사용자 기준으로 일괄 읽음 처리 */
export const markDiscussionsRead = async (instrNo: string): Promise<void> => {
  await apiClient.post<BaseResponse<void>>(`/api/work_opinion/v1.0/${instrNo}/read`);
};

/** 댓글 첨부파일을 브라우저를 통해 즉시 다운로드합니다. */
export const downloadCommentAttach = (cmntId: string, seqNo: number, realFileName: string) => {
  const url = `http://localhost:8082/api/work_opinion/v1.0/attach/download?cmntId=${cmntId}&seqNo=${seqNo}`;
  const link = document.createElement('a');
  link.href = url;
  link.download = realFileName;
  link.click();
};
