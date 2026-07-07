"use client";

import { useState, useEffect } from "react";
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid,
  Tooltip, ResponsiveContainer,
} from "recharts";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { COLOR } from "./types";
import type { ChartData } from "./types";
import { fetchMonthlyRegistrationStats } from "@hooks/report_total/MonthlyChartController";

type CategoryType = "전체" | "영업" | "배전" | "기술";
const CATEGORIES: CategoryType[] = ["전체", "영업", "배전", "기술"];

// ─── Tooltip ───────────────────────────────────────────────────────
function LineTooltip({ active, payload, label, currentYear }: any) {
  if (!active || !payload?.length) return null;

  return (
    <div
      className="rounded-lg px-3 py-2 text-[11px] shadow-lg"
      style={{
        background: "#ffffff",
        border: `1px solid ${COLOR.border}`,
        minWidth: 120,
      }}
    >
      {/* 월 */}
      <p
        className="font-semibold mb-2"
        style={{
          color: "#111827",
        }}
      >
        {label}
      </p>

      {payload
        .filter((p: any) => p.value != null)
        .map((p: any) => {
          const isCurrent = p.dataKey === "currentYY";

          return (
            <div
              key={p.dataKey}
              className="flex items-center justify-between gap-4"
            >
              <div className="flex items-center gap-1.5">

                {/* RankChart 범례 느낌 */}
                <span
                  className="inline-block w-2.5 h-2.5 rounded-sm"
                  style={{
                    background: isCurrent
                      ? "#3A6080"
                      : "#7AAAC8",
                  }}
                />

                <span
                  style={{
                    color: isCurrent
                      ? "#1C2D4F"
                      : "#7AAAC8",
                    fontWeight: isCurrent ? 700 : 500,
                  }}
                >
                  {isCurrent
                    ? `${currentYear}년`
                    : `${currentYear - 1}년`}
                </span>

              </div>

              <span
                style={{
                  color: isCurrent
                    ? "#1C2D4F"
                    : "#7AAAC8",
                  fontWeight: 700,
                }}
              >
                {p.value}건
              </span>

            </div>
          );
        })}
    </div>
  );
}


export default function MonthlyRegistrationChart() {
  const currentYear = new Date().getFullYear();

  const [selectedCategory, setSelectedCategory] = useState<CategoryType>("전체");
  const [data, setData]                         = useState<ChartData[]>([]);
  const [initialLoading, setInitialLoading]     = useState(true); // 최초 로딩만
  const [fetching, setFetching]                 = useState(false); // 버튼 클릭 시

  const loadData = async (cat: CategoryType, isInitial = false) => {
    isInitial ? setInitialLoading(true) : setFetching(true);
    try {
      const rawData = await fetchMonthlyRegistrationStats(currentYear.toString(), cat);
      const formatted: ChartData[] = rawData.map((d) => ({
        month:     `${d.month}월`,
        currentYY: d.currentYY ?? null,
        prevYY:    d.prevYY ?? null,
      }));
      setData(formatted);
    } catch (err) {
      console.error("월별 데이터 조회 실패:", err);
    } finally {
      isInitial ? setInitialLoading(false) : setFetching(false);
    }
  };

  useEffect(() => {
    loadData("전체", true);
  }, []);

  const handleCategoryChange = (cat: CategoryType) => {
    setSelectedCategory(cat);
    loadData(cat, false);
  };

  return (
    <Card className="border shadow-none" style={{ borderColor: COLOR.border, background: COLOR.white }}>
      <CardHeader className="pb-0 pt-1 px-4">
        <div className="flex justify-between items-center">
          <p 
              className="font-bold tracking-tight flex items-center gap-2" 
              style={{ 
                color: 'var(--title-h1)', 
                fontSize: 'var(--text-header-title)' 
              }}
            >
            📈 월별 작업지시서 등록 현황
          </p>
          <div className="flex items-center gap-3">
            <div className="flex items-center gap-1">
              {CATEGORIES.map((cat) => {
                const isActive = selectedCategory === cat;
                return (
                  <button
                    key={cat}
                    onClick={() => handleCategoryChange(cat)}
                    className="text-[11px] px-2.5 py-0.5 rounded transition-all"
                    style={{
                      background: isActive ? COLOR.navy : "transparent",
                      color:      isActive ? "#fff"      : COLOR.mid,
                      border:     `1px solid ${isActive ? COLOR.navy : COLOR.border}`,
                      fontWeight: isActive ? 600 : 400,
                    }}
                  >
                    {cat}
                  </button>
                );
              })}
            </div>
            <span style={{ color: COLOR.border }}>|</span>
            <div className="flex items-center gap-3 text-[11px]" style={{ color: COLOR.mid }}>
              <span className="flex items-center gap-1.5">
                <span className="inline-block w-4 h-0.5 rounded" style={{ background: COLOR.navy }} />
                {currentYear}년
              </span>
              <span className="flex items-center gap-1.5">
                <span className="inline-block w-4 border-t-2 border-dashed" style={{ borderColor: COLOR.steel }} />
                {currentYear - 1}년
              </span>
            </div>
          </div>
        </div>
      </CardHeader>
      <CardContent className="px-2 pt-2 pb-3">
        {/* 최초 로딩 */}
        {initialLoading ? (
          <div className="text-[12px] text-center py-10" style={{ color: COLOR.mid }}>불러오는 중...</div>
        ) : (
          // 버튼 클릭 시 차트 유지, opacity만 낮춤 → 깜빡임 없음
          <div style={{ opacity: fetching ? 0.5 : 1, transition: "opacity 0.2s" }}>
            <ResponsiveContainer width="100%" height={220}>
              <LineChart data={data} margin={{ top: 10, right: 16, left: -28, bottom: 0 }}>
                <CartesianGrid strokeDasharray="0" stroke="rgba(74,122,170,0.08)" />
                <XAxis dataKey="month" tick={{ fontSize: 11, fill: COLOR.steel }} axisLine={false} tickLine={false} />
                <YAxis domain={[0, 10]} ticks={[0, 2, 4, 6, 8, 10]} tick={{ fontSize: 11, fill: COLOR.steel }} axisLine={false} tickLine={false} />
                <Tooltip content={
                  <LineTooltip 
                    currentYear={currentYear}
                  />} />
                <Line type="monotone" dataKey="currentYY" stroke={COLOR.navy} strokeWidth={2.5} dot={{ r: 4, fill: COLOR.navy }} activeDot={{ r: 5 }} connectNulls={false} />
                <Line type="monotone" dataKey="prevYY" stroke={COLOR.steel} strokeWidth={1.5} strokeDasharray="5 4" dot={{ r: 3.5, fill: COLOR.steel }} activeDot={{ r: 5 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
