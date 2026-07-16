"use client";

import { useEffect, useState } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { ChevronLeft, ChevronRight, ClipboardList, CircleDollarSign, Zap, Wrench, UserRound } from "lucide-react";
import { COLOR } from "@hooks/work_part/type";
import type { AlertItem } from "@hooks/report_total/types";
// ─── API ───────────────────────────────────────────────────────
import { fetchLongAlerts, fetchDueAlerts } from "@/hooks/work_part/WorkPartController";
import WorkDetailModal from "@routes/common/components/WorkDetailModal";
import type { WorkDetailItem } from "@routes/common/components/WorkDetailModal";

// 알림 항목(AlertItem)은 부서/제목/경과일 정도만 담고 있으므로,
// 나머지 상세(지시내용/첨부파일/협의/결재 이력 등)는 모달이 workOrderNo로 다시 조회한다.
const toWorkDetailItem = (item: AlertItem): WorkDetailItem => ({
  workOrderNo: item.instId ?? "",
  title: item.title,
  department: item.dept,
  dueDt: "",
});

/* ────────────────────────────────────────────────────────────────────────
 * 📌 부서 필터 규칙
 * ────────────────────────────────────────────────────────────────────────
 * - 전체        : 영업/배전/기술 전체 건 노출
 * - 영업/배전/기술 : 선택 부서(파트) 건만 노출
 * - 마이        : its_work_history.ACT_SIGN='S' + 내 사번 매칭 건만 노출
 *
 * fetchLongAlerts / fetchDueAlerts 는 year, type(dept), sabun, page, size를
 * 백엔드(GET /api/work_part/v1.0/alerts/long|due)에 그대로 전달한다.
 * ──────────────────────────────────────────────────────────────────────── */

type DeptKey = "전체" | "영업" | "배전" | "기술" | "마이";

const DEPT_TABS: { key: DeptKey; label: string; icon: React.ElementType }[] = [
  { key: "전체", label: "전체", icon: ClipboardList },
  { key: "영업", label: "영업", icon: CircleDollarSign },
  { key: "배전", label: "배전", icon: Zap },
  { key: "기술", label: "기술", icon: Wrench },
  { key: "마이", label: "마이", icon: UserRound },
];

// ─── Props ───────────────────────────────────────────────────────
interface AlertPanelProps {
  /** 상위(대시보드)의 부서 탭 상태를 그대로 받아쓰고 싶을 때 전달.
   *  전달하면 AlertPanel 내부의 자체 탭 UI는 숨기고 이 값으로 필터링만 함. */
  readonly deptFilter?: DeptKey;
  /** 조회 연도. 상위(WorksPartMain)의 selectedYear를 그대로 전달받는다. */
  readonly year: number;
  /** '마이' 탭 조회에 필요한 로그인 사용자 사번. */
  readonly sabun?: string;
}

// ─── 부서 필터 탭 ───────────────────────────────────────────────
interface DeptFilterTabsProps {
  readonly value: DeptKey;
  readonly onChange: (key: DeptKey) => void;
}

function DeptFilterTabs({ value, onChange }: DeptFilterTabsProps) {
  return (
    <div className="flex items-center gap-1 bg-[#F5F7FB] border border-[#E8EEF6] rounded-lg p-1">
      {DEPT_TABS.map((tab) => {
        const Icon = tab.icon;
        const active = value === tab.key;
        return (
          <button
            key={tab.key}
            onClick={() => onChange(tab.key)}
            className="flex items-center gap-1 px-2.5 py-1 rounded-md text-[12px] font-semibold transition-colors"
            style={{
              backgroundColor: active ? "#1C2D4F" : "transparent",
              color: active ? "#fff" : "#64748b",
            }}
          >
            <Icon size={12} />
            {tab.label}
          </button>
        );
      })}
    </div>
  );
}

// ─── 섹션 헤더 ───────────────────────────────────────────────────
interface SectionHeaderProps {
  readonly emoji: string;
  readonly title: string;
  readonly total: number;
  readonly page: number;
  readonly totalPages: number;
  readonly onPrev: () => void;
  readonly onNext: () => void;
  readonly accentColor: string;
}

