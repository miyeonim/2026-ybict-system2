"use client";

import { useState, useRef, useEffect, useCallback } from "react";
import { getReportDetail } from "@/hooks/report_detail/ReportDetailController";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  ClipboardList,
  CheckCircle2,
  Settings2,
  GitMerge,
  Flame,
  Clock,
  CalendarDays,
  ChevronLeft,
  ChevronRight,
  CircleDollarSign, // 🌟 영업용 달러 아이콘 추가
  Zap,              // 🌟 배전용 번개 아이콘 추가
  Wrench,           // 🌟 기술용 렌치 아이콘 추가
} from "lucide-react";

// ─── 타입 ────────────────────────────────────────────────────────────────────

// 백엔드 API 응답 DTO 그대로 (string 날짜, assignee 없음)
// 컴포넌트 내부에서 사용하는 확장 타입 (Date 변환 + id 추가)
interface WorkOrder {
  id: number;         // 인덱스 기반 생성
  code: string;
  name: string;
  startDate: Date;    // string → Date 변환
  endDate: Date;      // string → Date 변환
  duration: number;
  department: string;
  status: string;     // 미완료 → 처리 중 변환
  assignee: string;   // approval 값으로 대체 표시
  approval: string;
}

// ─── 유틸 ────────────────────────────────────────────────────────────────────

function addDays(date: Date, days: number): Date {
  const d = new Date(date);
  d.setDate(d.getDate() + days);
  return d;
}

function formatDateLabel(date: Date): string {
  return `${date.getMonth() + 1}/${date.getDate()}`;
}

function formatDateFull(date: Date): string {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
}

function isSameDay(a: Date, b: Date): boolean {
  return (
    a.getFullYear() === b.getFullYear() &&
    a.getMonth() === b.getMonth() &&
    a.getDate() === b.getDate()
  );
}

// ─── API 데이터 → 내부 WorkOrder 변환 ────────────────────────────────────────

function parseLocalDate(dateStr: string): Date {
  // "yyyy-MM-dd" → Date (로컬 시간 기준, 시간대 오프셋 없음)
  const [y, m, d] = dateStr.split("-").map(Number);
  return new Date(y, m - 1, d);
}

function normalizeStatus(status: string): string {
  // 백엔드: 완료 / 접수 / 미완료 → 프론트: 완료 / 접수 / 미완료
  if (status === "미완료") return "미완료";
  return status;
}

function toWorkOrder(dto: import("@/hooks/report_detail/type").ReportDetailDto, index: number): WorkOrder {
  return {
    id: index + 1,
    code: dto.code,
    name: dto.name,
    startDate: parseLocalDate(dto.startDate),
    endDate: parseLocalDate(dto.endDate),
    duration: dto.duration,
    department: dto.department,
    status: normalizeStatus(dto.status),
    assignee: dto.approval, // 백엔드에 assignee 필드 없음 → approval로 표시
    approval: dto.approval,
  };
}


// ─── 달력 컴포넌트 ────────────────────────────────────────────────────────────

