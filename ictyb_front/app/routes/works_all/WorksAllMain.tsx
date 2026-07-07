import { useEffect, useMemo, useState } from "react";
import type {
  WorksAllListItem,
  WorksAllStatus,
  WorksAllApprovalStatus,
  WorksAllDepartment,
} from "./WorksAllDto";
import { fetchWorksAllList } from "~/hooks/work_all/WorksAllController";
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
import { CircleDollarSign, Zap, Wrench, CalendarDays } from "lucide-react";

// 상태별 뱃지 색상 (app.css의 --status-new/progress/done 토큰 재사용)
const STATUS_COLOR: Record<WorksAllStatus, string> = {
  접수: "var(--status-new)",
  "처리 중": "var(--status-progress)",
  완료: "var(--status-done)",
  협의: "#94A3B8", // slate-400
};

const APPROVAL_COLOR: Record<WorksAllApprovalStatus, string> = {
  "결재 완료": "var(--status-done)",
  미요청: "#94A3B8",
};

const DEPARTMENTS: WorksAllDepartment[] = ["영업", "배전", "기술"];

const DEPARTMENT_ICON: Record<WorksAllDepartment, React.ElementType> = {
  영업: CircleDollarSign,
  배전: Zap,
  기술: Wrench,
};

const ALL_PART = "전체";

const ALL_STATUS = "전체";
const STATUS_FILTERS: WorksAllStatus[] = ["접수", "처리 중", "완료", "협의"];
const SEEN_NEGOTIATIONS_STORAGE_KEY = "worksAll_seenNegotiations";
const PAGE_SIZE = 10;

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

