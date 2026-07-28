import { useState } from "react";
import { format, isValid, parse } from "date-fns";
import { CalendarIcon } from "lucide-react";

import { cn } from "~/lib/utils";
import { Button } from "@/components/ui/button";
import { YearMonthCalendar } from "@/components/ui/year-month-calendar";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";

// yyyy-MM-dd 문자열 <-> Date 변환을 감싼 달력 클릭형 날짜 선택 필드
export function DatePickerField({
  value,
  onChange,
  disabled,
  placeholder = "날짜 선택",
  className,
}: {
  value: string; // yyyy-MM-dd
  onChange: (value: string) => void;
  disabled?: boolean;
  placeholder?: string;
  className?: string;
}) {
  const [open, setOpen] = useState(false);
  const parsed = value ? parse(value, "yyyy-MM-dd", new Date()) : undefined;
  const selected = parsed && isValid(parsed) ? parsed : undefined;

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          type="button"
          variant="outline"
          disabled={disabled}
          className={cn(
            "w-[180px] justify-start text-left font-normal",
            !selected && "text-slate-400",
            className,
          )}
        >
          <CalendarIcon className="size-4" />
          {selected ? format(selected, "yyyy-MM-dd") : placeholder}
        </Button>
      </PopoverTrigger>
      <PopoverContent
        className="w-auto overflow-hidden p-0"
        align="start"
        side="bottom"
        avoidCollisions={false}
      >
        <YearMonthCalendar
          selected={selected}
          onSelect={(date) => {
            onChange(format(date, "yyyy-MM-dd"));
            setOpen(false);
          }}
        />
      </PopoverContent>
    </Popover>
  );
}
