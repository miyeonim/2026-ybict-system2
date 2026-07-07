import type { PersonCount } from "@hooks/report_daily/type";
import { calcTotals } from "@hooks/report_daily/type";

interface PersonCountTableProps {
  people: PersonCount[];
  editable?: boolean;
  onChangePerson?: (index: number, field: "inProgress" | "delayed" | "distributed", value: number) => void;
  onRemovePerson?: (index: number) => void;
}

export default function PersonCountTable({
  people,
  editable = false,
  onChangePerson,
  onRemovePerson,
}: PersonCountTableProps) {
  const totals = calcTotals(people);

  if (people.length === 0) {
    return (
      <div className="text-center text-xs text-muted-foreground py-6 border border-dashed border-border rounded-lg">
        등록된 인원이 없습니다.
      </div>
    );
  }

  // 진행중/일정지연은 DB 연동 값이라 읽기전용, 배포건수만 직접 입력/조정 가능
  const renderCell = (
    idx: number,
    field: "inProgress" | "delayed" | "distributed",
    value: number,
    colorClass = "text-[#1C2D4F]"
  ) => {
    if (editable && field === "distributed") {
      return (
        <input
          type="number"
          min={0}
          value={value}
          onChange={(e) => onChangePerson?.(idx, field, Math.max(0, Number(e.target.value) || 0))}
          className={`w-14 text-center border border-border rounded-md py-1 text-sm font-semibold focus:outline-none focus:ring-1 focus:ring-[#3A6499] ${colorClass}`}
        />
      );
    }
    return <span className={`font-semibold ${colorClass}`}>{value}</span>;
  };

  return (
    <div className="overflow-x-auto rounded-lg border border-border">
      <table className="w-full text-sm text-center border-collapse">
        <thead>
          <tr className="bg-slate-50 border-b border-border">
            <th className="px-3 py-2 font-semibold text-[#1C2D4F] w-28">구분</th>
            {people.map((p, idx) => (
              <th key={`${p.name}-${idx}`} className="px-3 py-2 font-semibold text-[#1C2D4F]">
                <div className="flex items-center justify-center gap-1.5">
                  <span>{p.name}</span>
                  {editable && (
                    <button
                      type="button"
                      onClick={() => onRemovePerson?.(idx)}
                      className="text-muted-foreground hover:text-red-500 text-xs transition-colors"
                      aria-label={`${p.name} 삭제`}
                    >
                      ✕
                    </button>
                  )}
                </div>
              </th>
            ))}
            <th className="px-3 py-2 font-semibold text-[#1C2D4F] bg-slate-100 w-20">합계</th>
          </tr>
        </thead>
        <tbody>
          <tr className="border-b border-border">
            <td className="px-3 py-2 font-medium text-muted-foreground">
              진행중 <span className="text-[10px] text-muted-foreground/70">(DB)</span>
            </td>
            {people.map((p, idx) => (
              <td key={idx} className="px-3 py-2">{renderCell(idx, "inProgress", p.inProgress)}</td>
            ))}
            <td className="px-3 py-2 font-bold bg-slate-50 text-[#1C2D4F]">{totals.totalInProgress}</td>
          </tr>
          <tr className="border-b border-border">
            <td className="px-3 py-2 font-medium text-red-500">
              일정지연 <span className="text-[10px] text-red-400">(DB)</span>
            </td>
            {people.map((p, idx) => (
              <td key={idx} className="px-3 py-2">{renderCell(idx, "delayed", p.delayed, "text-red-500")}</td>
            ))}
            <td className="px-3 py-2 font-bold bg-slate-50 text-red-500">{totals.totalDelayed}</td>
          </tr>
          <tr className="border-b border-border bg-slate-50">
            <td className="px-3 py-2 font-semibold text-[#1C2D4F]">합계</td>
            {people.map((p, idx) => (
              <td key={idx} className="px-3 py-2 font-bold text-[#1C2D4F]">{p.inProgress + p.delayed}</td>
            ))}
            <td className="px-3 py-2 font-bold text-[#1C2D4F]">{totals.totalSum}</td>
          </tr>
          <tr className="bg-blue-50">
            <td className="px-3 py-2 font-semibold text-[#1C2D4F]">
              배포건수 {editable && <span className="text-[10px] font-normal text-[#3A6499]">(직접입력)</span>}
            </td>
            {people.map((p, idx) => (
              <td key={idx} className="px-3 py-2">{renderCell(idx, "distributed", p.distributed)}</td>
            ))}
            <td className="px-3 py-2 font-bold text-[#1C2D4F]">{totals.totalDistributed}</td>
          </tr>
        </tbody>
      </table>
    </div>
  );
}