function SectionHeader({ emoji, title, total, page, totalPages, onPrev, onNext, accentColor }: SectionHeaderProps) {
  return (
    <div className="flex justify-between items-center mb-2">
      {/* 왼쪽: 제목 + 건수 배지 */}
      <div className="flex items-center gap-2">
        <span className="text-[14px] font-bold tracking-tight" style={{ color: "#1C2D4F" }}>
          {emoji} {title}
        </span>
        <span
          className="text-[12px] font-semibold px-2 py-0.5 rounded-full"
          style={{ background: accentColor + "18", color: accentColor }}
        >
          {total}건
        </span>
      </div>

      {/* 오른쪽: 페이지네이션 */}
      <div className="flex items-center gap-1">
        <span className="text-[12px]" style={{ color: COLOR.steel }}>
          {page} / {totalPages}
        </span>
        <Button
          variant="ghost"
          size="icon"
          className="h-5 w-5 rounded"
          style={{ color: "#1C2D4F" }}
          disabled={page <= 1}
          onClick={onPrev}
        >
          <ChevronLeft className="h-3 w-3" />
        </Button>
        <Button
          variant="ghost"
          size="icon"
          className="h-5 w-5 rounded"
          style={{ color: "#1C2D4F" }}
          disabled={page >= totalPages}
          onClick={onNext}
        >
          <ChevronRight className="h-3 w-3" />
        </Button>
      </div>
    </div>
  );
}

// ─── 장기 미처리 아이템 ──────────────────────────────────────────
interface LongItemCardProps {
  readonly item: AlertItem;
  readonly onClick: () => void;
}

function LongItemCard({ item, onClick }: LongItemCardProps) {
  return (
    <div
      onClick={onClick}
      className="flex justify-between items-center px-3 py-2.5 rounded-xl transition-colors cursor-pointer hover:brightness-95"
      style={{ background: "#F8FAFD", border: "1px solid #E8EEF6" }}
    >
      {/* 왼쪽: 부서 + 제목 */}
      <div className="flex flex-col gap-0.5">
        <span className="text-[9px] font-medium" style={{ color: COLOR.steel }}>
          {item.dept}
        </span>
        <span className="text-[12px] font-semibold leading-tight" style={{ color: "#1C2D4F" }}>
          {item.title}
        </span>
      </div>

      {/* 오른쪽: 경과일 배지 */}
      <span
        className="shrink-0 ml-3 text-[10px] font-semibold px-2 py-0.5 rounded-full"
        style={{ background: "#FEF3E2", color: "#92530A" }}
      >
        {item.date}
      </span>
    </div>
  );
}

// ─── 마감 임박 아이템 ────────────────────────────────────────────
interface DueItemCardProps {
  readonly item: AlertItem;
  readonly onClick: () => void;
}

function DueItemCard({ item, onClick }: DueItemCardProps) {
  return (
    <div
      onClick={onClick}
      className="flex justify-between items-center px-3 py-2.5 rounded-xl transition-colors cursor-pointer hover:brightness-95"
      style={{ background: "#FFF8F8", border: "1px solid #FADDDD" }}
    >
      {/* 왼쪽: 부서 + 제목 */}
      <div className="flex flex-col gap-0.5">
        <span className="text-[9px] font-medium" style={{ color: "#C0392B" }}>
          {item.dept}
        </span>
        <span className="text-[12px] font-semibold leading-tight" style={{ color: "#1C2D4F" }}>
          {item.title}
        </span>
      </div>

      {/* 오른쪽: 마감일 배지 */}
      <span
        className="shrink-0 ml-3 text-[10px] font-semibold px-2 py-0.5 rounded-full"
        style={{ background: "#FEE8E8", color: "#C0392B" }}
      >
        {item.date}
      </span>
    </div>
  );
}

