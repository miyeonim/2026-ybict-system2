// app/components/common/ProtectedRoute.tsx
import { useEffect } from "react";
import { useNavigate, useLocation } from "react-router";
import { useAuthContext } from "@routes/common/jwt/AuthContext";

interface ProtectedRouteProps {
  children: React.ReactNode;
}

export default function ProtectedRoute({ children }: ProtectedRouteProps) {
  const { user, loading } = useAuthContext();
  const navigate = useNavigate();
  const location = useLocation();

  // 🔒 인증 안 됨 -> 로그인 페이지로 이동 처리 (딱 한 번만 실행)
  useEffect(() => {
    // 로딩이 끝났고 유저 정보가 없는데, 현재 서 있는 위치가 로그인 페이지가 아니라면
    console.log("로그인 정보 START ================= ");
    console.log("로그인 정보 : ", user);
    console.log("로그인 정보 END ================= ");
    if (!loading && !user && location.pathname !== "/common/jwt") {
      // 로그인 후 원래 보던 화면으로 돌아올 수 있도록 주소를 state에 담아 보냅니다.
      navigate("/common/jwt", { state: { from: location }, replace: true });
    }
  }, [user, loading, location.pathname, navigate]);

  // 1. /me 응답 대기 중 (세션 확인 중): 로딩 화면 표시로 로그인창 깜빡임 방지
  if (loading) {
    return (
      <div style={{ display: "flex", justifyContent: "center", alignItems: "center", minHeight: "100vh", backgroundColor: "#F0F3F8" }}>
        <div style={{ color: "#3A6499", fontSize: "1rem" }}>로딩 중...</div>
      </div>
    );
  }

  // 2. 인증 안 됨: 위의 useEffect가 로그인 페이지 파일들을 다 받아와서 주소를 완전히 바꿀 때까지,
  //    중복 이동 명령을 내리지 않도록 안전하게 null을 리턴하여 침묵을 지킵니다. (무한루프 격파 핵심)
  if (!user) {
    return null;
  }

  // 3. 인증 완료 (세션 유지): 정상적으로 메인 등 자식 화면 렌더링 및 프론트 유저 정보 활용
  return <>{children}</>;
}