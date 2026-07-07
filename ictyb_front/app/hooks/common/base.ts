// ─── 백엔드 Enum 매핑 ───────────────────────────────────────────
export type StatusEnum = 'SUCCESS' | 'ERROR' | 'FAIL';

export type ResultCodeEnum =
  | 200   // SUCCESS
  | 201   // SUCCESS_CREATE
  | 204   // SUCCESS_DELETE
  | 400   // BAD_REQUEST
  | 401   // UNAUTHORIZED
  | 403   // FORBIDDEN
  | 404   // NOT_FOUND
  | 405   // METHOD_NOT_ALLOWED
  | 406   // RUNTIME_ERROR
  | 409   // CONFLICT
  | 500   // INTERNAL_SERVER_ERROR
  | 503;  // SERVICE_UNAVAILABLE

// ─── 공통 BaseResponse 타입 (백엔드 구조와 1:1 매핑) ────────────
export interface BaseResponse<T = null> {
  status: StatusEnum;        // StatusEnum.SUCCESS | ERROR | FAIL
  resultCode: ResultCodeEnum; // ResultCodeEnum.code 값 (200, 201, 404 ...)
  resultMsg: string;         // 응답 메시지
  data: T;                   // 실제 데이터
}