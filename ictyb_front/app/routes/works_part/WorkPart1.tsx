import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ClipboardList, CircleDollarSign, Zap, Wrench, UserRound } from "lucide-react";

type DeptKey = "전체" | "영업" | "배전" | "기술" | "마이";

const DEPT_TABS: { key: DeptKey; label: string; icon: React.ElementType }[] = [
  { key: "전체", label: "전체", icon: ClipboardList },
  { key: "영업", label: "영업", icon: CircleDollarSign },
  { key: "배전", label: "배전", icon: Zap },
  { key: "기술", label: "기술", icon: Wrench },
  { key: "마이", label: "마이", icon: UserRound },
];

function SummaryCard({ icon: Icon, value, label, highlight }: any) {
  return (
    <Card className="flex-1 min-w-[30px]">
      <CardContent className="flex items-center gap-3 px-4 py-0 h-[50px]">
        <Icon className="shrink-0" size={26} style={{ color: highlight ? "#ef4444" : "#4A7AAA" }} />
        <div>
          <p className="text-xl font-bold leading-tight" style={{ color: highlight ? "#ef4444" : "#1C2D4F" }}>
            {value}
          </p>
          <p className="text-[11px] text-muted-foreground whitespace-pre-line leading-tight mt-0.5">{label}</p>
        </div>
      </CardContent>
    </Card>
  );
}

export function WorkPart1({ selectedYear, setSelectedYear, deptTab, setDeptTab, stats }: any) {
  return (
    <>
      <div className="flex items-center justify-between flex-wrap gap-3">
        <div className="flex items-center gap-1.5 bg-white border border-border rounded-lg p-1 shadow-sm">
          {[2026, 2025, 2024].map((year) => (
            <button
              key={year}
              onClick={() => setSelectedYear(year)}
              className="px-4 py-1.5 rounded-md text-sm font-semibold transition-colors"
              style={{
                backgroundColor: selectedYear === year ? "#1C2D4F" : "transparent",
                color: selectedYear === year ? "#fff" : "#64748b",
              }}
            >
              {year}년
            </button>
          ))}
        </div>

        <div className="flex items-center gap-1.5 bg-white border border-border rounded-lg p-1 shadow-sm">
          {DEPT_TABS.map((tab) => {
            const Icon = tab.icon;
            const active = deptTab === tab.key;
            return (
              <button
                key={tab.key}
                onClick={() => setDeptTab(tab.key)}
                className="flex items-center gap-1.5 px-3 py-1.5 rounded-md text-sm font-semibold transition-colors"
                style={{
                  backgroundColor: active ? "#1C2D4F" : "transparent",
                  color: active ? "#fff" : "#64748b",
                }}
              >
                <Icon size={14} />
                {tab.label}
              </button>
            );
          })}
        </div>
      </div>

      <Card className="border border-border/60 shadow-md">
        <CardHeader className="pb-1 pt-1 px-5">
          <CardTitle className="text-sm font-semibold flex items-center gap-1.5">
            <span>📋</span> 작업지시서 현황
          </CardTitle>
        </CardHeader>
        <CardContent className="px-4 pb-1">
          <div className="flex gap-3 flex-wrap">
            {stats.map((s: any, i: number) => (
              <SummaryCard key={i} {...s} />
            ))}
          </div>
        </CardContent>
      </Card>
    </>
  );
}