import React from 'react';
import { Link, useLocation } from 'react-router'; // 또는 사용 중인 라우터
import { menuItems } from "@routes/common/layout/menu";             //메뉴 타이틀 가져오기
import { useAuthContext } from "@routes/common/jwt/AuthContext";


const Sidebar: React.FC = () => {
  const location = useLocation();
  const { user } = useAuthContext();
  const visibleMenuItems = menuItems.filter((item) => {
    // kepcoOnly 메뉴는 한전 사람(user.kepcoYn === "Y")에게만 보인다. KDN 사람은 제외.
    if (item.kepcoOnly && user?.kepcoYn !== "Y") return false;
    // deptHeadOnly 메뉴는 처장(user.deptHead === true)에게만 보인다.
    if (item.deptHeadOnly && !user?.deptHead) return false;
    return true;
  });

  return (
    <aside className="w-56 bg-[#1C2D4F] text-white p-6">
      <h1 className="text-xl font-bold mb-8">
        영배 KEPCO ICT<br/>
        <span className="text-sm opacity-70">업무지시서 관리 시스템</span>
      </h1>
      <div className="space-y-2">
        {visibleMenuItems.map((item, i) => (
          <Link
            key={i}
            to={item.path}
            className={`flex items-center gap-4 p-4 rounded-lg hover:bg-white/10 cursor-pointer font-medium transition-colors ${
              location.pathname === item.path ? 'bg-white/10' : ''
            }`}
          >
            {item.label}
          </Link>
        ))}
      </div>
    </aside>
  );
};

export default Sidebar;