import type { BaseResponse } from '../common/base';
import type { QnaListItem, QnaDetail, QnaRegisterForm } from '@routes/qna/list/QnaDto';
import apiClient from '@hooks/common/service/clientService';


/**
 * Q&A 목록을 전체 조회합니다.
 * @returns {Promise<QnaListItem[]>} Q&A 목록 배열
 */
export const fetchQnaList = async (): Promise<QnaListItem[]> => {
  const response = await apiClient.get<BaseResponse<QnaListItem[]>>('/api/qna/list');

  console.log('[QNA] 전체 응답:', response);
  console.log('[QNA] BaseResponse:', response.data);
  console.log('[QNA] status:', response.data.status);
  console.log('[QNA] resultCode:', response.data.resultCode);
  console.log('[QNA] data(목록):', response.data.data);
  console.log('[QNA] 목록 개수:', response.data.data?.length);

  return response.data.data;
};


/**
 * 새로운 Q&A 게시글을 등록합니다. (첨부파일 포함)
 * @param {QnaRegisterForm} form - 게시글 본문 폼 데이터
 * @param {File[]} files - 첨부파일 배열
 * @returns {Promise<BaseResponse<null>>} 백엔드 공통 응답 객체
 */
export const registerQna = async (
  form: QnaRegisterForm,
  files: File[]
): Promise<BaseResponse<null>> => {
  const formData = new FormData();

  // JSON 파트
  formData.append(
    'qnaData',
    new Blob([JSON.stringify(form)], { type: 'application/json' })
  );

  // 파일 파트
  files.forEach((file) => {
    formData.append('files', file);
  });

  const response = await apiClient.post<BaseResponse<null>>('/api/qna/register', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return response.data;
};


/**
 * Q&A 게시글을 단건 상세 조회합니다.
 * @param {number} noticeNo - 게시글 고유 번호
 * @returns {Promise<QnaDetail>} Q&A 상세 데이터
 */
export const fetchQnaDetail = async (noticeNo: number): Promise<QnaDetail> => {
  const response = await apiClient.get<BaseResponse<QnaDetail>>(`/api/qna/${noticeNo}`);
  return response.data.data;
};


/**
 * 첨부파일을 브라우저를 통해 즉시 다운로드합니다.
 * @param {string} noticeNo - 게시글 번호
 * @param {string} seq - 파일 일련번호
 * @param {string} realFileName - 저장될 실제 파일명
 */
export const downloadAttach = (noticeNo: string, seq: string, realFileName: string) => {
  const url = `http://localhost:8082/api/qna/attach/download?noticeNo=${noticeNo}&seq=${seq}`;
  const link = document.createElement('a');
  link.href = url;
  link.download = realFileName;
  link.click();
};

export default apiClient;


/**
 * Q&A 게시글을 삭제합니다.
 * @param {number} noticeNo - 삭제할 게시글 고유 번호
 * @returns {Promise<BaseResponse<null>>} 백엔드 공통 응답 객체
 */
export const deleteQna = async (noticeNo: number): Promise<BaseResponse<null>> => {
  // 백엔드 API 경로가 '/api/qna/delete' 또는 '/api/qna/{noticeNo}' 등일 것이오니
  // 하이니스님의 실제 백엔드 API 설계에 맞게 URL을 조정하소서.
  const response = await apiClient.delete<BaseResponse<null>>(`/api/qna/${noticeNo}`);
  return response.data;
};