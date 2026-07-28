"use client";

import DeptCompletionChart from "./DeptCompletionChart";
import MonthlyRegistrationChart from "./MonthlyRegistrationChart";
import RankChart from "./RankChart";
import AlertPanel from "./AlertPanel";
import { COLOR } from "./types";
import { useAuthContext } from "@routes/common/jwt/AuthContext";

// ─── 메인 레이아웃 ────────────────────────────────────────────────
// 각 차트가 자기 state/API를 직접 관리하므로
// ReportTotalMain은 레이아웃만 담당합니다.
export default function ReportTotalMain() {
  const { user } = useAuthContext();
  const isAllowed = !!user?.deptHead;

  if (!isAllowed) {
    return (
      <div
        className="min-h-screen flex items-center justify-center"
        style={{ background: COLOR.pageBg, fontFamily: "'Geist Variable', sans-serif" }}
      >
        <p className="text-lg font-medium text-gray-500">접근 권한이 없습니다.</p>
      </div>
    );
  }

  return (
    <div
      className="min-h-screen p-4 space-y-2.5 pb-10"
      style={{ background: COLOR.pageBg, fontFamily: "'Geist Variable', sans-serif" }}
    >
      {/* 1. 부서·파트별 완료율 — 연도 state를 내부에서 관리 */}
      <DeptCompletionChart />

      {/* 2. 월별 작업지시서 등록 현황 — 카테고리 state를 내부에서 관리 */}
      <MonthlyRegistrationChart />

      {/* 3. 하단 2열 */}
      <div className="flex flex-row gap-10 items-stretch w-full">
        {/* <div style={{ flex: '1 1 50%' }} className="flex flex-col h-full"> */}
        <div className="w-1/2">
          <RankChart />
        </div>
        {/* <div style={{ flex: '1 1 50%' }} className="flex flex-col h-full"> */}
        <div className="w-1/2">
          <AlertPanel />
        </div>
      </div>
    </div>
  );
}
