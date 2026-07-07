"use client";

import { useState, useEffect } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { COLOR } from "./types";
import type { AlertItem } from "@hooks/report_total/types";
// ─── API ───────────────────────────────────────────────────────
import { fetchLongAlerts, fetchDueAlerts } from "@/hooks/report_total/AlertController";

// ─── Props ───────────────────────────────────────────────────────
interface AlertPanelProps {
  readonly longItems: AlertItem[];
  readonly longTotal: number;
  readonly dueItems: AlertItem[];
  readonly dueTotal: number;
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
}

function LongItemCard({ item }: LongItemCardProps) {
  return (
    <div
      className="flex justify-between items-center px-3 py-2.5 rounded-xl transition-colors"
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
}

function DueItemCard({ item }: DueItemCardProps) {
  return (
    <div
      className="flex justify-between items-center px-3 py-2.5 rounded-xl transition-colors"
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
export default function AlertPanel() {
  const longPageSize = 2;  // 장기 미처리: 한 페이지에 3개 요청
  const duePageSize  = 2;  // 마감 임박: 한 페이지에 2개 요청

  // 장기 미처리 State
  const [longPage, setLongPage] = useState(1);
  const [longData, setLongData] = useState({ items: [] as AlertItem[], total: 0, totalPages: 1 });

  // 마감 임박 State
  const [duePage, setDuePage] = useState(1);
  const [dueData, setDueData] = useState({ items: [] as AlertItem[], total: 0, totalPages: 1 });

  // 장기 미처리 데이터 패칭 (longPage가 변경될 때마다 실행)
  useEffect(() => {
    fetchLongAlerts(longPage, longPageSize)
      .then(data => {
        if(data) {
          setLongData({ items: data.content, total: data.totalElements, totalPages: data.totalPages || 1 });
        }
      })
      .catch(err => console.error("장기 미처리 조회 실패", err));
  }, [longPage]);

  // 마감 임박 데이터 패칭 (duePage가 변경될 때마다 실행)
  useEffect(() => {
    fetchDueAlerts(duePage, duePageSize)
      .then(data => {
        if(data) {
          setDueData({ items: data.content, total: data.totalElements, totalPages: data.totalPages || 1 });
        }
      })
      .catch(err => console.error("마감 임박 조회 실패", err));
  }, [duePage]);

  return (
    <Card className="w-full border shadow-none" style={{ background: COLOR.white }}>
      {/* ← h-full 제거, flex flex-col 제거 */}
      <CardContent className="p-4">
    
        {/* ── 장기 미처리 ── */}
        <div className="flex flex-col mb-3">
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
          <div className="flex flex-col gap-1.5" style={{ minHeight: "106px" }}>
            {longData.items.map((item, i) => (
              <LongItemCard key={`${item.instId || i}`} item={item} />
            ))}
          </div>
        </div>

        {/* 구분선 */}
        <div className="h-[1px] mb-3" style={{ background: "#EDF2F7" }} />

        {/* ── 마감 임박 ── */}
        <div className="flex flex-col">
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
          {/* 카드에 값이 없어도 106px 으로 크기 고정하기 */}
          <div className="flex flex-col gap-1.5" style={{ minHeight: "106px" }}>
            {dueData.items.map((item, i) => (
              <DueItemCard key={`${item.instId || i}`} item={item} />
            ))}
          </div>
        </div>

      </CardContent>
    </Card>
  );
}