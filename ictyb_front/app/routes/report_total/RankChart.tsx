"use client";

import { useState, useEffect } from "react";
import { Bar, BarChart, CartesianGrid, XAxis, YAxis, LabelList } from "recharts";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { ChartContainer, ChartTooltip, ChartTooltipContent, type ChartConfig } from "@/components/ui/chart";
import { fetchPartRankStats } from "@/hooks/report_total/RankChartController";
import { COLOR } from "./types";
import type { RankItem } from "@/hooks/report_total/types";

type CategoryType = "전체" | "영업" | "배전" | "기술";
const CATEGORIES: CategoryType[] = ["전체", "영업", "배전", "기술"];

const chartConfig = {
  done:    { label: "완료",   color: "#3A6080" },
  pending: { label: "미완료", color: "#7AAAC8" },
} satisfies ChartConfig;

// ─── Custom Label ───────────────────────────────────────────────────
interface CustomLabelProps {
  readonly x: number;
  readonly y: number;
  readonly width: number;
  readonly value: number;
  readonly index: number;
  readonly chartData: RankItem[];
}

function CustomLabel({ x, y, width, value, index, chartData }: CustomLabelProps) {
  const item = chartData[index];
  return (
    <g transform={`translate(${x + width / 2}, ${y - 10})`}>
      <text textAnchor="middle" fontSize={12} fontWeight={700} fill="#1C2D4F">
        총 {value}건
      </text>
      <text textAnchor="middle" fontSize={11} dy={-16} fill="#3A6080">
        완료율: {item.pct}%
      </text>
    </g>
  );
}

// ─── Loading ────────────────────────────────────────────────────────
function LoadingCard() {
  return (
    <Card className="h-full flex items-center justify-center shadow-none border-none bg-white">
      <span className="text-sm text-gray-400">로딩 중...</span>
    </Card>
  );
}

// ─── Header ─────────────────────────────────────────────────────────
interface RankChartHeaderProps {
  readonly selectedCategory: CategoryType;
  readonly fetching: boolean;
  readonly onCategoryChange: (cat: CategoryType) => void;
}

function RankChartHeader({ selectedCategory, fetching, onCategoryChange }: RankChartHeaderProps) {
  return (
    <div className="flex justify-between items-center">
      <p 
        className="font-bold tracking-tight flex items-center gap-2" 
        style={{ 
          color: 'var(--title-h1)', 
          fontSize: 'var(--text-header-title)' 
        }}
      >
        🏅 건수 파트 순위
      </p>
      <div className="flex items-center gap-3">
        {/* 카테고리 버튼 */}
        <div className="flex items-center gap-1">
          {CATEGORIES.map((cat) => {
            const isActive = selectedCategory === cat;
            return (
              <button
                key={cat}
                onClick={() => onCategoryChange(cat)}
                disabled={fetching}
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

        {/* 범례 */}
        <div className="flex items-center gap-3 text-[11px]" style={{ color: COLOR.mid }}>
          <span className="flex items-center gap-1.5">
            <span className="inline-block w-3 h-3 rounded-sm" style={{ background: "#3A6080" }} />
            완료
          </span>
          <span className="flex items-center gap-1.5">
            <span className="inline-block w-3 h-3 rounded-sm" style={{ background: "#7AAAC8" }} />
            미완료
          </span>
        </div>
      </div>
    </div>
  );
}

// ─── Chart ──────────────────────────────────────────────────────────
interface RankBarChartProps {
  readonly chartData: RankItem[];
}

function RankBarChart({ chartData }: RankBarChartProps) {
  // pending: 0(완료율 100%)인 항목은 recharts가 해당 스택 구간을 그리지 않아 라벨도 함께 사라지므로,
  // 렌더링용 데이터에서만 미세한 값으로 보정해 라벨이 항상 표시되게 한다.
  const renderData = chartData.map((d) => (d.pending === 0 ? { ...d, pending: 0.0001 } : d));

  return (
    <ChartContainer config={chartConfig} className="h-[250px] w-full">
      <BarChart data={renderData} margin={{ top: 50, bottom: 0 }}>
        <CartesianGrid vertical={false} stroke="#E2E8F0" />
        <XAxis dataKey="name" tickLine={false} axisLine={false} tickMargin={10} />
        <YAxis hide />
        <ChartTooltip content={<ChartTooltipContent />} />

        <Bar dataKey="done"    stackId="a" fill="#3A6080" radius={[0, 0, 0, 0]} maxBarSize={45} />
        <Bar dataKey="pending" stackId="a" fill="#7AAAC8" radius={[4, 4, 0, 0]} maxBarSize={45}>
          <LabelList
            dataKey="total"
            position="top"
            offset={20}
            content={(props: any) => <CustomLabel {...props} chartData={chartData} />}
          />
        </Bar>
      </BarChart>
    </ChartContainer>
  );
}

// ─── Main ────────────────────────────────────────────────────────────
export default function RankChart() {
  const [selectedCategory, setSelectedCategory] = useState<CategoryType>("전체");
  const [chartData, setChartData]               = useState<RankItem[]>([]);
  const [initialLoading, setInitialLoading]     = useState(true);
  const [fetching, setFetching]                 = useState(false);

  const year = String(new Date().getFullYear());

  const loadData = async (cat: CategoryType, isInitial = false) => {
    isInitial ? setInitialLoading(true) : setFetching(true);
    try {
      const data = await fetchPartRankStats(year, cat);
      setChartData(data);
    } catch (err) {
      console.error("파트 랭킹 데이터 조회 실패:", err);
    } finally {
      isInitial ? setInitialLoading(false) : setFetching(false);
    }
  };

  useEffect(() => {
    loadData("전체", true);
  }, [year]);

  const handleCategoryChange = (cat: CategoryType) => {
    setSelectedCategory(cat);
    loadData(cat, false);
  };

  if (initialLoading) return <LoadingCard />;

  return (
    //"h-full flex flex-col border shadow-none" 
    <Card className="h-full flex flex-col shadow-none"
      style={{ borderColor: COLOR.border, background: COLOR.white }}>
      <CardHeader className="pb-0 pt-3 px-4 shrink-0">
        <RankChartHeader
          selectedCategory={selectedCategory}
          fetching={fetching}
          onCategoryChange={handleCategoryChange}
        />
      </CardHeader>
        <CardContent
          className="flex-1 min-h-0 pb-2 px-2 pt-2"
          style={{ opacity: fetching ? 0.5 : 1, transition: "opacity 0.2s" }}
        >
        <RankBarChart chartData={chartData} />
      </CardContent>
    </Card>
  );
}