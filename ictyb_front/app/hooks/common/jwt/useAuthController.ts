import { useState } from "react";
import { useNavigate, useLocation } from "react-router";
import { useAuthContext } from "@routes/common/jwt/AuthContext"
import type { LoginResponse } from "@hooks/common/jwt/useauthDto"


const API_BASE = "http://localhost:8082";

export function useAuth() {
  const [loading, setLoading] = useState<boolean>(false);
  const { setUser } = useAuthContext();
  const navigate = useNavigate();
  const location = useLocation();

  const login = async (
    userEmpno: string,
    password: string
  ): Promise<LoginResponse> => {
    setLoading(true);
    try {
      const loginRequest = {
        userEmpno,
        password,
        // refreshToken이 있으면 서버에서 세션 연장 여부 판단에 활용
        refreshToken: localStorage.getItem("refreshToken") || null,
      };

      const response = await fetch(`${API_BASE}/api/auth/v1.0/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include", // HttpOnly 쿠키 자동 수신
        body: JSON.stringify(loginRequest),
      });

      if (!response.ok) {
        return {
          success: false,
          accessToken: null,
          refreshToken: null,
          message: "서버 오류가 발생하였습니다.",
          user: null,
        };
      }

      const result: LoginResponse = await response.json();

      if (result.success && result.user) {
        // accessToken은 HttpOnly 쿠키로 자동 저장 → JS에서 저장 불필요
        // refreshToken만 localStorage 보관
        if (result.refreshToken) {
          localStorage.setItem("refreshToken", result.refreshToken);
        }

        setUser(result.user);

        // 🌟 로그인 전 요청했던 경로로 복귀 (없으면 메인으로)
        const from =
          (location.state as { from?: { pathname: string } })?.from?.pathname ||
          "/report_total";
        navigate(from, { replace: true });
      }

      return result;
    } catch (err) {
      console.error("로그인 중 오류 발생:", err);
      return {
        success: false,
        accessToken: null,
        refreshToken: null,
        message: "서버 연결에 실패하였사옵니다.",
        user: null,
      };
    } finally {
      setLoading(false);
    }
  };

const logout = async () => {

  console.log("로그아웃 요청");

  // 🌟 로그인 시도 전 기존 상태 초기화
  setUser(null);
  localStorage.removeItem("refreshToken");

  try {
    await fetch(`${API_BASE}/api/auth/v1.0/logout`, {
      method: "POST",
      credentials: "include",
    });
  } catch (err) {
    console.error("로그아웃 요청 실패:", err);
  }

  setUser(null);
  localStorage.removeItem("refreshToken");
};

  return { login, logout, loading };
}