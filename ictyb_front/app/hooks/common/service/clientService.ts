import axios, { type AxiosResponse } from 'axios';
import type { BaseResponse } from '@hooks/common/base'; // 하이니스님의 실제 base 경로에 맞게 지정하소서.

const clientService = axios.create({
  baseURL: 'http://localhost:8082',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
  // JWT 쿠키 기반 인증이 모든 통신에서 
  // 자동으로 백엔드로 전달되도록 쿠키 공유 옵션을 기본값으로 설정하옵니다.
  withCredentials: true, 
});



// ─── 응답 인터셉터: 모든 API에 공통 적용되는 BaseResponse 처리 ───────────────────────
clientService.interceptors.response.use(
  (response: AxiosResponse<BaseResponse<any>>) => {
    const contentType = String(response.headers['content-type'] ?? '');
 
    // 파일 다운로드(blob) 등 JSON 형식이 아닌 응답은 BaseResponse 구조 파싱을 건너뜁니다.
    if (!contentType.includes('application/json')) {
      return response;
    }
 
    const baseRes = response.data;
 
    // 백엔드 StatusEnum 기준: SUCCESS 이외는 모두 자바스크립트 오류(Reject)로 판단
    if (baseRes.status !== 'SUCCESS') {
      return Promise.reject(
        new Error(baseRes.resultMsg || '서버 오류가 발생했습니다.')
      );
    }
 
    return response;
  },
  (error) => {
    // 🌟 여기서 401(인증 만료) 등을 전역적으로 감지하여 추가 처리를 보완할 수도 있사옵니다.
    if (error.response?.status === 401) {
      console.warn('인증이 만료되었거나 토큰이 존재하지 않사옵니다.');
    }

    // HTTP 에러(4xx, 5xx) 또는 네트워크 단절 처리
    const message =
      error.response?.data?.resultMsg ||   // 백엔드 반환 에러 메시지 우선
      error.message ||
      '네트워크 오류가 발생했습니다.';
    return Promise.reject(new Error(message));
  }
);

export default clientService;