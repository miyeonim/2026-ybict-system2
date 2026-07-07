//로그인한 사용자 정보
export interface JwtUserDto {
  userEmpno: string;
  empNm: string;
  depId: string;
  parDepId: string;
  depTitle: string;
  kepcoMap: string;
}

export interface LoginResponse {
  success: boolean;
  accessToken : string | null;
  refreshToken : string | null;
  message: string;
  user: JwtUserDto | null;
}