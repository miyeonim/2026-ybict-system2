import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { PieChart, Pie, Cell } from "recharts";
import { ChevronLeft, ChevronRight } from "lucide-react";

const COLOR_RECEIVED = "#7AAAC8";
const COLOR_NOT_DONE = "#4A7AAA";
const COLOR_DONE = "#1C2D4F";

function HalfDonutChart({ received, done, notDone }: any) {
  const total = (received ?? 0) + done + notDone;
  const rate = total > 0 ? Math.round((done / total) * 100) : 0;
  const chartData = [
    ...(received !== undefined ? [{ name: "접수", value: received, color: COLOR_RECEIVED }] : []),
    { name: "미완료", value: notDone, color: COLOR_NOT_DONE },
    { name: "완료", value: done, color: COLOR_DONE },
  ];

  return (
    <div className="relative w-full flex flex-col items-center justify-center" style={{ height: "240px" }}>
      <div className="relative w-[280px] h-[150px] flex items-center justify-center overflow-hidden">
        <PieChart width={280} height={200} style={{ marginBottom: "-50px" }}>
          <Pie data={chartData} cx="50%" cy="80%" startAngle={180} endAngle={0} innerRadius={75} outerRadius={115} paddingAngle={1} dataKey="value">
            {chartData.map((entry, index) => <Cell key={`cell-${index}`} fill={entry.color} />)}
          </Pie>
        </PieChart>
        <div className="absolute" style={{ bottom: "0px", textAlign: "center" }}>
          <span className="block text-3xl font-extrabold" style={{ color: "#1C2D4F" }}>{rate}%</span>
          <span className="block text-xs text-muted-foreground font-medium mt-1">완료율</span>
        </div>
      </div>
      <div className="flex items-center gap-5 mt-6 text-xs text-muted-foreground flex-wrap justify-center">
        {received !== undefined && (
          <span className="flex items-center gap-1.5">
            <span className="inline-block w-2.5 h-2.5 rounded-full" style={{ backgroundColor: COLOR_RECEIVED }} />
            접수 <b style={{ color: "#1C2D4F" }}>{received}건</b>
          </span>
        )}
        <span className="flex items-center gap-1.5">
          <span className="inline-block w-2.5 h-2.5 rounded-full" style={{ backgroundColor: COLOR_NOT_DONE }} />
          미완료 <b style={{ color: "#1C2D4F" }}>{notDone}건</b>
        </span>
        <span className="flex items-center gap-1.5">
          <span className="inline-block w-2.5 h-2.5 rounded-full" style={{ backgroundColor: COLOR_DONE }} />
          완료 <b style={{ color: "#1C2D4F" }}>{done}건</b>
        </span>
      </div>
    </div>
  );
}

function StackedBarRow({ name, done, notDone }: any) {
  const total = done + notDone;
  const pct = (v: number) => (total > 0 ? (v / total) * 100 : 0);
  return (
    <div className="flex items-center gap-3">
      <span className="w-20 shrink-0 text-sm font-semibold truncate" style={{ color: "#1C2D4F" }}>{name}</span>
      <div className="flex-1 h-7 rounded-md overflow-hidden flex bg-[#F0F3F8]">
        {notDone > 0 && (
          <div className="h-full flex items-center justify-center text-[11px] font-bold text-white whitespace-nowrap" style={{ width: `${pct(notDone)}%`, backgroundColor: COLOR_NOT_DONE }}>
            {pct(notDone) > 12 ? `미완료 ${notDone}` : notDone}
          </div>
        )}
        {done > 0 && (
          <div className="h-full flex items-center justify-center text-[11px] font-bold text-white whitespace-nowrap" style={{ width: `${pct(done)}%`, backgroundColor: COLOR_DONE }}>
            {pct(done) > 12 ? `완료 ${done}` : done}
          </div>
        )}
      </div>
      <span className="w-8 shrink-0 text-sm font-bold text-right" style={{ color: "#1C2D4F" }}>{total}</span>
    </div>
  );
}

export function WorkPart2({ selectedYear, deptTab, done, notDone, receivedTotal, barRows, partPage, setPartPage, pageCount }: any) {
  const pagedRows = barRows.slice(partPage * 4, partPage * 4 + 4);

  return (
    <Card className="border border-border/60 shadow-md">
      <CardHeader className="pb-3 pt-4 px-5">
        <CardTitle className="text-sm font-semibold flex items-center gap-1.5">
          <span>📊</span> {selectedYear}년 {deptTab === "전체" ? "전체" : deptTab === "마이" ? "마이" : deptTab + " 부서"} 처리 현황
        </CardTitle>
      </CardHeader>
      <CardContent className="px-6 pb-6 pt-2">
        <div className="grid grid-cols-1 lg:grid-cols-[340px_1fr] gap-10 items-center">
          <div className="flex justify-center w-full border-r border-dashed border-border/60 pr-4 lg:pr-10">
            <HalfDonutChart received={receivedTotal} done={done} notDone={notDone} />
          </div>
          <div className="flex flex-col w-full">
            <div className="flex items-center justify-between mb-3">
              <p className="text-sm font-bold text-muted-foreground">
                {deptTab === "전체" ? "부서별 처리 현황 (완료/미완료)" : deptTab === "마이" ? "나의 작업 처리 현황" : `${deptTab}부서 파트별 처리 현황`}
              </p>
              {pageCount > 1 && (
                <div className="flex items-center gap-2 text-xs text-muted-foreground">
                  <button onClick={() => setPartPage((p: number) => Math.max(0, p - 1))} disabled={partPage === 0} className="p-1 rounded hover:bg-[#F0F3F8] disabled:opacity-30">
                    <ChevronLeft size={14} />
                  </button>
                  <span>{partPage + 1} / {pageCount}</span>
                  <button onClick={() => setPartPage((p: number) => Math.min(pageCount - 1, p + 1))} disabled={partPage >= pageCount - 1} className="p-1 rounded hover:bg-[#F0F3F8] disabled:opacity-30">
                    <ChevronRight size={14} />
                  </button>
                </div>
              )}
            </div>
            <div className="space-y-4 flex-1">
              {pagedRows.map((p: any, i: number) => <StackedBarRow key={i} name={p.name} done={p.완료} notDone={p.미완료} />)}
            </div>
            <div className="flex items-center gap-4 mt-6 pt-4 border-t text-xs text-muted-foreground">
              <span className="flex items-center gap-1.5"><span className="inline-block w-2.5 h-2.5 rounded-sm" style={{ backgroundColor: COLOR_NOT_DONE }} /> 미완료</span>
              <span className="flex items-center gap-1.5"><span className="inline-block w-2.5 h-2.5 rounded-sm" style={{ backgroundColor: COLOR_DONE }} /> 완료</span>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}