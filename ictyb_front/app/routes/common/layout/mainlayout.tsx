// components/layout/MainLayout.tsx
import React from 'react';
import { Outlet, useLocation} from 'react-router';
import Sidebar from './sidebar';
import Header from './header';
import Footer from './footer';
import ProtectedRoute from '@routes/common/jwt/ProtectedRoute';



const MainLayout: React.FC = () => {
  const location = useLocation();

  //ProtectedRoute 를 통한 로그인 여부 및 token 여부 확인 
  return (
    <ProtectedRoute> 
      <div className="flex min-h-screen bg-[#F0F3F8] text-[#1C2D4F] text-base">
        <Sidebar />
        <main className="flex-1 flex flex-col pt-0 p-10 overflow-auto">
          <Header />
          
          {/* ✨ 해결 포인트 1: shrink-0 을 추가하여 내부 차트가 쪼그라들어 짤리는 것을 방지 */}
          <div className="flex-1 shrink-0">
            <Outlet />
          </div>

          {/* ✨ 해결 포인트 2: 푸터 역시 공간을 확보하도록 shrink-0 적용 및 간격(mt-10) 추가 */}
          <div className="shrink-0 mt-10">
            <Footer />
          </div>
        </main>
      </div>
    </ProtectedRoute>
  );
};

export default MainLayout;