// ─── 메인 컴포넌트 ───────────────────────────────────────────────
export default function AlertPanel({ deptFilter, year, sabun }: AlertPanelProps) {
  const longPageSize = 3; // 장기 미처리: 기본 3건 노출
  const duePageSize = 3; // 마감 임박: 기본 3건 노출

  // 부서 필터 (전체/영업/배전/기술/마이) - 두 섹션 공통 적용
  // deptFilter prop이 주어지면 상위(대시보드) 탭을 그대로 따르고(controlled),
  // 없으면 AlertPanel 자체 탭으로 독립 동작(uncontrolled)
  const isControlled = deptFilter !== undefined;
  const [internalDeptTab, setInternalDeptTab] = useState<DeptKey>("전체");
  const deptTab = isControlled ? (deptFilter as DeptKey) : internalDeptTab;

  // 장기 미처리 State
  const [longPage, setLongPage] = useState(1);
  const [longData, setLongData] = useState({ items: [] as AlertItem[], total: 0, totalPages: 1 });

  // 마감 임박 State
  const [duePage, setDuePage] = useState(1);
  const [dueData, setDueData] = useState({ items: [] as AlertItem[], total: 0, totalPages: 1 });

  // 클릭한 알림 항목의 상세 모달 State
  const [detailItem, setDetailItem] = useState<AlertItem | null>(null);

  const handleDeptChange = (key: DeptKey) => {
    if (!isControlled) setInternalDeptTab(key);
    setLongPage(1);
    setDuePage(1);
  };

  // deptFilter prop이 바뀌면(상위 탭 전환) 페이지도 초기화
  useEffect(() => {
    if (isControlled) {
      setLongPage(1);
      setDuePage(1);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [deptFilter]);

  // 장기 미처리 데이터 패칭 (longPage / deptTab / year / sabun 변경 시 실행)
  useEffect(() => {
    fetchLongAlerts({ year, type: deptTab, sabun, page: longPage, size: longPageSize })
      .then((data) => {
        if (data) {
          setLongData({ items: data.content, total: data.totalElements, totalPages: data.totalPages || 1 });
        }
      })
      .catch((err) => console.error("장기 미처리 조회 실패", err));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [longPage, deptTab, year, sabun]);

  // 마감 임박 데이터 패칭 (duePage / deptTab / year / sabun 변경 시 실행)
  useEffect(() => {
    fetchDueAlerts({ year, type: deptTab, sabun, page: duePage, size: duePageSize })
      .then((data) => {
        if (data) {
          setDueData({ items: data.content, total: data.totalElements, totalPages: data.totalPages || 1 });
        }
      })
      .catch((err) => console.error("마감 임박 조회 실패", err));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [duePage, deptTab, year, sabun]);

  return (
    <div className="w-full space-y-3">
      {/* ── 부서 필터 탭 (전체/영업/배전/기술/마이) — 상위에서 deptFilter를 주면 숨김 ── */}
      {!isControlled && (
        <div className="flex justify-end">
          <DeptFilterTabs value={deptTab} onChange={handleDeptChange} />
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
        {/* ── 장기 미처리 ── */}
        <Card className="w-full border shadow-none" style={{ background: COLOR.white }}>
          <CardContent className="p-4">
            <SectionHeader
              emoji="🚩"
              title="장기 미처리 (3개월 초과)"
              total={longData.total}
              page={longPage}
              totalPages={longData.totalPages}
              accentColor="#92530A"
              onPrev={() => setLongPage((p) => Math.max(1, p - 1))}
              onNext={() => setLongPage((p) => Math.min(longData.totalPages, p + 1))}
            />
            <div className="flex flex-col gap-1.5" style={{ minHeight: "150px" }}>
              {longData.items.length > 0 ? (
                longData.items.map((item, i) => (
                  <LongItemCard key={`${item.instId || i}`} item={item} onClick={() => setDetailItem(item)} />
                ))
              ) : (
                <EmptyState />
              )}
            </div>
          </CardContent>
        </Card>

        {/* ── 마감 임박 ── */}
        <Card className="w-full border shadow-none" style={{ background: COLOR.white }}>
          <CardContent className="p-4">
            <SectionHeader
              emoji="⏰"
              title="마감 임박 (마감일 3일 이내)"
              total={dueData.total}
              page={duePage}
              totalPages={dueData.totalPages}
              accentColor="#C0392B"
              onPrev={() => setDuePage((p) => Math.max(1, p - 1))}
              onNext={() => setDuePage((p) => Math.min(dueData.totalPages, p + 1))}
            />
            {/* 카드에 값이 없어도 크기 고정하기 */}
            <div className="flex flex-col gap-1.5" style={{ minHeight: "150px" }}>
              {dueData.items.length > 0 ? (
                dueData.items.map((item, i) => (
                  <DueItemCard key={`${item.instId || i}`} item={item} onClick={() => setDetailItem(item)} />
                ))
              ) : (
                <EmptyState />
              )}
            </div>
          </CardContent>
        </Card>
      </div>

      <WorkDetailModal
        key={detailItem?.instId}
        item={detailItem ? toWorkDetailItem(detailItem) : null}
        onClose={() => setDetailItem(null)}
        footer={
          <Button variant="outline" onClick={() => setDetailItem(null)}>
            닫기
          </Button>
        }
      />
    </div>
  );
}

function EmptyState() {
  return (
    <div className="flex items-center justify-center h-full py-6 text-[12px]" style={{ color: COLOR.steel }}>
      해당 조건의 항목이 없습니다.
    </div>
  );
}