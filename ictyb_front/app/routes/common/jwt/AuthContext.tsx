// app/contexts/AuthContext.tsx
import { createContext,  useContext,  useState,  useEffect,  useRef, useMemo,  type ReactNode, } from "react";
import type { JwtUserDto } from "@hooks/common/jwt/useauthDto";

interface AuthContextType {
  user: JwtUserDto | null;
  setUser: (user: JwtUserDto | null) => void;
  loading: boolean;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const API_BASE = "http://localhost:8082";

// /me 호출을 건너뛸 경로 목록 (로그인 페이지 등)
const PUBLIC_PATHS = ["/common/jwt"];


export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<JwtUserDto | null>(null);
  const [loading, setLoading] = useState(true);
  
  // 🔒 무한 루프 방지: /me 요청이 이미 진행 중인지 추적
  const isFetchingRef = useRef(false);
  // 🔒 컴포넌트 언마운트 후 상태 변경 방지
  const isMountedRef = useRef(true);

  useEffect(() => {
    isMountedRef.current = true;

    // 현재 경로가 PUBLIC 경로이면 /me 호출 자체를 하지 않음 (로그인 요청 화면)
    const currentPath = globalThis.location.pathname;
    //const isPublicPath = PUBLIC_PATHS.some((p) => currentPath.startsWith(p));
    const isPublicPath = PUBLIC_PATHS.some((p) => currentPath === p || currentPath.startsWith(p + '/'));

    if (isPublicPath) {
      setLoading(false);
      return;
    }

    // 이미 fetch 중이면 중복 호출 방지
    if (isFetchingRef.current) return;
    isFetchingRef.current = true;

    fetch(`${API_BASE}/api/auth/v1.0/me`, {
      credentials: "include",
      // 브라우저 캐시 방지 (토큰 만료 시 stale 응답을 받지 않도록)
      headers: { "Cache-Control": "no-cache" },
    })
      .then(async (res) => {
        // 401/403은 토큰 없음 or 만료 → 로그인 페이지로 처리 (무한 루프 없음)
        if (res.status === 401 || res.status === 403) {
          return { success: false, user: null };
        }
        // 그 외 에러 (500 등)는 네트워크 에러로 처리
        if (!res.ok) throw new Error(`서버 오류: ${res.status}`);
        return res.json();
      })
      .then((data) => {
        if (!isMountedRef.current) return;
        setUser(data.success ? data.user : null);
      })
      .catch(() => {
        if (!isMountedRef.current) return;
        setUser(null);
      })
      .finally(() => {
        isFetchingRef.current = false;
        if (isMountedRef.current) setLoading(false);
      });

    return () => {
      isMountedRef.current = false;
    };
  }, []); // ← 의존성 배열 비워둠: 마운트 시 딱 1회만 실행

//    user 또는 loading이 바뀔 때만 새 객체를 생성 → 불필요한 하위 컴포넌트 리렌더 방지
  const contextValue = useMemo<AuthContextType>(
    () => ({
      user,
      setUser,
      loading,
      isAuthenticated: !!user,
    }),
    [user, loading] // setUser는 useState가 보장하는 안정적 참조
  );

  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuthContext() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuthContext must be used within AuthProvider");
  return ctx;
}