//로그인한 사용자 정보
export interface JwtUserDto {
  userEmpno: string;
  empNm: string;
  depId: string;
  parDepId: string;
  depTitle: string;
  kepcoMap: string;
  kepcoYn: string;
  deptHead: boolean;
  partId: string | null;   // 소속 파트ID (ictyb_user_info 기준, KDN만 - 화면 표시/힌트 용도)
  partLeader: boolean;     // 파트장 여부 (KDN만 - 화면 표시/힌트 용도, 실제 인가는 서버가 재검증)
  bujan: boolean;          // 부서장(부장) 여부 (KDN만 - deptHead(처장)와는 다른 개념)
}

export interface LoginResponse {
  success: boolean;
  accessToken : string | null;
  refreshToken : string | null;
  message: string;
  user: JwtUserDto | null;
}