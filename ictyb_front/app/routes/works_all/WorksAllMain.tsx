import { useEffect, useMemo, useState } from "react";
import type {
  WorksAllListItem,
  WorksAllStatus,
  WorksAllDepartment,
} from "./WorksAllDto";
import { fetchWorksAllList, fetchWorksAllCounts } from "~/hooks/work_all/WorksAllController";
import type { WorksAllCounts } from "~/hooks/work_all/WorksAllController";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
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
import { CircleDollarSign, Zap, Wrench, CalendarDays, History as HistoryIcon } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { fetchWorkDetail } from "~/hooks/work_my/WorksMyController";
import type { WorksMyApprovalHistoryItem } from "@routes/works_my/WorksMyDto";
import { useAuthContext } from "@routes/common/jwt/AuthContext";

// 상태별 뱃지 색상 (app.css의 --status-new/progress/done 토큰 재사용)
const STATUS_COLOR: Record<WorksAllStatus, string> = {
  접수: "var(--status-new)",
  "처리 중": "var(--status-progress)",
  완료: "var(--status-done)",
  협의: "#94A3B8", // slate-400
};

const DEPARTMENTS: WorksAllDepartment[] = ["영업", "배전", "기술"];

const DEPARTMENT_ICON: Record<WorksAllDepartment, React.ElementType> = {
  영업: CircleDollarSign,
  배전: Zap,
  기술: Wrench,
};

const ALL_PART = "전체";

const ALL_STATUS = "전체";
const STATUS_FILTERS: WorksAllStatus[] = ["접수", "처리 중", "협의", "완료"];
const SEEN_NEGOTIATIONS_STORAGE_KEY = "worksAll_seenNegotiations";
const PAGE_SIZE = 10;

// 마감일 조회 기본 범위 (당일 기준 ±10일)
const DUE_DT_DEFAULT_RANGE_DAYS = 10;

