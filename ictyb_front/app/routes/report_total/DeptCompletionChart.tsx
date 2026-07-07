"use client";

import { useState, useEffect } from "react";
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid,
  Tooltip, LabelList, Cell, ResponsiveContainer,
} from "recharts";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { COLOR, getBarColor } from "./types";
import type { DeptPart, DeptSection } from "./types";
import { cn } from "@/lib/utils";
import { fetchDeptCompletionStats } from "@hooks/report_total/DeptCompletionChartController";

const getYears = (currentYear: number) =>
  Array.from({ length: 3 }, (_, i) => currentYear - i);

function DeptAxisTick({ x, y, payload }: any) {
  return (
    <text x={x} y={y + 8} textAnchor="middle" fontSize={9} fontWeight={500} fill="#111111">
      {payload.value}
    </text>
  );
}

function PctLabel({ x, y, width, value }: any) {
  if (value == null) return null;
  return (
    <text x={x + width / 2} y={y - 4} textAnchor="middle" fontSize={9} fontWeight={600} fill={COLOR.navy}>
      {value}%
    </text>
  );
}

function DeptVBarChart({ data }: { data: DeptPart[] }) {
  return (
    <ResponsiveContainer width="100%" height={200}>
      <BarChart data={data} barCategoryGap="8%" barSize={24} margin={{ top: 18, right: 5, left: 5, bottom: 28 }}>
        <CartesianGrid vertical={false} stroke="rgba(74,122,170,0.08)" />
        <XAxis dataKey="label" tick={<DeptAxisTick />} axisLine={false} tickLine={false} interval={0} padding={{ left: 10, right: 10 }} />
        <YAxis hide domain={[0, 100]} />
        <Tooltip
          cursor={{ fill: "transparent" }}
          content={({ active, payload }) => {
            if (!active || !payload?.length) return null;
            const d = payload[0].payload as DeptPart;
            return (
              <div className="bg-white p-3 rounded-lg shadow-xl border border-slate-100 min-w-[160px] text-[11px]">
                <p className="font-bold text-slate-800 mb-2 border-b border-slate-100 pb-1.5">{d.label}</p>
                
                {/* 완료율 및 총 건수 */}
                <div className="text-slate-600 mb-2 font-medium">
                  완료율 <span className="text-slate-800 font-semibold">{d.pct}%</span> | 
                  총 <span className="text-slate-800 font-semibold">{d.total}건</span>
                </div>

                {/* 텍스트와 숫자를 바로 옆에 붙여서 정렬 */}
                <div className="flex flex-col gap-1 text-slate-600">
                  <div className="flex items-center gap-1.5">
                    <span className="w-2 h-2 rounded-sm" style={{ background: "#3A6080" }} />
                    <span>완료 <span className="font-semibold text-slate-800">{d.done}건</span></span>
                  </div>
                  
                  <div className="flex items-center gap-1.5">
                    <span className="w-2 h-2 rounded-sm" style={{ background: "#7AAAC8" }} />
                    <span>미완료 <span className="font-semibold text-slate-800">{d.pending}건</span></span>
                  </div>
                </div>
              </div>
            );
          }}
        />
        <Bar dataKey="pct" radius={[3, 3, 0, 0]} >
          <LabelList dataKey="pct" content={<PctLabel />} />
          {data.map((d) => (
            <Cell key={d.label} fill={getBarColor(d.pct)} />
          ))}
        </Bar>
      </BarChart>
    </ResponsiveContainer>
  );
}

export default function DeptCompletionChart() {
  const defaultYear = new Date().getFullYear();
  const years       = getYears(defaultYear);

  const [selectedYear, setSelectedYear] = useState(defaultYear);
  const [sections, setSections]         = useState<DeptSection[]>([]);
  const [initialLoading, setInitialLoading] = useState(true); // 최초 로딩만
  const [fetching, setFetching]             = useState(false); // 버튼 클릭 시

  const loadData = async (year: number, isInitial = false) => {
    isInitial ? setInitialLoading(true) : setFetching(true);
    try {
      const data = await fetchDeptCompletionStats(year.toString());
      setSections(data || []);
    } catch (err) {
      console.error("부서 완료율 조회 실패:", err);
    } finally {
      isInitial ? setInitialLoading(false) : setFetching(false);
    }
  };

  useEffect(() => {
    loadData(defaultYear, true);
  }, []);

  return (
    <Card className="border shadow-none" style={{ borderColor: COLOR.border, background: COLOR.white }}>
      <CardHeader className="pb-1 pt-1 px-4">
        <div className="flex items-center justify-between">
          <p 
              className="font-bold tracking-tight flex items-center gap-2" 
              style={{ 
                color: 'var(--title-h1)', 
                fontSize: 'var(--text-header-title)' 
              }}
            >
            🏆 부서 · 파트별 완료율
          </p>
          <div className="flex items-center rounded-md overflow-hidden" style={{ border: `1px solid ${COLOR.border}` }}>
            {years.map((year) => (
              <button
                key={year}
                onClick={() => { setSelectedYear(year); loadData(year, false); }}
                className={cn("px-2 py-1 text-[10px] font-medium transition-colors cursor-pointer", "border-r last:border-r-0")}
                style={{ borderColor: COLOR.border, background: year === selectedYear ? COLOR.navy : COLOR.white, color: year === selectedYear ? "#fff" : COLOR.mid }}
              >
                {year}
              </button>
            ))}
          </div>
        </div>
      </CardHeader>
      <CardContent className="px-2 pb-2">
        {/* 최초 로딩 */}
        {initialLoading ? (
          <div className="text-[12px] text-center py-10" style={{ color: COLOR.mid }}>불러오는 중...</div>
        ) : (
          // 버튼 클릭 시 차트 유지, opacity만 낮춤 → 깜빡임 없음
          <div style={{ opacity: fetching ? 0.5 : 1, transition: "opacity 0.2s" }}>
            <div className="grid grid-cols-3 gap-2">
              {sections.map((dept) => (
                <div key={dept.title} className="border rounded-lg p-2" style={{ borderColor: COLOR.border }}>
                  <div className="text-[11px] font-bold text-center pb-2 mb-2 border-b" style={{ color: COLOR.navy, borderColor: COLOR.border }}>
                    {dept.icon} {dept.title}
                  </div>
                  <DeptVBarChart data={dept.data} />
                </div>
              ))}
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