function DatePickerPopover({
  value,
  onChange,
  onClose,
}: {
  value: Date;
  onChange: (d: Date) => void;
  onClose: () => void;
}) {
  const [viewYear, setViewYear] = useState(value.getFullYear());
  const [viewMonth, setViewMonth] = useState(value.getMonth());
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) onClose();
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, [onClose]);

  const firstDay = new Date(viewYear, viewMonth, 1).getDay();
  const daysInMonth = new Date(viewYear, viewMonth + 1, 0).getDate();
  const cells: (number | null)[] = [
    ...Array(firstDay).fill(null),
    ...Array.from({ length: daysInMonth }, (_, i) => i + 1),
  ];
  while (cells.length % 7 !== 0) cells.push(null);

  const MONTH_NAMES = ["1월","2월","3월","4월","5월","6월","7월","8월","9월","10월","11월","12월"];
  const DAY_NAMES = ["일","월","화","수","목","금","토"];

  const today = new Date();

  return (
    <div
      ref={ref}
      className="absolute z-50 top-10 left-0 bg-white border border-border rounded-xl shadow-xl p-4 w-64"
      style={{ boxShadow: "0 8px 32px rgba(28,45,79,0.18)" }}
    >
      {/* 헤더 */}
      <div className="flex items-center justify-between mb-3">
        <button
          className="p-1 rounded hover:bg-slate-100 transition-colors"
          onClick={() => {
            if (viewMonth === 0) { setViewMonth(11); setViewYear(y => y - 1); }
            else setViewMonth(m => m - 1);
          }}
        >
          <ChevronLeft size={16} />
        </button>
        <span className="text-sm font-bold text-[#1C2D4F]">
          {viewYear}년 {MONTH_NAMES[viewMonth]}
        </span>
        <button
          className="p-1 rounded hover:bg-slate-100 transition-colors"
          onClick={() => {
            if (viewMonth === 11) { setViewMonth(0); setViewYear(y => y + 1); }
            else setViewMonth(m => m + 1);
          }}
        >
          <ChevronRight size={16} />
        </button>
      </div>
      {/* 요일 */}
      <div className="grid grid-cols-7 mb-1">
        {DAY_NAMES.map((d, i) => (
          <div key={d} className={`text-center text-[10px] font-semibold py-1 ${i === 0 ? "text-red-400" : i === 6 ? "text-blue-400" : "text-muted-foreground"}`}>{d}</div>
        ))}
      </div>
      {/* 날짜 */}
      <div className="grid grid-cols-7">
        {cells.map((day, idx) => {
          if (!day) return <div key={idx} />;
          const thisDate = new Date(viewYear, viewMonth, day);
          const isSelected = isSameDay(thisDate, value);
          const isToday = isSameDay(thisDate, today);
          const dow = idx % 7;
          return (
            <button
              key={idx}
              className={`text-xs py-1.5 rounded-lg transition-colors font-medium
                ${isSelected ? "text-white" : isToday ? "font-bold" : ""}
                ${isSelected ? "" : "hover:bg-slate-100"}
                ${dow === 0 && !isSelected ? "text-red-400" : ""}
                ${dow === 6 && !isSelected ? "text-blue-400" : ""}
              `}
              style={isSelected ? { backgroundColor: "#1C2D4F", color: "#fff" } : isToday ? { color: "#3A6499" } : {}}
              onClick={() => { onChange(thisDate); onClose(); }}
            >
              {day}
            </button>
          );
        })}
      </div>
      {/* 오늘 버튼 */}
      <div className="mt-3 pt-3 border-t border-border">
        <button
          className="w-full text-xs text-center py-1 rounded-lg hover:bg-slate-100 transition-colors font-medium"
          style={{ color: "#3A6499" }}
          onClick={() => { onChange(new Date()); onClose(); }}
        >
          오늘로 이동
        </button>
      </div>
    </div>
  );
}

// ─── 서브 컴포넌트: 통계 카드 ─────────────────────────────────────────────────

function SummaryCard({
  icon: Icon,
  value,
  label,
  highlight,
}: {
  icon: React.ElementType;
  value: string;
  label: string;
  highlight: boolean;
}) {
  return (
    <Card className="flex-1 min-w-[30px]">
      <CardContent className="flex items-center gap-3 px-4 py-0 h-[50px]">
        <Icon
          className="shrink-0"
          size={26}
          style={{ color: highlight ? "#ef4444" : "#4A7AAA" }}
        />
        <div>
          <p
            className="text-xl font-bold leading-tight"
            style={{ color: highlight ? "#ef4444" : "#1C2D4F" }}
          >
            {value}
          </p>
          <p className="text-[11px] text-muted-foreground whitespace-pre-line leading-tight mt-0.5">
            {label}
          </p>
        </div>
      </CardContent>
    </Card>
  );
}
// ─── 서브 컴포넌트: 간트 범례 ────────────────────────────────────────────────────

