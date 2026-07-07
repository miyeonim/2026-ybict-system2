import { User, LogOut } from "lucide-react";
import { useLocation, useNavigate } from "react-router";
import { useAuthContext } from "@routes/common/jwt/AuthContext";    //사용자정보가져오기 
import { menuItems } from "@routes/common/layout/menu";             //메뉴 타이틀 가져오기
import { useAuth } from "@hooks/common/jwt/useAuthController";

/*
interface HeaderProps {
  title: string; 
}
*/

//export default function Header({ title }: HeaderProps) {
export default function Header() {
  const { user } = useAuthContext();
  const { logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation(); // 현재 경로 정보

  // 정확히 일치하는 메뉴가 없으면 하위 경로(/report_daily/:id 등)까지 고려해 가장 긴 prefix로 매칭
  const currentMenu =
    menuItems.find(item => item.path === location.pathname) ??
    menuItems
      .filter(item => location.pathname.startsWith(item.path + "/"))
      .sort((a, b) => b.path.length - a.path.length)[0];
  const title = currentMenu ? currentMenu.label : "대시보드"; // 기본값 설정

  const handleLogout = async () => {
    await logout();
    navigate("/common/jwt");
  };

  return (
    // 1. bg-white, shadow-sm, border, mb-6 제거
    // 2. h-16 등 고정 높이를 주어 정렬이 유지되도록 하였사옵니다.
    <header className="flex justify-between items-center w-full px-0 py-4">
      <h1 className="text-xl font-extrabold text-[#1C2D4F]">{title}</h1>

      <div className="flex items-center gap-3">
        {/* 사용자 정보 영역 */}
        <div className="flex items-center gap-3 bg-[#1C2D4F] text-white px-3 py-1.5 rounded-full text-xm font-semibold">
          <User size={18} />
          <span>{user 
                  ? ` ${user.depTitle}  ${user.empNm} (${user.userEmpno}) ` 
                  : "ICT 담당자 (KEPCO)"
                }</span>
        </div>

        {/* 로그아웃 버튼 영역 */}
        <button
          onClick={handleLogout}
          className="flex items-center gap-1.5 border border-[#DDE4EE] text-[#5A7090] px-3 py-1.5 rounded-full text-xm font-bold hover:bg-slate-100 hover:text-[#1C2D4F] transition-colors"
        >
          <LogOut size={18} /> 로그아웃
        </button>
      </div>
    </header>
  );
}