const formatDateYmd = (d: Date) =>
  `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;

const getDefaultDueDtRange = () => {
  const today = new Date();
  const start = new Date(today);
  start.setDate(start.getDate() - DUE_DT_DEFAULT_RANGE_DAYS);
  const end = new Date(today);
  end.setDate(end.getDate() + DUE_DT_DEFAULT_RANGE_DAYS);
  return { start: formatDateYmd(start), end: formatDateYmd(end) };
};

const DotBadge: React.FC<{ label: string; color: string }> = ({
  label,
  color,
}) => (
  <span className="inline-flex items-center gap-1.5 px-2 py-1 rounded-full text-xs font-medium bg-slate-50 border border-slate-200 text-slate-600">
    <span
      className="w-1.5 h-1.5 rounded-full"
      style={{ backgroundColor: color }}
    />
    {label}
  </span>
);


const CountBadge: React.FC<{ count: number; active: boolean }> = ({
  count,
  active,
}) => (
  <span
    className={`inline-flex items-center justify-center min-w-[20px] px-1.5 py-0.5 rounded-full text-xs font-semibold ${
      active
        ? "bg-[var(--sidebar-bg)] text-white"
        : "bg-slate-200 text-slate-600"
    }`}
  >
    {count}
  </span>
);

// yyyyMMddHHmmss -> yyyy-MM-dd HH:mm:ss (결재이력 표시용, 시분초까지)
const formatDateTime = (dt: string | null) => {
  if (!dt || dt.length < 14) return dt;
  return `${dt.slice(0, 4)}-${dt.slice(4, 6)}-${dt.slice(6, 8)} ${dt.slice(8, 10)}:${dt.slice(10, 12)}:${dt.slice(12, 14)}`;
};

// 결재이력 팝업을 여는 버튼 (목록 화면에서는 버튼만 표시, 이력은 클릭 시 조회)
const ApprovalHistoryButton: React.FC<{ onOpen: () => void }> = ({ onOpen }) => (
  <Button variant="outline" size="sm" className="h-7 px-2.5 text-xs" onClick={onOpen}>
    <HistoryIcon className="size-3.5" />
    결재이력
  </Button>
);

// 결재이력을 처리 순서대로(일렬로) 보여주는 팝업 - 열릴 때마다 상세 조회 API로 이력을 조회한다(지연 로딩)
const ApprovalHistoryDialog: React.FC<{
  workOrderNo: string | null;
  onClose: () => void;
}> = ({ workOrderNo, onClose }) => {
  const { user } = useAuthContext();
  const [history, setHistory] = useState<WorksMyApprovalHistoryItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!workOrderNo || !user) return;
    setHistory([]);
    setError(null);
    setLoading(true);
    fetchWorkDetail(workOrderNo, user.userEmpno)
      .then((detail) => setHistory(detail.approvalHistory ?? []))
      .catch(() => setError("결재이력을 불러오지 못했습니다."))
      .finally(() => setLoading(false));
  }, [workOrderNo, user]);

  return (
    <Dialog open={!!workOrderNo} onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="max-w-md">
        <DialogTitle>결재이력</DialogTitle>

        {loading ? (
          <div className="text-center py-8 text-sm text-slate-400">
            <span className="inline-block w-4 h-4 border-2 border-slate-300 border-t-[var(--sidebar-bg)] rounded-full animate-spin mr-2 align-middle" />
            불러오는 중...
          </div>
        ) : error ? (
          <div className="text-xs text-red-500 bg-red-50 px-3 py-2 rounded-md border border-red-200">
            {error}
          </div>
        ) : history.length === 0 ? (
          <p className="text-sm text-slate-400 py-6 text-center">결재이력이 없습니다.</p>
        ) : (
          <div className="flex flex-col gap-3 py-2 max-h-96 overflow-y-auto">
            {history.map((h, idx) => (
              <div key={idx} className="flex items-start gap-3">
                <div className="flex flex-col items-center pt-0.5">
                  <span className="flex items-center justify-center w-5 h-5 rounded-full bg-slate-100 text-slate-500 text-[11px] font-semibold">
                    {idx + 1}
                  </span>
                  {idx < history.length - 1 && (
                    <span className="w-px flex-1 bg-slate-200 mt-1" />
                  )}
                </div>
                <div className="pb-3 flex-1">
                  <div className="text-sm">
                    <span className="font-medium text-slate-800">{h.name}</span>
                    <span className="text-slate-400"> · {h.actIdNm}</span>
                    <span
                      className={
                        h.signLabel === "반려"
                          ? "text-red-500 font-medium ml-1"
                          : h.signLabel === "결재대기"
                          ? "text-amber-500 font-medium ml-1"
                          : "text-emerald-600 font-medium ml-1"
                      }
                    >
                      {h.signLabel}
                    </span>
                  </div>
                  <div className="text-xs text-slate-400 mt-0.5">
                    {h.regDt ? formatDateTime(h.regDt) : "결재 대기 중"}
                  </div>
                  {h.signLabel === "반려" && h.reason && (
                    <div className="text-xs text-red-500 mt-1 bg-red-50 border border-red-100 rounded px-2 py-1">
                      반송사유: {h.reason}
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}

        <DialogFooter>
          <Button variant="outline" onClick={onClose}>
            닫기
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

const EMPTY_COUNTS: WorksAllCounts = {
  deptCounts: {},
  partCounts: {},
  statusCounts: {},
  totalCount: 0,
};

const WorksAllMain: React.FC = () => {
  const [pageItems, setPageItems] = useState<WorksAllListItem[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [counts, setCounts] = useState<WorksAllCounts>(EMPTY_COUNTS);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedDept, setSelectedDept] = useState<WorksAllDepartment>("영업");
  const [selectedPart, setSelectedPart] = useState<string>(ALL_PART);
  const [selectedStatus, setSelectedStatus] = useState<string>(ALL_STATUS);
  const [startDueDt, setStartDueDt] = useState<string>(
    () => getDefaultDueDtRange().start,
  );
  const [endDueDt, setEndDueDt] = useState<string>(
    () => getDefaultDueDtRange().end,
  );
  const [seenNegotiations, setSeenNegotiations] = useState<Set<string>>(
    new Set(),
  );
  const [seenLoaded, setSeenLoaded] = useState(false);
  const [detailItem, setDetailItem] = useState<WorksAllListItem | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [historyWorkOrderNo, setHistoryWorkOrderNo] = useState<string | null>(null);

  useEffect(() => {
    try {
      const raw = localStorage.getItem(SEEN_NEGOTIATIONS_STORAGE_KEY);
      if (raw) setSeenNegotiations(new Set(JSON.parse(raw)));
    } catch {
      // localStorage 접근 불가 시 무시
    } finally {
      setSeenLoaded(true);
    }
  }, []);

  // 협의 확인 목록은 Set 참조가 매번 바뀌므로, effect 의존성 비교용으로 문자열 키를 따로 둔다
  const seenKey = useMemo(
    () => Array.from(seenNegotiations).sort().join(","),
    [seenNegotiations],
  );

  const markNegotiationSeen = (workOrderNo: string) => {
    setSeenNegotiations((prev) => {
      if (prev.has(workOrderNo)) return prev;
      const next = new Set(prev);
      next.add(workOrderNo);
      try {
        localStorage.setItem(
          SEEN_NEGOTIATIONS_STORAGE_KEY,
          JSON.stringify(Array.from(next)),
        );
      } catch {
        // localStorage 접근 불가 시 무시
      }
      return next;
    });
  };

  const handleRowClick = (item: WorksAllListItem) => {
    if (item.status === "협의") markNegotiationSeen(item.workOrderNo);
    setDetailItem(item);
  };

  const toDetailItem = (item: WorksAllListItem): WorkDetailItem => ({
    workOrderNo: item.workOrderNo,
    title: item.title,
    department: item.department,
    part: item.part,
    workType: item.workType,
    managerName: item.managerName,
    status: item.status,
    approvalStatus: item.approvalStatus,
    regDt: item.regDt,
    dueDt: item.dueDt,
  });

  const handleDeptChange = (dept: WorksAllDepartment) => {
    setSelectedDept(dept);
    setSelectedPart(ALL_PART);
    setSelectedStatus(ALL_STATUS);
  };

  const handleResetDueDt = () => {
    const defaultRange = getDefaultDueDtRange();
    setStartDueDt(defaultRange.start);
    setEndDueDt(defaultRange.end);
  };

  // 파트 탭 순서 = 카운트 응답이 내려준 순서(파트명 기준 정렬)를 그대로 사용
  const partOrder = useMemo(() => Object.keys(counts.partCounts), [counts.partCounts]);

  const partTotal = useMemo(
    () => Object.values(counts.partCounts).reduce((sum, n) => sum + n, 0),
    [counts.partCounts],
  );

  // 필터 조건이 바뀌면 1페이지로 복귀 (이미 1페이지면 상태 변경이 없어 재조회도 발생하지 않음)
  useEffect(() => {
    setCurrentPage(1);
  }, [selectedDept, selectedPart, selectedStatus, startDueDt, endDueDt]);

  // 조회 결과가 줄어들어 현재 페이지가 범위를 벗어나면 마지막 페이지로 보정
  useEffect(() => {
    if (currentPage > totalPages) setCurrentPage(totalPages);
  }, [currentPage, totalPages]);

  // 목록: 부서/파트/상태/마감일/페이지가 바뀔 때마다 해당 페이지만 서버에 재조회한다
  useEffect(() => {
    if (!seenLoaded) return;
    let cancelled = false;

    setLoading(true);
    setError(null);
    fetchWorksAllList({
      dept: selectedDept,
      part: selectedPart,
      status: selectedStatus,
      startDueDt,
      endDueDt,
      seenNegotiations: Array.from(seenNegotiations),
      page: currentPage,
      size: PAGE_SIZE,
    })
      .then((data) => {
        if (cancelled) return;
        setPageItems(data.content);
        setTotalElements(data.totalElements);
        setTotalPages(Math.max(1, data.totalPages));
      })
      .catch((e: any) => {
        if (cancelled) return;
        setError(e.message ?? "목록 조회 중 오류가 발생했습니다.");
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedDept, selectedPart, selectedStatus, startDueDt, endDueDt, currentPage, seenLoaded, seenKey]);

  // 탭 건수 배지: 목록과 별개로 가벼운 집계 쿼리만 다시 조회한다 (전체 목록을 내려받지 않음)
  useEffect(() => {
    if (!seenLoaded) return;
    let cancelled = false;

    fetchWorksAllCounts({
      dept: selectedDept,
      part: selectedPart,
      startDueDt,
      endDueDt,
      seenNegotiations: Array.from(seenNegotiations),
    })
      .then((data) => {
        if (!cancelled) setCounts(data);
      })
      .catch(() => {
        // 배지 카운트 실패는 목록 표시에 영향 주지 않으므로 조용히 무시
      });

    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedDept, selectedPart, startDueDt, endDueDt, seenLoaded, seenKey]);

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

  const formatDate = (dt: string) => {
    if (!dt || dt.length < 8) return dt;
    return `${dt.slice(0, 4)}-${dt.slice(4, 6)}-${dt.slice(6, 8)}`;
  };

  const renderTableBody = () => {
    if (loading) {
      return (
        <TableRow>
          <TableCell colSpan={8} className="text-center py-10 text-slate-400">
            <span className="inline-block w-5 h-5 border-2 border-slate-300 border-t-[var(--sidebar-bg)] rounded-full animate-spin mr-2 align-middle" />
            불러오는 중...
          </TableCell>
        </TableRow>
      );
    }

    if (pageItems.length === 0) {
      return (
        <TableRow>
          <TableCell colSpan={8} className="text-center py-10 text-slate-400">
            등록된 업무지시서가 없습니다.
          </TableCell>
        </TableRow>
      );
    }

    return pageItems.map((item) => (
      <TableRow
        key={item.workOrderNo}
        onClick={() => handleRowClick(item)}
        className="cursor-pointer hover:bg-slate-50 transition-colors"
      >
        <TableCell className="text-center text-slate-500">
          {item.workOrderNo}
        </TableCell>
        <TableCell className="font-medium text-[var(--sidebar-bg)]">
          {item.title}
        </TableCell>
        <TableCell className="text-center">{item.part}</TableCell>
        <TableCell className="text-center">{item.managerName}</TableCell>
        <TableCell className="text-center">
          <DotBadge label={item.status} color={STATUS_COLOR[item.status]} />
        </TableCell>
        <TableCell className="text-center">{formatDate(item.regDt)}</TableCell>
        <TableCell className="text-center">{formatDate(item.dueDt)}</TableCell>
        <TableCell onClick={(e) => e.stopPropagation()} className="text-center cursor-default">
          <ApprovalHistoryButton onOpen={() => setHistoryWorkOrderNo(item.workOrderNo)} />
        </TableCell>
      </TableRow>
    ));
  };

  return (
    <div className="w-full bg-[var(--page-bg)] p-6 rounded-xl border border-slate-200">
      <div className="flex items-center gap-3 mb-4 px-4 py-3 rounded-lg bg-slate-50 border border-slate-200 flex-wrap">
        <span className="flex items-center gap-1.5 text-sm font-medium text-slate-600 shrink-0">
          <CalendarDays className="size-4" />
          마감일 조회
        </span>
        <input
          type="date"
          value={startDueDt}
          onChange={(e) => setStartDueDt(e.target.value)}
          className="px-2 py-1.5 rounded-md border border-slate-300 text-sm text-slate-700 bg-white"
        />
        <span className="text-slate-400">~</span>
        <input
          type="date"
          value={endDueDt}
          onChange={(e) => setEndDueDt(e.target.value)}
          className="px-2 py-1.5 rounded-md border border-slate-300 text-sm text-slate-700 bg-white"
        />
        <Button variant="outline" size="sm" onClick={handleResetDueDt}>
          초기화
        </Button>
      </div>

      <div className="flex items-center justify-end mb-6 gap-4 flex-wrap">
        <div className="flex items-center gap-2 flex-wrap">
          <button
            onClick={() => setSelectedStatus(ALL_STATUS)}
            className={`px-3 py-1.5 rounded-full text-sm font-medium transition-colors ${
              selectedStatus === ALL_STATUS
                ? "bg-[var(--sidebar-bg)] text-white"
                : "bg-slate-100 text-slate-600 hover:bg-slate-200"
            }`}
          >
            전체 {counts.totalCount}
          </button>
          {STATUS_FILTERS.map((status) => (
            <button
              key={status}
              onClick={() => setSelectedStatus(status)}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm font-medium transition-colors ${
                selectedStatus === status
                  ? "bg-[var(--sidebar-bg)] text-white"
                  : "bg-slate-100 text-slate-600 hover:bg-slate-200"
              }`}
            >
              <span
                className="w-1.5 h-1.5 rounded-full"
                style={{ backgroundColor: STATUS_COLOR[status] }}
              />
              {status} {counts.statusCounts[status] ?? 0}
            </button>
          ))}
        </div>
      </div>

      {error && (
        <div className="bg-red-50 text-red-600 p-4 rounded-lg mb-4 text-sm border border-red-200">
          {error}
        </div>
      )}

      <Tabs
        value={selectedDept}
        onValueChange={(v: string) => handleDeptChange(v as WorksAllDepartment)}
        className="mb-4"
      >
        <TabsList variant="line" className="h-10">
          {DEPARTMENTS.map((dept) => {
            const Icon = DEPARTMENT_ICON[dept];
            return (
              <TabsTrigger
                key={dept}
                value={dept}
                className="gap-2 text-base px-3"
              >
                <Icon className="size-4" />
                {dept}
                <CountBadge
                  count={counts.deptCounts[dept] ?? 0}
                  active={selectedDept === dept}
                />
              </TabsTrigger>
            );
          })}
        </TabsList>
      </Tabs>

      <div className="flex items-center gap-2 mb-6 flex-wrap">
        <button
          onClick={() => setSelectedPart(ALL_PART)}
          className={`flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm font-medium transition-colors ${
            selectedPart === ALL_PART
              ? "bg-[var(--sidebar-bg)] text-white"
              : "bg-slate-100 text-slate-600 hover:bg-slate-200"
          }`}
        >
          전체 {partTotal}
        </button>
        {partOrder.map((part) => (
          <button
            key={part}
            onClick={() => setSelectedPart(part)}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm font-medium transition-colors ${
              selectedPart === part
                ? "bg-[var(--sidebar-bg)] text-white"
                : "bg-slate-100 text-slate-600 hover:bg-slate-200"
            }`}
          >
            {part} {counts.partCounts[part]}
          </button>
        ))}
      </div>

      <div className="rounded-lg border border-slate-200 bg-white shadow-sm overflow-hidden">
        <Table>
          <TableHeader className="bg-slate-50">
            <TableRow>
              <TableHead className="w-[110px] text-center text-[var(--sidebar-bg)] font-bold">
                번호
              </TableHead>
              <TableHead className="text-[var(--sidebar-bg)] font-bold">
                제목
              </TableHead>
              <TableHead className="w-[110px] text-center text-[var(--sidebar-bg)] font-bold">
                파트
              </TableHead>
              <TableHead className="w-[100px] text-center text-[var(--sidebar-bg)] font-bold">
                담당자
              </TableHead>
              <TableHead className="w-[100px] text-center text-[var(--sidebar-bg)] font-bold">
                상태
              </TableHead>
              <TableHead className="w-[120px] text-center text-[var(--sidebar-bg)] font-bold">
                등록일
              </TableHead>
              <TableHead className="w-[120px] text-center text-[var(--sidebar-bg)] font-bold">
                마감일
              </TableHead>
              <TableHead className="w-[120px] text-center text-[var(--sidebar-bg)] font-bold">
                결재이력
              </TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>{renderTableBody()}</TableBody>
        </Table>
      </div>

      <div className="text-right text-xs text-slate-400 mt-2">
        총 {totalElements}건
      </div>

      {totalElements > 0 && (
        <Pagination className="mt-4">
          <PaginationContent>
            <PaginationItem>
              <PaginationPrevious
                href="#"
                onClick={(e) => {
                  e.preventDefault();
                  if (currentPage > 1) setCurrentPage(currentPage - 1);
                }}
                aria-disabled={currentPage === 1}
                className={
                  currentPage === 1 ? "pointer-events-none opacity-40" : ""
                }
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
              ),
            )}
            <PaginationItem>
              <PaginationNext
                href="#"
                onClick={(e) => {
                  e.preventDefault();
                  if (currentPage < totalPages) setCurrentPage(currentPage + 1);
                }}
                aria-disabled={currentPage === totalPages}
                className={
                  currentPage === totalPages
                    ? "pointer-events-none opacity-40"
                    : ""
                }
              />
            </PaginationItem>
          </PaginationContent>
        </Pagination>
      )}

      <WorkDetailModal
        key={detailItem?.workOrderNo}
        item={detailItem ? toDetailItem(detailItem) : null}
        onClose={() => setDetailItem(null)}
        footer={
          <Button variant="outline" onClick={() => setDetailItem(null)}>
            닫기
          </Button>
        }
      />

      <ApprovalHistoryDialog
        workOrderNo={historyWorkOrderNo}
        onClose={() => setHistoryWorkOrderNo(null)}
      />
    </div>
  );
};

export default WorksAllMain;