const WorksAllMain: React.FC = () => {
  const [list, setList] = useState<WorksAllListItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedDept, setSelectedDept] = useState<WorksAllDepartment>("영업");
  const [selectedPart, setSelectedPart] = useState<string>(ALL_PART);
  const [selectedStatus, setSelectedStatus] = useState<string>(ALL_STATUS);
  const [startDueDt, setStartDueDt] = useState<string>("");
  const [endDueDt, setEndDueDt] = useState<string>("");
  const [seenNegotiations, setSeenNegotiations] = useState<Set<string>>(
    new Set(),
  );
  const [detailItem, setDetailItem] = useState<WorksAllListItem | null>(null);
  const [currentPage, setCurrentPage] = useState(1);

  useEffect(() => {
    try {
      const raw = localStorage.getItem(SEEN_NEGOTIATIONS_STORAGE_KEY);
      if (raw) setSeenNegotiations(new Set(JSON.parse(raw)));
    } catch {
      // localStorage 접근 불가 시 무시
    }
  }, []);

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

  // 마감일 범위 필터 (시작/종료 중 하나만 입력해도 동작, YYYY-MM-DD -> YYYYMMDD 변환 후 비교)
  const dueDtFilteredList = useMemo(() => {
    if (!startDueDt && !endDueDt) return list;
    const start = startDueDt.replaceAll("-", "");
    const end = endDueDt.replaceAll("-", "");
    return list.filter((item) => {
      if (start && item.dueDt < start) return false;
      if (end && item.dueDt > end) return false;
      return true;
    });
  }, [list, startDueDt, endDueDt]);

  const handleResetDueDt = () => {
    setStartDueDt("");
    setEndDueDt("");
  };

  const deptCounts = useMemo(() => {
    const counts: Record<WorksAllDepartment, number> = {
      영업: 0,
      배전: 0,
      기술: 0,
    };
    dueDtFilteredList.forEach((item) => {
      counts[item.department] += 1;
    });
    return counts;
  }, [dueDtFilteredList]);

  const deptList = useMemo(
    () =>
      dueDtFilteredList.filter((item) => item.department === selectedDept),
    [dueDtFilteredList, selectedDept],
  );

  const partCounts = useMemo(() => {
    const counts: Record<string, number> = {};
    deptList.forEach((item) => {
      counts[item.part] = (counts[item.part] ?? 0) + 1;
    });
    return counts;
  }, [deptList]);

  const partOrder = useMemo(() => {
    const seen: string[] = [];
    deptList.forEach((item) => {
      if (!seen.includes(item.part)) seen.push(item.part);
    });
    return seen;
  }, [deptList]);

  const filteredList = useMemo(
    () =>
      selectedPart === ALL_PART
        ? deptList
        : deptList.filter((item) => item.part === selectedPart),
    [deptList, selectedPart],
  );

  // 협의는 사용자가 아직 확인하지 않은 건만 집계/표시 (확인한 협의는 카운트와 목록에서 제외)
  const statusCounts = useMemo(() => {
    const counts: Record<string, number> = {};
    filteredList.forEach((item) => {
      if (item.status === "협의" && seenNegotiations.has(item.workOrderNo)) {
        return;
      }
      counts[item.status] = (counts[item.status] ?? 0) + 1;
    });
    return counts;
  }, [filteredList, seenNegotiations]);

  const statusFilteredList = useMemo(() => {
    if (selectedStatus === ALL_STATUS) return filteredList;
    if (selectedStatus === "협의") {
      return filteredList.filter(
        (item) =>
          item.status === "협의" && !seenNegotiations.has(item.workOrderNo),
      );
    }
    return filteredList.filter((item) => item.status === selectedStatus);
  }, [filteredList, selectedStatus, seenNegotiations]);

  const totalPages = Math.max(
    1,
    Math.ceil(statusFilteredList.length / PAGE_SIZE),
  );

  const pagedList = useMemo(
    () =>
      statusFilteredList.slice(
        (currentPage - 1) * PAGE_SIZE,
        currentPage * PAGE_SIZE,
      ),
    [statusFilteredList, currentPage],
  );

  // 필터 조건이 바뀌면 1페이지로 복귀
  useEffect(() => {
    setCurrentPage(1);
  }, [selectedDept, selectedPart, selectedStatus, startDueDt, endDueDt]);

  // 필터링 결과가 줄어들어 현재 페이지가 범위를 벗어나면 마지막 페이지로 보정
  useEffect(() => {
    if (currentPage > totalPages) setCurrentPage(totalPages);
  }, [currentPage, totalPages]);

  const loadList = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchWorksAllList();
      setList(data);
    } catch (e: any) {
      setError(e.message ?? "목록 조회 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadList();
  }, []);

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
          <TableCell colSpan={9} className="text-center py-10 text-slate-400">
            <span className="inline-block w-5 h-5 border-2 border-slate-300 border-t-[var(--sidebar-bg)] rounded-full animate-spin mr-2 align-middle" />
            불러오는 중...
          </TableCell>
        </TableRow>
      );
    }

    if (statusFilteredList.length === 0) {
      return (
        <TableRow>
          <TableCell colSpan={9} className="text-center py-10 text-slate-400">
            등록된 업무지시서가 없습니다.
          </TableCell>
        </TableRow>
      );
    }

    return pagedList.map((item) => (
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
        <TableCell className="text-center">{item.workType}</TableCell>
        <TableCell className="text-center">{item.part}</TableCell>
        <TableCell className="text-center">{item.managerName}</TableCell>
        <TableCell className="text-center">
          <DotBadge
            label={item.approvalStatus}
            color={APPROVAL_COLOR[item.approvalStatus]}
          />
        </TableCell>
        <TableCell className="text-center">
          <DotBadge label={item.status} color={STATUS_COLOR[item.status]} />
        </TableCell>
        <TableCell className="text-center">{formatDate(item.regDt)}</TableCell>
        <TableCell className="text-center">{formatDate(item.dueDt)}</TableCell>
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
            전체 {filteredList.length}
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
              {status} {statusCounts[status] ?? 0}
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
                  count={deptCounts[dept]}
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
          전체 {deptList.length}
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
            {part} {partCounts[part]}
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
                유형
              </TableHead>
              <TableHead className="w-[110px] text-center text-[var(--sidebar-bg)] font-bold">
                파트
              </TableHead>
              <TableHead className="w-[100px] text-center text-[var(--sidebar-bg)] font-bold">
                담당자
              </TableHead>
              <TableHead className="w-[110px] text-center text-[var(--sidebar-bg)] font-bold">
                결재
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
            </TableRow>
          </TableHeader>
          <TableBody>{renderTableBody()}</TableBody>
        </Table>
      </div>

      <div className="text-right text-xs text-slate-400 mt-2">
        총 {statusFilteredList.length}건
      </div>

      {statusFilteredList.length > 0 && (
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
    </div>
  );
};

export default WorksAllMain;