function GanttLegend() {
  const steps = [
    { label: "1건", color: "#F8FAFC" },
    { label: "2건", color: "#E2E8F0" },
    { label: "3건", color: "#CBD5E1" },
    { label: "4건", color: "#94A3B8" },
    { label: "5~6건", color: "#64748B" },
    { label: "7건 이상", color: "#475569" },
  ];

  return (
    <div className="flex items-center gap-3">
      <span className="text-[10px] font-bold text-[#1C2D4F]">겹침:</span>
      {steps.map((s) => (
        <div key={s.label} className="flex items-center gap-1">
          <div className="w-4 h-3 rounded-sm border border-slate-200" style={{ backgroundColor: s.color , opacity: 0.4}} />
          <span className="text-[9px] text-muted-foreground">{s.label}</span>
        </div>
      ))}
    </div>
  );
}
// ─── 서브 컴포넌트: 간트 행 ────────────────────────────────────────────────────

function GanttRow({
  order,
  filteredOrders,
  dates,
  todayDate,
  selected,
  onSelect,
}: Readonly<{
  order: WorkOrder;
  filteredOrders: WorkOrder[];
  dates: Date[];
  todayDate: Date;
  selected: boolean;
  onSelect: (o: WorkOrder) => void;
}>) {
  return (
    <tr
      className={`border-b border-border text-xs cursor-pointer transition-colors ${
        selected ? "bg-blue-50" : "hover:bg-slate-50"
      }`}
      onClick={() => onSelect(order)}
    >
      <td className="px-2 py-1.5 text-muted-foreground w-6">{order.id}</td>
      <td className="px-2 py-1.5 font-medium text-[#3A6499] w-14">{order.code}</td>
      <td className="px-2 py-1.5 w-14">
        <Badge variant="outline" className="text-[10px] scale-90">{order.department}</Badge>
      </td>
      <td className="px-2 py-1.5 font-medium w-36 truncate max-w-[144px]">{order.name}</td>
      <td className="px-2 py-1.5 w-20">
        <span className={`font-semibold ${
          order.status === "완료" ? "text-[#3A6080]" : 
          order.status === "미완료" ? "text-[#7AAAC8]" : "text-gray-500"
        }`}>
          {order.status}
        </span>
      </td>
      <td className="px-2 py-1.5 text-muted-foreground w-14">{formatDateFull(order.startDate).slice(5)}</td>
      <td className="px-2 py-1.5 text-muted-foreground w-14">{formatDateFull(order.endDate).slice(5)}</td>
      <td className="px-2 py-1.5 text-center w-8">{order.duration}</td>
      
      {dates.map((d, colIdx) => {
        const ts = d.getTime();
        const isBar = ts >= order.startDate.getTime() && ts <= order.endDate.getTime();
        const isStart = isSameDay(d, order.startDate);
        const isEnd = isSameDay(d, order.endDate);
        const isToday = isSameDay(d, todayDate);
        const overlapCount = filteredOrders.filter(o => 
            d.getTime() >= o.startDate.getTime() && d.getTime() <= o.endDate.getTime()
        ).length;

        return (
          <td key={colIdx} className="relative p-0 h-8" style={{ width: "48px", minWidth: "48px" }}>
            <div className="absolute inset-0 pointer-events-none" style={{ borderLeft: "1px dashed #e2e8f0" }} />

            {overlapCount > 0 && (
                <div 
                className="absolute inset-0"
                style={{ backgroundColor: getOverlapColor(overlapCount), opacity: 0.4, zIndex: 0 }} 
                />
            )}

            {isBar && (
              <div className={`absolute top-1/2 -translate-y-1/2 h-4 w-full ${isStart ? "rounded-l" : ""} ${isEnd ? "rounded-r" : ""}`} 
                   style={{ backgroundColor: selected ? "#3A6499" : "#4A7AAA", zIndex: 1 }} />
            )}

            {isToday && (
                <div
                className="absolute top-0 bottom-0 pointer-events-none z-10"
                style={{
                    left: "24px",
                    borderLeft: "2px dashed #3A6499",
                    opacity: 0.6,
                    zIndex: 2
                }}
                />
            )}

          </td>
        );
      })}
    </tr>
  );
}

const getOverlapColor = (count: number) => {
  if (count <= 1) return "#F8FAFC";
  if (count === 2) return "#E2E8F0";
  if (count === 3) return "#CBD5E1";
  if (count === 4) return "#94A3B8";
  if (count <= 6) return "#64748B";
  return "#475569";
};


