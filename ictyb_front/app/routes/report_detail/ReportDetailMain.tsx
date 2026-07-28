"use client";

import { useState, useRef, useEffect, useCallback } from "react";
import { getReportDetail } from "@/hooks/report_detail/ReportDetailController";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";
import WorkDetailModal from "@routes/common/components/WorkDetailModal";
import type { WorkDetailItem } from "@routes/common/components/WorkDetailModal";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip";
import { YearMonthRangeCalendar } from "@/components/ui/year-month-range-calendar";
import {
  ClipboardList,
  CheckCircle2,
  Settings2,
  GitMerge,
  Flame,
  Clock,
  CalendarDays,
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

const GANTT_RANGE_DAYS = 10; // 간트 차트 좌우 표시 일수
const GANTT_PAGE_SIZE = 10; // 간트 차트 한 페이지당 표시 행 수

// 간트 차트의 #, 부서, 작업명, 상태, 시작, 종료, 기간 컬럼 폭 합계(px).
// 일자별 바 차트의 날짜 열을 간트 차트 날짜 열과 같은 x 위치에 맞추기 위한 오프셋으로도 사용.
const GANTT_LABEL_WIDTH = 24 + 56 + 144 + 56 + 56 + 56 + 56;

// 일자별 바 차트는 간트 차트의 라벨 칼럼 폭(GANTT_LABEL_WIDTH)만큼 왼쪽 여백이 생기므로,
// 그 여백을 간트 차트보다 더 과거 날짜의 실제 막대로 채운다. 간트 차트에는 이 날짜들을 표시하지 않는다.
const EXTRA_BAR_DAYS = Math.floor(GANTT_LABEL_WIDTH / 48);
const BAR_CHART_MARGIN = GANTT_LABEL_WIDTH - EXTRA_BAR_DAYS * 48;

function addDays(date: Date, days: number): Date {
  const d = new Date(date);
  d.setDate(d.getDate() + days);
  return d;
}

function daysBetweenInclusive(start: Date, end: Date): Date[] {
  const days: Date[] = [];
  for (let d = start; d.getTime() <= end.getTime(); d = addDays(d, 1)) {
    days.push(d);
  }
  return days;
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

// code(=INST_ID)를 workOrderNo로 그대로 사용해 WorkDetailModal이 상세를 재조회하도록 한다.
function toWorkDetailItem(order: WorkOrder): WorkDetailItem {
  return {
    workOrderNo: order.code,
    title: order.name,
    department: order.department,
    dueDt: formatDateFull(order.endDate),
    status: order.status,
    approvalStatus: order.approval,
    managerName: order.assignee,
  };
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
  const tooltipContent = (
    <TooltipContent side="top" className="block max-w-none w-64 rounded-lg p-3 text-left">
      <p className="text-xs font-bold" style={{ color: "#8EC4FF" }}>{order.code}</p>
      <p className="text-sm font-semibold leading-tight mt-0.5">{order.name}</p>
      <div className="mt-1.5 space-y-0.5 text-[11px] opacity-90">
        <div>부서 · 파트: {order.department}</div>
        <div>담당자: {order.assignee}</div>
        <div>{formatDateFull(order.startDate)} ~ {formatDateFull(order.endDate)} ({order.duration}일)</div>
        <div>상태: {order.status} · 결재: {order.approval}</div>
      </div>
    </TooltipContent>
  );

  return (
    <tr
      className={`border-b border-border text-xs cursor-pointer transition-colors ${
        selected ? "bg-blue-50" : "hover:bg-slate-50"
      }`}
      onClick={() => onSelect(order)}
    >
      <td className="px-2 py-1.5 text-muted-foreground w-6">{order.id}</td>
      <td className="px-2 py-1.5 w-14">
        <Badge variant="outline" className="text-[10px] scale-90">{order.department}</Badge>
      </td>
      <td className="px-2 py-1.5 font-medium w-36 max-w-[144px]">
        <Tooltip>
          <TooltipTrigger asChild>
            <span className="block truncate cursor-pointer">{order.name}</span>
          </TooltipTrigger>
          {tooltipContent}
        </Tooltip>
      </td>
      <td className="px-2 py-1.5 w-14">
        <span className={`font-semibold ${
          order.status === "완료" ? "text-[#3A6080]" :
          order.status === "미완료" ? "text-[#7AAAC8]" : "text-gray-500"
        }`}>
          {order.status}
        </span>
      </td>
      <td className="px-2 py-1.5 text-muted-foreground w-14">{formatDateFull(order.startDate).slice(5)}</td>
      <td className="px-2 py-1.5 text-muted-foreground w-14">{formatDateFull(order.endDate).slice(5)}</td>
      <td className="px-2 py-1.5 text-center w-14">{order.duration}</td>
      
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
              <Tooltip>
                <TooltipTrigger asChild>
                  <div className={`absolute top-1/2 -translate-y-1/2 h-4 w-full cursor-pointer ${isStart ? "rounded-l" : ""} ${isEnd ? "rounded-r" : ""}`}
                       style={{ backgroundColor: selected ? "#3A6499" : "#4A7AAA", zIndex: 1 }} />
                </TooltipTrigger>
                {tooltipContent}
              </Tooltip>
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

  const [range, setRange] = useState<{ start: Date; end: Date }>(() => ({
    start: addDays(todayReal, -GANTT_RANGE_DAYS),
    end: addDays(todayReal, GANTT_RANGE_DAYS),
  }));
  const [isCustomRange, setIsCustomRange] = useState(false);
  const [showCalendar, setShowCalendar] = useState(false);
  const [deptTab, setDeptTab] = useState<DeptTab>("전체");
  const [selected, setSelected] = useState<WorkOrder | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);
  // 일자별 바 차트에서 클릭한 날짜(선택 시 간트 차트를 해당 날짜와 겹치는 작업지시서로만 필터링)
  const [selectedBarDate, setSelectedBarDate] = useState<Date | null>(null);

  const [allOrders, setAllOrders] = useState<WorkOrder[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(1);

  // 일자별 바 차트 ↔ 간트 차트 가로 스크롤 연동 (날짜 열 위치를 계속 맞추기 위함)
  const barScrollRef = useRef<HTMLDivElement>(null);
  const ganttScrollRef = useRef<HTMLDivElement>(null);
  const isSyncingScroll = useRef(false);

  const handleBarScroll = () => {
    if (isSyncingScroll.current) return;
    isSyncingScroll.current = true;
    if (ganttScrollRef.current && barScrollRef.current) {
      ganttScrollRef.current.scrollLeft = barScrollRef.current.scrollLeft;
    }
    isSyncingScroll.current = false;
  };

  const handleGanttScroll = () => {
    if (isSyncingScroll.current) return;
    isSyncingScroll.current = true;
    if (barScrollRef.current && ganttScrollRef.current) {
      barScrollRef.current.scrollLeft = ganttScrollRef.current.scrollLeft;
    }
    isSyncingScroll.current = false;
  };

  const fetchOrders = useCallback(async (rangeStart: Date, rangeEnd: Date, custom: boolean) => {
    setIsLoading(true);
    setError(null);
    try {
      // 기본(오늘 ±N일) 모드에서는 일자별 바 차트 왼쪽 여백을 실제 막대로 채우기 위해 간트 차트보다
      // 더 과거 날짜까지 함께 조회한다. 사용자가 기간을 직접 지정한 경우에는 지정한 시작일보다
      // 과거 데이터를 추가로 불러오지 않는다.
      const fetchStart = custom ? rangeStart : addDays(rangeStart, -EXTRA_BAR_DAYS);
      const fmt = (d: Date) =>
        `${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, "0")}${String(d.getDate()).padStart(2, "0")}`;
      const dtos = await getReportDetail(fmt(fetchStart), fmt(rangeEnd));
      setAllOrders(dtos.map(toWorkOrder));
    } catch (e) {
      setError("데이터를 불러오는 중 오류가 발생했습니다.");
      setAllOrders([]);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchOrders(range.start, range.end, isCustomRange);
  }, [range, isCustomRange, fetchOrders]);

  const dates: Date[] = daysBetweenInclusive(range.start, range.end);

  // 일자별 바 차트 왼쪽 여백을 채우는 간트 차트 이전 날짜들(간트 차트에는 표시하지 않음).
  // 기간을 직접 지정한 경우 이 구간의 데이터는 조회되지 않으므로 막대 없이 빈 칸으로만 표시된다.
  const barExtraDates: Date[] = Array.from({ length: EXTRA_BAR_DAYS }, (_, i) =>
    addDays(range.start, i - EXTRA_BAR_DAYS)
  );
  const barChartDates = [...barExtraDates, ...dates];

  const deptFilteredOrders = allOrders.filter(
    (o) => deptTab === "전체" || o.department === deptTab
  );

  // 간트 차트/통계는 기존과 동일하게 ±GANTT_RANGE_DAYS 범위만 사용
  const filteredOrders = deptFilteredOrders.filter(
    (o) => o.endDate.getTime() >= dates[0].getTime() && o.startDate.getTime() <= dates[dates.length - 1].getTime()
  );

  // 바 차트에서 특정 날짜를 선택한 경우, 그 날짜와 겹치는 작업지시서만 간트 차트에 표시
  const ganttOrders = selectedBarDate
    ? filteredOrders.filter(
        (o) =>
          o.startDate.getTime() <= selectedBarDate.getTime() &&
          selectedBarDate.getTime() <= o.endDate.getTime()
      )
    : filteredOrders;

  const totalPages = Math.max(1, Math.ceil(ganttOrders.length / GANTT_PAGE_SIZE));

  const pagedOrders = ganttOrders.slice(
    (currentPage - 1) * GANTT_PAGE_SIZE,
    currentPage * GANTT_PAGE_SIZE
  );

  // 부서 탭, 조회 기간, 바 차트 날짜 선택이 바뀌어 목록이 달라지면 1페이지로 복귀
  useEffect(() => {
    setCurrentPage(1);
  }, [deptTab, range, selectedBarDate]);

  // 필터링 결과가 줄어들어 현재 페이지가 범위를 벗어나면 마지막 페이지로 보정
  useEffect(() => {
    if (currentPage > totalPages) setCurrentPage(totalPages);
  }, [currentPage, totalPages]);

  // 현재 페이지 주변 ±2 페이지 + 처음/끝 페이지만 노출, 나머지는 생략(...) 처리
  const getPageNumbers = (page: number, total: number): (number | "ellipsis")[] => {
    if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1);
    const pages = new Set<number>([1, total, page - 1, page, page + 1]);
    const sorted = Array.from(pages)
      .filter((p) => p >= 1 && p <= total)
      .sort((a, b) => a - b);
    const result: (number | "ellipsis")[] = [];
    sorted.forEach((p, i) => {
      if (i > 0 && p - sorted[i - 1] > 1) result.push("ellipsis");
      result.push(p);
    });
    return result;
  };

  const total = filteredOrders.length;
  const done = filteredOrders.filter((o) => o.status === "완료").length;
  const inProgress = filteredOrders.filter((o) => o.status === "미완료").length; 
  const received = filteredOrders.filter((o) => o.status === "접수").length;

  const chartData = barChartDates.map((d) => {
    const ts = d.getTime();
    const value = deptFilteredOrders.filter(
      (o) => ts >= o.startDate.getTime() && ts <= o.endDate.getTime()
    ).length;
    return { date: formatDateLabel(d), value, isToday: isSameDay(d, todayReal), dateObj: d };
  });

  // "최대 동시 진행" 통계는 간트 차트와 동일한 ±GANTT_RANGE_DAYS 범위 기준(왼쪽 채움용 날짜는 제외)
  const scopedChartData = chartData.slice(EXTRA_BAR_DAYS);
  const maxSimul = Math.max(...scopedChartData.map((d) => d.value));
  const maxSimulDate = scopedChartData.find((d) => d.value === maxSimul);

  // 바 높이 정규화는 채움용 날짜를 포함해 화면에 실제로 그려지는 전체 구간 기준
  const barScaleMax = Math.max(...chartData.map((d) => d.value));

  const avgDuration =
    filteredOrders.length > 0
      ? (filteredOrders.reduce((s, o) => s + Number(o.duration), 0) / filteredOrders.length).toFixed(1)
      : "0";

  const todayIndex = dates.findIndex((d) => isSameDay(d, todayReal));
  const barTodayIndex = barChartDates.findIndex((d) => isSameDay(d, todayReal));

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
    setDetailOpen(true);
  };

  const fmtRange = (d: Date) => `${d.getFullYear()}.${String(d.getMonth()+1).padStart(2,"0")}.${String(d.getDate()).padStart(2,"0")}`;

  return (
    <div className="min-h-screen p-4 space-y-4" style={{ backgroundColor: "#F0F3F8" }}>

      {/* ── 최상단 헤더 바 ── */}
      <div className="flex items-center justify-between flex-wrap gap-3">
        {/* 날짜 선택 */}
        <div className="relative flex items-center gap-2">
          <Popover open={showCalendar} onOpenChange={setShowCalendar}>
            <PopoverTrigger asChild>
              <button
                className="flex items-center gap-2 bg-white border border-border rounded-lg px-3 py-2 text-sm font-medium shadow-sm hover:shadow-md transition-shadow"
                style={{ color: "#1C2D4F" }}
              >
                <CalendarDays size={16} style={{ color: "#3A6499" }} />
                <span>{fmtRange(range.start)}</span>
                <span className="text-muted-foreground">~</span>
                <span>{fmtRange(range.end)}</span>
                <span
                  className={`text-xs text-muted-foreground ml-1 ${
                    isCustomRange ? "invisible" : ""
                  }`}
                >
                  (오늘 기준 ±{GANTT_RANGE_DAYS}일)
                </span>
              </button>
            </PopoverTrigger>
            <PopoverContent
              className="w-auto overflow-hidden p-0"
              align="start"
              side="bottom"
              avoidCollisions={false}
            >
              <YearMonthRangeCalendar
                selected={range}
                onSelect={(r) => {
                  setRange(r);
                  setIsCustomRange(true);
                  setSelected(null);
                  setSelectedBarDate(null);
                  setShowCalendar(false);
                }}
              />
            </PopoverContent>
          </Popover>
          {isCustomRange && (
            <button
              className="text-xs px-2.5 py-1.5 rounded-lg border border-border bg-white hover:bg-slate-50 transition-colors font-medium"
              style={{ color: "#3A6499" }}
              onClick={() => {
                setRange({ start: addDays(todayReal, -GANTT_RANGE_DAYS), end: addDays(todayReal, GANTT_RANGE_DAYS) });
                setIsCustomRange(false);
                setSelected(null);
                setSelectedBarDate(null);
              }}
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
                onClick={() => { setDeptTab(tab.key); setSelected(null); setSelectedBarDate(null); }}
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
            <CardHeader className="pb-1 pt-3 px-5 flex flex-row items-start justify-between">
              <div>
                <CardTitle className="text-sm font-semibold flex items-center gap-1.5">
                  <span>📊</span> 일자별 동시 진행 작업 수
                </CardTitle>
                <p className="text-xs text-muted-foreground">
                  막대 높이 = 해당 일자에 동시 진행 중인 작업지시서 수
                </p>
              </div>
              {selectedBarDate && (
                <button
                  className="text-xs px-2.5 py-1 rounded-lg border border-border bg-white hover:bg-slate-50 transition-colors font-medium"
                  style={{ color: "#3A6499" }}
                  onClick={() => setSelectedBarDate(null)}
                >
                  초기화
                </button>
              )}
            </CardHeader>
            <CardContent
                ref={barScrollRef}
                onScroll={handleBarScroll}
                className="px-0 pb-4 pt-2 overflow-x-auto"
            >
                <div
                style={{
                    display:"grid",
                    gridTemplateColumns:`repeat(${barChartDates.length},48px)`,
                    width:`${barChartDates.length * 48}px`,
                    height:"230px",
                    position:"relative",
                    marginLeft:`${BAR_CHART_MARGIN}px`,
                }}
                >

                {chartData.map((d,index)=>{
                    const BAR_MAX_H = 155;
                    const barH = barScaleMax > 0 ? Math.round((d.value / barScaleMax) * BAR_MAX_H) : 0;
                    const isFiller = index < EXTRA_BAR_DAYS;
                    const isSelectedBar = !isFiller && !!selectedBarDate && isSameDay(d.dateObj, selectedBarDate);
                    return (
                <div
                    key={index}
                    onClick={isFiller ? undefined : () => setSelectedBarDate(d.dateObj)}
                    style={{
                    width:"48px",
                    height:"100%",
                    position:"relative",
                    borderLeft:"1px dashed #e2e8f0",
                    cursor: isFiller ? "default" : "pointer",
                    backgroundColor: isSelectedBar ? "rgba(74,122,170,0.10)" : undefined,
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
                            // 간트 차트 라벨 칼럼(부서/작업명/상태/시작/종료/기간) 위에 정렬용으로 채운
                            // 선택 기간 이전 구간은 실제 조회 기간과 구분되도록 회색으로 표시
                            isFiller
                            ? "#a1a1aa"
                            : d.isToday
                            ? "#4A7AAA"
                            : index < barTodayIndex
                            ? "#7AAAC8"
                            : "#B8CFE0",
                        borderRadius:"3px 3px 0 0",
                        outline: isSelectedBar ? "2px solid #ef4444" : "none",
                        outlineOffset: "1px",
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
                        color:isSelectedBar
                        ? "#ef4444"
                        : d.isToday
                        ? "#1C2D4F"
                        : "#94a3b8",
                        fontWeight:isSelectedBar || d.isToday ? 700 : 400,
                    }}
                    >
                    {d.date}
                    </div>


                </div>
                    );
                })}


                {/* 오늘 기준선(오늘이 현재 조회 기간 안에 있을 때만 표시) */}
                {barTodayIndex !== -1 && (
                <div
                style={{
                position:"absolute",
                left:`${barTodayIndex * 48 + 24}px`,
                top:0,
                height:"190px",
                borderLeft:"2px dashed #3A6499",
                opacity:0.6,
                }}
                />
                )}


                </div>

                </CardContent>
          </Card>


        
          {/* 간트 차트 */}
          <TooltipProvider delayDuration={100}>
          <Card className="overflow-hidden border border-border/60">
              <CardHeader className="pb-2 pt-3 px-5 flex flex-row items-center justify-between">
                <div className="flex items-center gap-2">
                  <CardTitle className="text-sm font-semibold flex items-center gap-1.5">
                      <span>📋</span> 간트 차트
                  </CardTitle>
                  {selectedBarDate && (
                    <Badge
                      variant="outline"
                      className="text-[10px]"
                      style={{ borderColor: "#4A7AAA", color: "#3A6499" }}
                    >
                      {formatDateFull(selectedBarDate)} 필터 중
                    </Badge>
                  )}
                </div>
                <div className="flex items-center gap-3">
                  <GanttLegend />
                </div>
              </CardHeader>
              <CardContent
                ref={ganttScrollRef}
                onScroll={handleGanttScroll}
                className="p-0 overflow-x-auto"
              >
                <table className="border-collapse" style={{ width: `${GANTT_LABEL_WIDTH + dates.length * 48}px`, minWidth: `${GANTT_LABEL_WIDTH + dates.length * 48}px`, tableLayout: "fixed" }}>
                  <thead>
                    <tr className="border-b border-border bg-slate-50">
                      <th className="px-2 py-2 text-left w-6 text-xs text-muted-foreground font-normal">#</th>
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
                    {ganttOrders.length === 0 ? (
                      <tr>
                        <td colSpan={6 + dates.length} className="text-center py-8 text-muted-foreground text-xs">
                          {isLoading
                            ? "데이터를 불러오는 중입니다..."
                            : error
                            ? error
                            : selectedBarDate
                            ? "선택한 날짜에 해당하는 작업지시서가 없습니다."
                            : "해당 부서의 작업지시서가 없습니다."}
                        </td>
                      </tr>
                    ) : (
                      pagedOrders.map((order) => (
                        <GanttRow
                          key={order.id}
                          order={order}
                          filteredOrders={filteredOrders}
                          dates={dates}
                          todayDate={todayReal}
                          selected={selected?.id === order.id}
                          onSelect={handleSelect}
                        />
                      ))
                    )}
                  </tbody>
                </table>
              </CardContent>

              {ganttOrders.length > 0 && (
                <div className="px-4 pb-3 pt-1 border-t border-border/60">
                  <div className="text-right text-[11px] text-muted-foreground mb-1">
                    총 {ganttOrders.length}건
                  </div>
                  <Pagination>
                    <PaginationContent>
                      <PaginationItem>
                        <PaginationPrevious
                          href="#"
                          onClick={(e) => {
                            e.preventDefault();
                            if (currentPage > 1) setCurrentPage(currentPage - 1);
                          }}
                          aria-disabled={currentPage === 1}
                          className={currentPage === 1 ? "pointer-events-none opacity-40" : ""}
                        />
                      </PaginationItem>
                      {getPageNumbers(currentPage, totalPages).map((p, idx) =>
                        p === "ellipsis" ? (
                          <PaginationItem key={`ellipsis-${idx}`}>
                            <span className="px-2 text-slate-400">...</span>
                          </PaginationItem>
                        ) : (
                          <PaginationItem key={p}>
                            <PaginationLink
                              href="#"
                              isActive={p === currentPage}
                              onClick={(e) => {
                                e.preventDefault();
                                setCurrentPage(p);
                              }}
                            >
                              {p}
                            </PaginationLink>
                          </PaginationItem>
                        )
                      )}
                      <PaginationItem>
                        <PaginationNext
                          href="#"
                          onClick={(e) => {
                            e.preventDefault();
                            if (currentPage < totalPages) setCurrentPage(currentPage + 1);
                          }}
                          aria-disabled={currentPage === totalPages}
                          className={currentPage === totalPages ? "pointer-events-none opacity-40" : ""}
                        />
                      </PaginationItem>
                    </PaginationContent>
                  </Pagination>
                </div>
              )}
            </Card>
          </TooltipProvider>
      </div>

      <WorkDetailModal
        key={selected?.code}
        item={detailOpen && selected ? toWorkDetailItem(selected) : null}
        onClose={() => setDetailOpen(false)}
        footer={
          <Button variant="outline" onClick={() => setDetailOpen(false)}>
            닫기
          </Button>
        }
      />
    </div>
  );
}