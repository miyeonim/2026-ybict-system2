import React from 'react';
import { Link, useLocation } from 'react-router'; // 또는 사용 중인 라우터
import { menuItems } from "@routes/common/layout/menu";             //메뉴 타이틀 가져오기


const Sidebar: React.FC = () => {
  const location = useLocation();

  return (
    <aside className="w-56 bg-[#1C2D4F] text-white p-6">
      <h1 className="text-xl font-bold mb-8">
        영배 KEPCO ICT<br/>
        <span className="text-sm opacity-70">업무지시서 관리 시스템</span>
      </h1>
      <div className="space-y-2">
        {menuItems.map((item, i) => (
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