// ─── 메인 컴포넌트 ────────────────────────────────────────────────────────────

/* 🌟 변경점 1: 기존 string 문자열 배열 대신 컴포넌트 바인딩이 가능한 객체 배열 구조로 리팩토링 */
type DeptTab = "전체" | "영업" | "배전" | "기술";

const DEPT_TABS: { key: DeptTab; label: string; icon: React.ElementType }[] = [
  { key: "전체", label: "전체", icon: ClipboardList },
  { key: "영업", label: "영업", icon: CircleDollarSign }, // 하이니스께서 고르신 달러 기호 적용
  { key: "배전", label: "배전", icon: Zap },
  { key: "기술", label: "기술", icon: Wrench },
];

export default function ReportDetailMain() {
  const todayReal = new Date();
  todayReal.setHours(0, 0, 0, 0);

  const [baseDate, setBaseDate] = useState<Date>(todayReal);
  const [showCalendar, setShowCalendar] = useState(false);
  const [deptTab, setDeptTab] = useState<DeptTab>("전체");
  const [selected, setSelected] = useState<WorkOrder | null>(null);

  const [allOrders, setAllOrders] = useState<WorkOrder[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchOrders = useCallback(async (centerDate: Date) => {
    setIsLoading(true);
    setError(null);
    try {
      const rangeStart = addDays(centerDate, -15);
      const rangeEnd   = addDays(centerDate, 15);
      const fmt = (d: Date) =>
        `${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, "0")}${String(d.getDate()).padStart(2, "0")}`;
      const dtos = await getReportDetail(fmt(rangeStart), fmt(rangeEnd));
      setAllOrders(dtos.map(toWorkOrder));
    } catch (e) {
      setError("데이터를 불러오는 중 오류가 발생했습니다.");
      setAllOrders([]);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchOrders(baseDate);
  }, [baseDate, fetchOrders]);

  const dates: Date[] = Array.from({ length: 31 }, (_, i) =>
    addDays(baseDate, i - 15)
  );

  const filteredOrders = allOrders.filter(
    (o) => deptTab === "전체" || o.department === deptTab
  );

  const total = filteredOrders.length;
  const done = filteredOrders.filter((o) => o.status === "완료").length;
  const inProgress = filteredOrders.filter((o) => o.status === "미완료").length; 
  const received = filteredOrders.filter((o) => o.status === "접수").length;

  const chartData = dates.map((d) => {
    const ts = d.getTime();
    const value = filteredOrders.filter(
      (o) => ts >= o.startDate.getTime() && ts <= o.endDate.getTime()
    ).length;
    return { date: formatDateLabel(d), value, isToday: isSameDay(d, baseDate), dateObj: d };
  });

  const maxSimul = Math.max(...chartData.map((d) => d.value));
  const maxSimulDate = chartData.find((d) => d.value === maxSimul);

  const avgDuration =
    filteredOrders.length > 0
      ? (filteredOrders.reduce((s, o) => s + Number(o.duration), 0) / filteredOrders.length).toFixed(1)
      : "0";

  const todayIndex = 15;

  const SUMMARY_STATS = [
    { icon: ClipboardList, value: `${total} 건`, label: "전체 작업지시서", highlight: false },
    { icon: CheckCircle2, value: `${done} 건`, label: `완료 ${total ? Math.round((done / total) * 100) : 0}%`, highlight: false },
    { icon: Settings2, value: `${inProgress} 건`, label: `미완료 ${total ? Math.round((inProgress / total) * 100) : 0}%`, highlight: false },
    { icon: GitMerge, value: `${received} 건`, label: `접수 ${total ? Math.round((received / total) * 100) : 0}% (지시서 작성 및 승인 단계)`, highlight: false },
    { icon: Flame, value: `${maxSimul} 건`, label: `최대 동시 진행\n${maxSimulDate?.date ?? "-"}`, highlight: true },
    { icon: Clock, value: `${avgDuration} 일`, label: "평균 작업 기간", highlight: false },
  ];

 const handleSelect = (order: WorkOrder) => {
    setSelected(order);
  };

  const rangeStart = addDays(baseDate, -15);
  const rangeEnd = addDays(baseDate, 15);
  const fmtRange = (d: Date) => `${d.getFullYear()}.${String(d.getMonth()+1).padStart(2,"0")}.${String(d.getDate()).padStart(2,"0")}`;

  return (
    <div className="min-h-screen p-4 space-y-4" style={{ backgroundColor: "#F0F3F8" }}>

      {/* ── 최상단 헤더 바 ── */}
      <div className="flex items-center justify-between flex-wrap gap-3">
        {/* 날짜 선택 */}
        <div className="relative flex items-center gap-2">
          <button
            className="flex items-center gap-2 bg-white border border-border rounded-lg px-3 py-2 text-sm font-medium shadow-sm hover:shadow-md transition-shadow"
            style={{ color: "#1C2D4F" }}
            onClick={() => setShowCalendar((v) => !v)}
          >
            <CalendarDays size={16} style={{ color: "#3A6499" }} />
            <span>{fmtRange(rangeStart)}</span>
            <span className="text-muted-foreground">~</span>
            <span>{fmtRange(rangeEnd)}</span>
            <span className="text-xs text-muted-foreground ml-1">(오늘 기준 ±15일)</span>
          </button>
          {showCalendar && (
            <DatePickerPopover
              value={baseDate}
              onChange={(d) => { setBaseDate(d); setSelected(null); }}
              onClose={() => setShowCalendar(false)}
            />
          )}
          {!isSameDay(baseDate, todayReal) && (
            <button
              className="text-xs px-2.5 py-1.5 rounded-lg border border-border bg-white hover:bg-slate-50 transition-colors font-medium"
              style={{ color: "#3A6499" }}
              onClick={() => { setBaseDate(todayReal); setSelected(null); }}
            >
              오늘로
            </button>
          )}
        </div>

        {/* 부서 필터 탭 */}
        <div className="flex items-center gap-1.5 bg-white border border-border rounded-lg p-1 shadow-sm">
          {/* 🌟 변경점 2: DEPT_TABS 배열 객체를 풀어서 아이콘과 텍스트를 flex로 정렬 */}
          {DEPT_TABS.map((tab) => {
            const Icon = tab.icon;
            const active = deptTab === tab.key;
            return (
              <button
                key={tab.key}
                className={`flex items-center gap-1.5 px-4 py-1.5 rounded-md text-sm font-semibold transition-all ${
                  active ? "text-white shadow-sm" : "text-muted-foreground hover:text-foreground hover:bg-slate-50"
                }`}
                style={active ? { backgroundColor: "#1C2D4F" } : {}}
                onClick={() => { setDeptTab(tab.key); setSelected(null); }}
              >
                <Icon size={14} />
                {tab.label}
              </button>
            );
          })}
        </div>
      </div>

      {/* ── 작업지시서 일정현황 박스 (통계 카드만) ── */}
      <Card className="border border-border/60 shadow-md">
        <CardHeader className="pb-1 pt-1 px-5">
          <CardTitle className="text-sm font-semibold flex items-center gap-1.5">
            <span>📋</span> 작업지시서 일정현황
          </CardTitle>
        </CardHeader>
        <CardContent className="px-4 pb-1">
          <div className="flex gap-3 flex-wrap">
            {SUMMARY_STATS.map((s, i) => (
              <SummaryCard key={i} {...s} />
            ))}
          </div>
        </CardContent>
      </Card>

      {/* ── 일자별 바 차트 + 간트 영역 ── */}
      <div className="space-y-4">

      {/* ── 일자별 바 차트 (독립 박스) ── */}
          <Card className="border border-border/60 shadow-md">
            <CardHeader className="pb-1 pt-3 px-5">
              <CardTitle className="text-sm font-semibold flex items-center gap-1.5">
                <span>📊</span> 일자별 동시 진행 작업 수
              </CardTitle>
              <p className="text-xs text-muted-foreground">
                막대 높이 = 해당 일자에 동시 진행 중인 작업지시서 수
              </p>
            </CardHeader>
            <CardContent className="pb-4 pt-2 overflow-x-auto">
                <div
                style={{
                    display:"grid",
                    gridTemplateColumns:`repeat(${dates.length},48px)`,
                    width:`${dates.length * 48}px`,
                    height:"230px",
                    position:"relative",
                }}
                >

                {chartData.map((d,index)=>{
                    const BAR_MAX_H = 155;
                    const barH = maxSimul > 0 ? Math.round((d.value / maxSimul) * BAR_MAX_H) : 0;
                    return (
                <div
                    key={index}
                    style={{
                    width:"48px",
                    height:"100%",
                    position:"relative",
                    borderLeft:"1px dashed #e2e8f0",
                    }}
                >

                    {/* 막대 */}
                    {d.value > 0 && (
                    <div
                        style={{
                        position:"absolute",
                        bottom:"35px",
                        left:"12px",
                        width:"24px",
                        height:`${barH}px`,
                        backgroundColor:
                            d.isToday
                            ? "#4A7AAA"
                            : index < todayIndex
                            ? "#7AAAC8"
                            : "#B8CFE0",
                        borderRadius:"3px 3px 0 0",
                        }}
                    />
                    )}


                    {/* 숫자 */}
                    {d.value > 0 && (
                    <div
                        style={{
                        position:"absolute",
                        bottom:`${35 + barH + 2}px`,
                        width:"48px",
                        textAlign:"center",
                        fontSize:"10px",
                        color:"#475569",
                        }}
                    >
                        {d.value}
                    </div>
                    )}


                    {/* 날짜 */}
                    <div
                    style={{
                        position:"absolute",
                        bottom:"8px",
                        width:"48px",
                        textAlign:"center",
                        fontSize:"10px",
                        color:d.isToday
                        ? "#1C2D4F"
                        : "#94a3b8",
                        fontWeight:d.isToday ? 700 : 400,
                    }}
                    >
                    {d.date}
                    </div>


                </div>
                    );
                })}


                {/* 오늘 기준선 */}
                <div
                style={{
                position:"absolute",
                left:`${todayIndex * 48 + 24}px`,
                top:0,
                height:"190px",
                borderLeft:"2px dashed #3A6499",
                opacity:0.6,
                }}
                />


                </div>

                </CardContent>
          </Card>


        
          {/* 간트 차트 + 선택 정보 */}
          <div className="flex gap-3 items-start">
            {/* 간트 테이블 */}
            <Card className="flex-1 overflow-hidden min-w-0 border border-border/60">
              <CardHeader className="pb-2 pt-3 px-5 flex flex-row items-center justify-between">
                <CardTitle className="text-sm font-semibold flex items-center gap-1.5">
                    <span>📋</span> 간트 차트
                </CardTitle>
                <GanttLegend />
              </CardHeader>
              <CardContent className="p-0 overflow-x-auto">
                <table className="border-collapse" style={{ width: `${6 * 80 + dates.length * 48}px`, minWidth: `${6 * 80 + dates.length * 48}px`, tableLayout: "fixed" }}>
                  <thead>
                    <tr className="border-b border-border bg-slate-50">
                      <th className="px-2 py-2 text-left w-6 text-xs text-muted-foreground font-normal">#</th>
                      <th className="px-2 py-2 text-left w-20 text-xs text-muted-foreground font-normal">지시서</th>
                      <th className="px-2 py-2 text-left w-14 text-xs text-muted-foreground font-normal">부서</th>
                      <th className="px-2 py-2 text-left w-36 text-xs text-muted-foreground font-normal">작업명</th>
                      <th className="px-2 py-2 text-left w-14 text-xs text-muted-foreground font-normal">상태</th>
                      <th className="px-2 py-2 text-left w-14 text-xs text-muted-foreground font-normal">시작</th>
                      <th className="px-2 py-2 text-left w-14 text-xs text-muted-foreground font-normal">종료</th>
                      <th className="px-2 py-2 text-center w-14 text-xs text-muted-foreground font-normal">기간</th>
                      {dates.map((d, i) => {
                        const isCenter = i === todayIndex;
                        return (
                          <th
                            key={i}
                            className="py-2 text-center"
                            style={{
                              minWidth: "48px",
                              width: "48px",
                              fontSize: "10px",
                              fontWeight: isCenter ? 700 : 400,
                              color: isCenter ? "#1C2D4F" : "#94a3b8",
                            }}
                          >
                            {formatDateLabel(d)}
                          </th>
                        );
                      })}
                    </tr>
                  </thead>
                  <tbody>
                    {filteredOrders.length === 0 ? (
                      <tr>
                        <td colSpan={6 + dates.length} className="text-center py-8 text-muted-foreground text-xs">
                          {isLoading
                            ? "데이터를 불러오는 중입니다..."
                            : error
                            ? error
                            : "해당 부서의 작업지시서가 없습니다."}
                        </td>
                      </tr>
                    ) : (
                      filteredOrders.map((order) => (
                        <GanttRow
                          key={order.id}
                          order={order}
                          filteredOrders={filteredOrders}
                          dates={dates}
                          todayDate={baseDate}
                          selected={selected?.id === order.id}
                          onSelect={handleSelect}
                        />
                      ))
                    )}
                  </tbody>
                </table>
              </CardContent>
            </Card>

            {/* 선택한 작업 정보 */}
            <Card className="w-56 shrink-0 border border-border/60">
              <CardHeader className="pb-2 pt-3 px-4">
                <CardTitle className="text-sm font-semibold flex items-center gap-1">
                  <span>📌</span> 선택한 작업 정보
                </CardTitle>
              </CardHeader>
              <CardContent className="px-4 pb-4">
                {selected ? (
                  <div className="space-y-3">
                    <div>
                      <p className="text-xs font-bold text-[#3A6499]">{selected.code}</p>
                      <p className="text-sm font-semibold text-[#1C2D4F] leading-tight mt-0.5">
                        {selected.name}
                      </p>
                    </div>
                    <div className="space-y-2 text-xs">
                      {[
                        { label: "부서 · 파트", value: selected.department },
                        { label: "담당자", value: selected.assignee },
                        { label: "시작일", value: formatDateFull(selected.startDate) },
                        { label: "종료일", value: formatDateFull(selected.endDate) },
                        { label: "기간", value: `${selected.duration}일` },
                      ].map(({ label, value }) => (
                        <div key={label} className="flex justify-between items-start gap-1">
                          <span className="text-muted-foreground shrink-0">{label}</span>
                          <span className="font-medium text-right">{value}</span>
                        </div>
                      ))}
                      <div className="flex justify-between items-center">
                        <span className="text-muted-foreground">상태</span>
                        <Badge
                          variant="secondary"
                          className="text-[10px] px-1.5 py-0"
                          style={{
                            backgroundColor:
                              selected.status === "미완료" ? "#EFF6FF"
                              : selected.status === "완료" ? "#F0FDF4"
                              : "#FFF7ED",
                            color:
                              selected.status === "미완료" ? "#3A6499"
                              : selected.status === "완료" ? "#16a34a"
                              : "#ea580c",
                          }}
                        >
                          ● {selected.status}
                        </Badge>
                      </div>
                      <div className="flex justify-between items-center">
                        <span className="text-muted-foreground">결재</span>
                        <Badge
                          variant="secondary"
                          className="text-[10px] px-1.5 py-0"
                          style={{
                            backgroundColor: selected.approval === "미요청" ? "#FFF7ED" : "#F0FDF4",
                            color: selected.approval === "미요청" ? "#ea580c" : "#16a34a",
                          }}
                        >
                          ● {selected.approval}
                        </Badge>
                      </div>
                    </div>
                    <Button
                      className="w-full text-xs h-8"
                      style={{ backgroundColor: "#1C2D4F" }}
                    >
                      상세보기
                    </Button>
                  </div>
                ) : (
                  <div className="flex flex-col items-center justify-center py-8 text-center gap-2">
                    <ClipboardList size={28} className="text-muted-foreground opacity-40" />
                    <p className="text-xs text-muted-foreground">
                      간트 차트에서<br />작업을 선택하세요
                    </p>
                  </div>
                )}
              </CardContent>
            </Card>
          </div>
      </div>
    </div>
  );
}