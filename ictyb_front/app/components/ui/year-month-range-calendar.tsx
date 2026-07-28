import { useState } from "react";
import { addMonths, addYears, format, setMonth, setYear } from "date-fns";
import { ko } from "date-fns/locale";
import { ChevronLeftIcon, ChevronRightIcon } from "lucide-react";
import type { DateRange } from "react-day-picker";

import { Button } from "@/components/ui/button";
import { Calendar } from "@/components/ui/calendar";

type ViewMode = "days" | "months" | "years";

const MONTH_LABELS = [
  "1월", "2월", "3월", "4월", "5월", "6월",
  "7월", "8월", "9월", "10월", "11월", "12월",
];

function PickerHeader({
  label,
  onPrev,
  onNext,
  onLabelClick,
}: {
  label: string;
  onPrev: () => void;
  onNext: () => void;
  onLabelClick?: () => void;
}) {
  return (
    <div className="flex items-center justify-between px-1 pt-1 pb-2">
      <Button type="button" variant="ghost" size="icon-sm" onClick={onPrev} aria-label="이전">
        <ChevronLeftIcon className="size-4" />
      </Button>
      {onLabelClick ? (
        <Button type="button" variant="ghost" size="sm" className="font-medium" onClick={onLabelClick}>
          {label}
        </Button>
      ) : (
        <span className="px-2 text-sm font-medium">{label}</span>
      )}
      <Button type="button" variant="ghost" size="icon-sm" onClick={onNext} aria-label="다음">
        <ChevronRightIcon className="size-4" />
      </Button>
    </div>
  );
}

// 시작일을 먼저 클릭하고 종료일을 다시 클릭해 기간을 지정하는 달력.
// 두 날짜가 모두 선택된 순간에만 onSelect가 호출된다(시작일만 고른 상태에서는 호출하지 않음).
export function YearMonthRangeCalendar({
  selected,
  onSelect,
}: {
  selected: { start: Date; end: Date } | undefined;
  onSelect: (range: { start: Date; end: Date }) => void;
}) {
  const [view, setView] = useState<ViewMode>("days");
  const [navDate, setNavDate] = useState<Date>(() => selected?.start ?? new Date());
  // 팝오버를 열 때마다 항상 빈 선택 상태로 시작한다. 기존 범위를 미리 채워두면
  // react-day-picker가 첫 클릭만으로 완결된 범위로 인식해버려 "시작일→종료일" 2클릭 흐름이 깨진다.
  const [range, setRange] = useState<DateRange | undefined>(undefined);

  const handleRangeSelect = (r: DateRange | undefined) => {
    setRange(r);
    if (r?.from && r?.to) {
      onSelect({ start: r.from, end: r.to });
    }
  };

  if (view === "months") {
    const year = navDate.getFullYear();
    return (
      <div className="w-64 p-2">
        <PickerHeader
          label={`${year}년`}
          onPrev={() => setNavDate(addYears(navDate, -1))}
          onNext={() => setNavDate(addYears(navDate, 1))}
          onLabelClick={() => setView("years")}
        />
        <div className="grid grid-cols-3 gap-2">
          {MONTH_LABELS.map((label, idx) => {
            const isSelected = range?.from?.getFullYear() === year && range?.from?.getMonth() === idx;
            return (
              <Button
                key={label}
                type="button"
                variant={isSelected ? "default" : "ghost"}
                className="h-9"
                onClick={() => {
                  setNavDate(setMonth(navDate, idx));
                  setView("days");
                }}
              >
                {label}
              </Button>
            );
          })}
        </div>
      </div>
    );
  }

  if (view === "years") {
    const decadeStart = Math.floor(navDate.getFullYear() / 10) * 10;
    const years = Array.from({ length: 10 }, (_, i) => decadeStart + i);
    return (
      <div className="w-64 p-2">
        <PickerHeader
          label={`${decadeStart}년 - ${decadeStart + 9}년`}
          onPrev={() => setNavDate(addYears(navDate, -10))}
          onNext={() => setNavDate(addYears(navDate, 10))}
        />
        <div className="grid grid-cols-2 gap-2">
          {years.map((year) => {
            const isSelected = range?.from?.getFullYear() === year;
            return (
              <Button
                key={year}
                type="button"
                variant={isSelected ? "default" : "ghost"}
                className="h-9"
                onClick={() => {
                  setNavDate(setYear(navDate, year));
                  setView("months");
                }}
              >
                {year}년
              </Button>
            );
          })}
        </div>
      </div>
    );
  }

  return (
    <div className="w-64">
      <PickerHeader
        label={format(navDate, "yyyy년 M월", { locale: ko })}
        onPrev={() => setNavDate(addMonths(navDate, -1))}
        onNext={() => setNavDate(addMonths(navDate, 1))}
        onLabelClick={() => setView("months")}
      />
      <div className="flex justify-center">
        <Calendar
          mode="range"
          min={1}
          locale={ko}
          month={navDate}
          onMonthChange={setNavDate}
          selected={range}
          onSelect={handleRangeSelect}
          hideNavigation
          components={{ MonthCaption: () => <></> }}
          className="p-2 pt-0"
        />
      </div>
      <p className="px-3 pb-2 text-[11px] text-muted-foreground">
        {range?.from && !range?.to ? "종료일을 선택하세요" : "시작일을 클릭한 뒤 종료일을 클릭하세요"}
      </p>
    </div>
  );
}
