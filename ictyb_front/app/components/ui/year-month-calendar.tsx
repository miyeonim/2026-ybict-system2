import { useState } from "react";
import { addMonths, addYears, format, setMonth, setYear } from "date-fns";
import { ko } from "date-fns/locale";
import { ChevronLeftIcon, ChevronRightIcon } from "lucide-react";

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

// 날짜 보기(day) <-> 월 그리드(1~12월) <-> 연도 그리드(10년 단위)를 오가는 달력.
// 상단 "년월"을 클릭하면 월 그리드로, 거기서 "년"을 다시 클릭하면 10년 단위 연도 그리드로 내려간다.
// 연도 그리드에서 연도를 고르면 월 그리드로, 월 그리드에서 월을 고르면 날짜 보기로 되돌아간다.
export function YearMonthCalendar({
  selected,
  onSelect,
}: {
  selected: Date | undefined;
  onSelect: (date: Date) => void;
}) {
  const [view, setView] = useState<ViewMode>("days");
  const [navDate, setNavDate] = useState<Date>(() => selected ?? new Date());

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
            const isSelected = selected?.getFullYear() === year && selected?.getMonth() === idx;
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
            const isSelected = selected?.getFullYear() === year;
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
          mode="single"
          locale={ko}
          month={navDate}
          onMonthChange={setNavDate}
          selected={selected}
          onSelect={(date) => date && onSelect(date)}
          hideNavigation
          components={{ MonthCaption: () => <></> }}
          className="p-2 pt-0"
        />
      </div>
    </div>
  );
}
