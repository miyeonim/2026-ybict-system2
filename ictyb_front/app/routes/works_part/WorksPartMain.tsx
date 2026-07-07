import { useEffect, useMemo, useState } from "react";
import { useAuthContext } from "@routes/common/jwt/AuthContext";
import AlertPanel from "./AlertPanel";
import { WorkPart1 } from "./WorkPart1";
import { WorkPart2 } from "./WorkPart2";
import { fetchWorkPartSummary } from "@hooks/work_part/WorkPartController";
import { type DeptKey, type WorkPartSummaryResponse } from "@hooks/work_part/type";
import {
  ClipboardList,
  CheckCircle2,
  AlertTriangle,
  GitMerge,
  ClipboardCheck,
  UserRound,
} from "lucide-react";

const PARTS_PER_PAGE = 4;

const EMPTY_SUMMARY: WorkPartSummaryResponse = {
  done: 0,
  notDone: 0,
  receivedTotal: null,
  barRows: [],
};

export default function WorksPartMain() {
  const { user } = useAuthContext();
  const sabun = user?.userEmpno; // AuthContext의 실제 사번 필드명에 맞게 수정 필요

  const [selectedYear, setSelectedYear] = useState<number>(2026);
  const [deptTab, setDeptTab] = useState<DeptKey>("전체");
  const [partPage, setPartPage] = useState(0);

  const [summary, setSummary] = useState<WorkPartSummaryResponse>(EMPTY_SUMMARY);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let ignore = false;
    setLoading(true);

    fetchWorkPartSummary({ year: selectedYear, type: deptTab, sabun })
      .then((data) => {
        if (!ignore) setSummary(data ?? EMPTY_SUMMARY);
      })
      .finally(() => {
        if (!ignore) setLoading(false);
      });

    return () => {
      ignore = true;
    };
  }, [selectedYear, deptTab, sabun]);

  const { done, notDone, receivedTotal, barRows } = summary;

  const total = done + notDone + (deptTab === "전체" ? receivedTotal ?? 0 : 0);

  const SUMMARY_STATS = useMemo(() => {
    if (deptTab === "전체") {
      return [
        { icon: ClipboardList, value: `${total} 건`, label: "전체 작업지시서", highlight: false },
        {
          icon: CheckCircle2,
          value: `${done} 건`,
          label: `완료 ${total ? Math.round((done / total) * 100) : 0}%`,
          highlight: false,
        },
        {
          icon: AlertTriangle,
          value: `${notDone} 건`,
          label: `미완료 ${total ? Math.round((notDone / total) * 100) : 0}%`,
          highlight: false,
        },
        {
          icon: GitMerge,
          value: `${receivedTotal ?? 0} 건`,
          label: `접수 ${total ? Math.round(((receivedTotal ?? 0) / total) * 100) : 0}%\n(지시서 배부 전 · 부서 미분류)`,
          highlight: false,
        },
      ];
    }
    return [
      {
        icon: deptTab === "마이" ? UserRound : ClipboardCheck,
        value: `${done + notDone} 건`,
        label: deptTab === "마이" ? "나와 관련된 작업지시서" : `${deptTab} 작업지시서`,
        highlight: false,
      },
      {
        icon: CheckCircle2,
        value: `${done} 건`,
        label: `완료 ${done + notDone ? Math.round((done / (done + notDone)) * 100) : 0}%`,
        highlight: false,
      },
      {
        icon: AlertTriangle,
        value: `${notDone} 건`,
        label: `미완료 ${done + notDone ? Math.round((notDone / (done + notDone)) * 100) : 0}%`,
        highlight: notDone > 0,
      },
    ];
  }, [deptTab, done, notDone, receivedTotal, total]);

  const pageCount = Math.max(1, Math.ceil(barRows.length / PARTS_PER_PAGE));

  const handleTabChange = (key: DeptKey) => {
    setDeptTab(key);
    setPartPage(0);
  };

  return (
    <div className="min-h-screen p-4 space-y-4" style={{ backgroundColor: "#F0F3F8" }}>
      {/* 작업지시서 현황1 */}
      <WorkPart1
        selectedYear={selectedYear}
        setSelectedYear={setSelectedYear}
        deptTab={deptTab}
        setDeptTab={handleTabChange}
        stats={SUMMARY_STATS}
      />

      {/* 작업지시서 현황2 */}
      <WorkPart2
        selectedYear={selectedYear}
        deptTab={deptTab}
        done={done}
        notDone={notDone}
        receivedTotal={deptTab === "전체" ? receivedTotal ?? 0 : undefined}
        barRows={barRows}
        partPage={partPage}
        setPartPage={setPartPage}
        pageCount={pageCount}
        loading={loading}
      />

      {/* 장기미처리, 마감임박 현황 */}
      <AlertPanel deptFilter={deptTab} year={selectedYear} sabun={sabun} />
    </div>
  );
}