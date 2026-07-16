import axios, { type AxiosResponse, type AxiosError, type InternalAxiosRequestConfig } from 'axios';
import type { BaseResponse } from '@hooks/common/base';

const API_BASE = import.meta.env.VITE_API_BASE_URL;

const clientService = axios.create({
  baseURL: API_BASE,
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
});

// refresh 요청 자체는 별도 axios 인스턴스로 (clientService로 호출하면 이 인터셉터를 다시 타서 무한루프 위험)
const rawAxios = axios.create({ baseURL: API_BASE, withCredentials: true });

// 동시에 여러 API가 401을 맞아도 refresh는 딱 1번만 실행되도록 공유
let refreshPromise: Promise<boolean> | null = null;

async function refreshAccessToken(): Promise<boolean> {
  if (refreshPromise) return refreshPromise; // 이미 refresh 진행 중이면 그 결과를 같이 기다림

  refreshPromise = (async () => {
    const rt = localStorage.getItem('refreshToken');
    if (!rt) return false;

    try {
      const res = await rawAxios.post('/api/auth/v1.0/refresh', { refreshToken: rt });
      const data = res.data;
      if (data.success) {
        if (data.refreshToken) {
          localStorage.setItem('refreshToken', data.refreshToken); // rotation된 새 RT로 교체
        }
        return true; // 새 access token은 서버가 쿠키로 이미 심어줬음
      }
      return false;
    } catch {
      return false;
    }
  })();

  const result = await refreshPromise;
  refreshPromise = null;
  return result;
}

// ─── 응답 인터셉터 ───────────────────────
clientService.interceptors.response.use(
  (response: AxiosResponse<BaseResponse<any>>) => {
    const contentType = String(response.headers['content-type'] ?? '');
    if (!contentType.includes('application/json')) return response;

    const baseRes = response.data;
    if (baseRes.status !== 'SUCCESS') {
      return Promise.reject(new Error(baseRes.resultMsg || '서버 오류가 발생했습니다.'));
    }
    return response;
  },
  
  async (error: AxiosError<BaseResponse<any>>) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    // 401이고, 아직 재시도 안 한 요청이고, refresh 요청 자체의 401은 제외
    if (
      error.response?.status === 401 &&
      !originalRequest._retry &&
      !originalRequest.url?.includes('/api/auth/v1.0/refresh')
    ) {
      originalRequest._retry = true; // 무한루프 방지 플래그

      const refreshed = await refreshAccessToken();

      if (refreshed) {
        return clientService(originalRequest); // 원래 요청 재시도
      }

      // refresh도 실패 → 완전 로그아웃 처리
      localStorage.removeItem('refreshToken');
      window.location.href = '/common/jwt';
      return Promise.reject(new Error('세션이 만료되었습니다. 다시 로그인해주세요.'));
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