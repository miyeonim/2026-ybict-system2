import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router";
import { useAuthContext } from "@routes/common/jwt/AuthContext";
import type {
  WorksRequestMyListItem,
  WorksRequestMyTabKey,
  WorksRequestMyDetail,
} from "./WorksRequestMyDto";
import type {
  WorksMyCandidate,
  WorksMyReturnTarget,
  WorksMyCreateRequestPrefill,
} from "@routes/works_my/WorksMyDto";
import {
  fetchWorkRequestsMyList,
  fetchRequestNextCandidates,
  approveWorkRequest,
  fetchRequestReturnTargets,
  returnWorkRequest,
} from "~/hooks/work_request_my/WorksRequestMyController";
import CreateWorkRequestDialog from "@routes/works_my/CreateWorkRequestDialog";

import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { Textarea } from "@/components/ui/textarea";
import {
  ClipboardCheck,
  ListChecks,
  CheckCircle,
  XCircle,
  History as HistoryIcon,
  FileEdit,
  Plus,
} from "lucide-react";

// [2026-07-23] 작업 요청서는 영업배전시스템실이 받아서 처리하는 대상이라, 그 소속 사람 본인은
// 등록할 수 없다(백엔드 WorkMyServiceImpl.SALES_DISTRIBUTION_SYSTEM_SOSOK_HAN과 동일한 값 - 반대로
// 작업지시서(WorksMyMain.tsx) 직접등록은 이 소속만 가능). 서버도 동일하게 거부하지만 버튼을 숨겨
// 혼란을 줄인다.
const SALES_DISTRIBUTION_SYSTEM_SOSOK_HAN = "영업배전시스템실";

const TAB_CONFIG: { key: WorksRequestMyTabKey; label: string; icon: React.ElementType }[] = [
  { key: "결재대기", label: "결재대기", icon: ClipboardCheck },
  { key: "전체", label: "전체", icon: ListChecks },
];

// 요청서는 아직 협의/완료 개념이 없어, 작업지시서(MY)처럼 여러 탭에 걸치지 않고 하나로만 분류한다.
const resolveTab = (item: WorksRequestMyListItem): WorksRequestMyTabKey =>
  item.approvalStatus === "결재 대기" ? "결재대기" : "전체";

const formatDate = (dt: string | null) => {
  if (!dt || dt.length < 8) return dt ?? "-";
  return `${dt.slice(0, 4)}-${dt.slice(4, 6)}-${dt.slice(6, 8)}`;
};

const formatDateTime = (dt: string | null) => {
  if (!dt || dt.length < 14) return formatDate(dt);
  return `${dt.slice(0, 4)}-${dt.slice(4, 6)}-${dt.slice(6, 8)} ${dt.slice(8, 10)}:${dt.slice(10, 12)}:${dt.slice(12, 14)}`;
};

const DotBadge: React.FC<{ label: string; color: string }> = ({ label, color }) => (
  <span className="inline-flex items-center gap-1.5 px-2 py-1 rounded-full text-xs font-medium bg-slate-50 border border-slate-200 text-slate-600">
    <span className="w-1.5 h-1.5 rounded-full" style={{ backgroundColor: color }} />
    {label}
  </span>
);

const CountBadge: React.FC<{ count: number; active: boolean }> = ({ count, active }) => (
  <span
    className={`inline-flex items-center justify-center min-w-[20px] px-1.5 py-0.5 rounded-full text-xs font-semibold ${
      active ? "bg-[var(--sidebar-bg)] text-white" : "bg-slate-200 text-slate-600"
    }`}
  >
    {count}
  </span>
);

const ApprovalHistoryButton: React.FC<{
  history: WorksRequestMyListItem["approvalHistory"];
  onOpen: () => void;
}> = ({ history, onOpen }) => {
  if (!history || history.length === 0) {
    return <span className="text-xs text-slate-300">-</span>;
  }
  return (
    <Button variant="outline" size="sm" className="h-7 px-2.5 text-xs" onClick={onOpen}>
      <HistoryIcon className="size-3.5" />
      {history.length}건 보기
    </Button>
  );
};

const ApprovalHistoryDialog: React.FC<{
  open: boolean;
  history: WorksRequestMyListItem["approvalHistory"];
  onClose: () => void;
}> = ({ open, history, onClose }) => (
  <Dialog open={open} onOpenChange={(v) => !v && onClose()}>
    <DialogContent className="max-w-md">
      <DialogTitle className="text-center">결재이력</DialogTitle>
      <div className="flex flex-col gap-3 py-2 max-h-96 overflow-y-auto">
        {(history ?? []).map((h, idx) => (
          <div key={idx} className="flex items-start gap-3">
            <div className="flex flex-col items-center pt-0.5">
              <span className="flex items-center justify-center w-5 h-5 rounded-full bg-slate-100 text-slate-500 text-[11px] font-semibold">
                {idx + 1}
              </span>
              {idx < history.length - 1 && <span className="w-px flex-1 bg-slate-200 mt-1" />}
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
              {h.reason && (
                <div className="text-xs text-red-500 mt-1 bg-red-50 border border-red-100 rounded px-2 py-1">
                  {h.reason}
                </div>
              )}
            </div>
          </div>
        ))}
      </div>
      <DialogFooter>
        <Button variant="outline" onClick={onClose}>
          닫기
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
);

// 103(한전 IT부서 직원) 담당자를 지정하며 102를 승인하는 다이얼로그
const ApproveDialog: React.FC<{
  open: boolean;
  workRequestNo: string | null;
  onClose: () => void;
  onConfirmed: () => void;
}> = ({ open, workRequestNo, onClose, onConfirmed }) => {
  const { user } = useAuthContext();
  const [candidates, setCandidates] = useState<WorksMyCandidate[]>([]);
  const [selectedSabun, setSelectedSabun] = useState("");
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!open || !workRequestNo || !user) return;
    setSelectedSabun("");
    setError(null);
    setLoading(true);
    fetchRequestNextCandidates(workRequestNo, user.userEmpno)
      .then((res) => setCandidates(res.candidates))
      .catch((e: any) => setError(e.message ?? "다음 단계 담당자 후보 조회에 실패했습니다."))
      .finally(() => setLoading(false));
  }, [open, workRequestNo, user]);

  const handleConfirm = async () => {
    if (!workRequestNo || !user) return;
    const next = candidates.find((c) => c.sabun === selectedSabun);
    if (!next) return;
    setSubmitting(true);
    setError(null);
    try {
      await approveWorkRequest(workRequestNo, user, next);
      onConfirmed();
    } catch (e: any) {
      setError(e.message ?? "결재 승인 처리 중 오류가 발생했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="max-w-md">
        <DialogTitle>요청서 접수(IT담당자) 지정</DialogTitle>

        {error && (
          <div className="text-xs text-red-500 bg-red-50 px-3 py-2 rounded-md border border-red-200">
            {error}
          </div>
        )}

        {loading ? (
          <div className="text-center py-8 text-sm text-slate-400">
            <span className="inline-block w-4 h-4 border-2 border-slate-300 border-t-[var(--sidebar-bg)] rounded-full animate-spin mr-2 align-middle" />
            후보를 불러오는 중...
          </div>
        ) : (
          <div className="flex flex-col gap-2 py-2">
            <p className="text-xs text-slate-500">
              승인 후 이 요청서를 접수할 한전 IT부서 담당자를 선택하세요.
            </p>
            <div className="flex flex-col gap-1.5 max-h-64 overflow-y-auto">
              {candidates.length === 0 && (
                <p className="text-xs text-slate-400">지정 가능한 담당자 후보가 없습니다.</p>
              )}
              {candidates.map((c) => (
                <button
                  key={c.sabun}
                  onClick={() => setSelectedSabun(c.sabun)}
                  className={`text-left px-3 py-2 rounded-md border text-sm transition-colors ${
                    selectedSabun === c.sabun
                      ? "border-[var(--sidebar-bg)] bg-[var(--sidebar-bg)]/5"
                      : "border-slate-200 hover:border-slate-300"
                  }`}
                >
                  <span className="font-medium text-slate-800">{c.name}</span>
                  <span className="text-slate-400 ml-2 text-xs">
                    {c.roleNm} · {c.sabun}
                  </span>
                </button>
              ))}
            </div>
          </div>
        )}

        <DialogFooter>
          <Button variant="outline" onClick={onClose} disabled={submitting}>
            취소
          </Button>
          <Button
            className="bg-[var(--sidebar-bg)] hover:bg-[var(--sidebar-bg)]/90 text-white"
            disabled={submitting || loading || !selectedSabun}
            onClick={handleConfirm}
          >
            <CheckCircle className="size-4" />
            승인 확정
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

// 반려(반송): 요청서는 102의 이전 단계가 100(요청자 작성) 하나뿐이라 대상 선택이 단순하다
const ReturnDialog: React.FC<{
  open: boolean;
  workRequestNo: string | null;
  onClose: () => void;
  onConfirmed: () => void;
}> = ({ open, workRequestNo, onClose, onConfirmed }) => {
  const { user } = useAuthContext();
  const [targets, setTargets] = useState<WorksMyReturnTarget[]>([]);
  const [selectedActId, setSelectedActId] = useState("");
  const [reason, setReason] = useState("");
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!open || !workRequestNo || !user) return;
    setReason("");
    setSelectedActId("");
    setError(null);
    setLoading(true);
    fetchRequestReturnTargets(workRequestNo, user.userEmpno)
      .then((res) => {
        setTargets(res.targets);
        setSelectedActId(res.targets[0]?.actId ?? "");
      })
      .catch((e: any) => setError(e.message ?? "반송 대상 단계 조회에 실패했습니다."))
      .finally(() => setLoading(false));
  }, [open, workRequestNo, user]);

  const handleConfirm = async () => {
    if (!workRequestNo || !user || !reason.trim() || !selectedActId) return;
    setSubmitting(true);
    setError(null);
    try {
      await returnWorkRequest(workRequestNo, user, reason.trim(), selectedActId);
      onConfirmed();
    } catch (e: any) {
      setError(e.message ?? "반송 처리 중 오류가 발생했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="max-w-md">
        <DialogTitle>이전 단계로 반송</DialogTitle>

        {error && (
          <div className="text-xs text-red-500 bg-red-50 px-3 py-2 rounded-md border border-red-200">
            {error}
          </div>
        )}

        {loading ? (
          <div className="text-center py-8 text-sm text-slate-400">
            <span className="inline-block w-4 h-4 border-2 border-slate-300 border-t-[var(--sidebar-bg)] rounded-full animate-spin mr-2 align-middle" />
            반송 대상 단계를 불러오는 중...
          </div>
        ) : (
          <div className="flex flex-col gap-2 py-2">
            <p className="text-xs text-slate-500">되돌아갈 단계를 선택하세요.</p>
            <div className="flex flex-col gap-1.5 max-h-48 overflow-y-auto">
              {targets.map((t) => (
                <button
                  key={t.actId}
                  onClick={() => setSelectedActId(t.actId)}
                  className={`text-left px-3 py-2 rounded-md border text-sm transition-colors ${
                    selectedActId === t.actId
                      ? "border-[var(--sidebar-bg)] bg-[var(--sidebar-bg)]/5"
                      : "border-slate-200 hover:border-slate-300"
                  }`}
                >
                  <span className="font-medium text-slate-800">{t.actIdNm}</span>
                  {t.name && (
                    <span className="text-slate-400 ml-2 text-xs">
                      {t.name} · {t.sabun}
                    </span>
                  )}
                </button>
              ))}
            </div>
          </div>
        )}

        <div className="flex flex-col gap-1.5 py-2">
          <p className="text-xs text-slate-500">반송 사유를 입력하세요.</p>
          <Textarea
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            placeholder="반송 사유를 입력하세요..."
            className="min-h-24"
          />
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={onClose} disabled={submitting}>
            취소
          </Button>
          <Button
            variant="outline"
            className="text-red-500 border-red-200 hover:bg-red-50"
            disabled={submitting || loading || !reason.trim() || !selectedActId}
            onClick={handleConfirm}
          >
            <XCircle className="size-4" />
            반송 확정
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

// 작업 요청서(MY): 작업지시서(MY)와 동일한 화면 구조(카드형 탭 + 테이블 + 결재이력)를 쓰되,
// its_real_work_report(요청서)만 보여준다. 한전 사람에게만 좌측 메뉴가 노출된다(menu.ts 참고).
// 102(요청서 승인)는 승인/반려 둘 다 지원한다. 103(요청서 접수)은 승인 버튼이 없고, 대신 접수
// 담당자가 "작업지시서 작성"을 누르면 작업지시서(MY)로 이동해 이 요청서 내용으로 미리 채워진
// 등록 다이얼로그가 열린다 - 그 지시서를 실제로 등록해야 103이 완료 처리된다(반려는 여전히 가능).
const WorksRequestMyMain: React.FC = () => {
  const [list, setList] = useState<WorksRequestMyListItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedTab, setSelectedTab] = useState<WorksRequestMyTabKey>("결재대기");
  const [detailWorkRequestNo, setDetailWorkRequestNo] = useState<string | null>(null);
  const [approveDialogOpen, setApproveDialogOpen] = useState(false);
  const [returnDialogOpen, setReturnDialogOpen] = useState(false);
  const [historyDialogItem, setHistoryDialogItem] = useState<WorksRequestMyListItem | null>(null);
  const [createRequestDialogOpen, setCreateRequestDialogOpen] = useState(false);
  const navigate = useNavigate();
  const { user } = useAuthContext();
  const canCreateWorkRequest = user?.depTitle !== SALES_DISTRIBUTION_SYSTEM_SOSOK_HAN;

  const handleCreateWorkOrder = (detail: WorksRequestMyDetail) => {
    const prefill: WorksMyCreateRequestPrefill = {
      sourceRequestNo: detail.workRequestNo,
      changeTitle: detail.changeTitle,
      changeReason: detail.changeReason ?? "",
      systemCd: detail.systemCd ?? "",
      serviceType: detail.serviceType ?? "",
      hopeFinishedDt: detail.expectedDt ?? "",
    };
    navigate("/works_my", { state: { prefillFromRequest: prefill } });
  };

  const loadList = async () => {
    if (!user) return;
    setLoading(true);
    setError(null);
    try {
      setList(await fetchWorkRequestsMyList(user.userEmpno));
    } catch (e: any) {
      setError(e.message ?? "목록 조회 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadList();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

  const itemsWithTabs = useMemo(
    () => list.map((item) => ({ item, tab: resolveTab(item) })),
    [list],
  );

  const tabCounts = useMemo(() => {
    const counts: Record<WorksRequestMyTabKey, number> = { 결재대기: 0, 전체: list.length };
    itemsWithTabs.forEach(({ tab }) => {
      if (tab === "결재대기") counts.결재대기 += 1;
    });
    return counts;
  }, [itemsWithTabs, list.length]);

  const filteredList = useMemo(
    () =>
      selectedTab === "전체"
        ? list
        : itemsWithTabs.filter(({ tab }) => tab === selectedTab).map(({ item }) => item),
    [itemsWithTabs, list, selectedTab],
  );

  const handleDecisionConfirmed = () => {
    setApproveDialogOpen(false);
    setReturnDialogOpen(false);
    setDetailWorkRequestNo(null);
    loadList();
  };

  const renderTableBody = () => {
    if (loading) {
      return (
        <TableRow>
          <TableCell colSpan={5} className="text-center py-10 text-slate-400">
            <span className="inline-block w-5 h-5 border-2 border-slate-300 border-t-[var(--sidebar-bg)] rounded-full animate-spin mr-2 align-middle" />
            불러오는 중...
          </TableCell>
        </TableRow>
      );
    }

    if (filteredList.length === 0) {
      return (
        <TableRow>
          <TableCell colSpan={5} className="text-center py-10 text-slate-400">
            해당 분류의 작업 요청서가 없습니다.
          </TableCell>
        </TableRow>
      );
    }

    return filteredList.map((item) => (
      <TableRow
        key={item.workRequestNo}
        onClick={() => setDetailWorkRequestNo(item.workRequestNo)}
        className="cursor-pointer hover:bg-slate-50 transition-colors"
      >
        <TableCell className="text-center text-slate-500">{item.workRequestNo}</TableCell>
        <TableCell className="font-medium text-[var(--sidebar-bg)]">{item.title}</TableCell>
        <TableCell className="text-center">
          <DotBadge
            label={item.approvalStatus}
            color={item.approvalStatus === "결재 대기" ? "#F59E0B" : "#94A3B8"}
          />
        </TableCell>
        <TableCell className="text-center">{formatDate(item.dueDt)}</TableCell>
        <TableCell onClick={(e) => e.stopPropagation()} className="text-center cursor-default">
          <ApprovalHistoryButton history={item.approvalHistory} onOpen={() => setHistoryDialogItem(item)} />
        </TableCell>
      </TableRow>
    ));
  };

  return (
    <div className="w-full bg-[var(--page-bg)] p-6 rounded-xl border border-slate-200">
      <div className="flex items-start justify-between mb-6 gap-3 flex-wrap">
        <p className="text-sm text-slate-500">한전 담당자 · 내가 관련된 작업 요청서를 관리합니다.</p>
        {canCreateWorkRequest && (
          <Button
            className="bg-[var(--sidebar-bg)] hover:bg-[var(--sidebar-bg)]/90 text-white"
            onClick={() => setCreateRequestDialogOpen(true)}
          >
            <Plus className="size-4" />
            작업 요청서 등록
          </Button>
        )}
      </div>

      {error && (
        <div className="bg-red-50 text-red-600 p-4 rounded-lg mb-4 text-sm border border-red-200">
          {error}
        </div>
      )}

      <div className="grid grid-cols-2 gap-4 mb-6">
        {TAB_CONFIG.map(({ key, label }) => (
          <button
            key={key}
            onClick={() => setSelectedTab(key)}
            className={`relative text-left p-4 rounded-xl border bg-white transition-colors ${
              selectedTab === key ? "border-[var(--sidebar-bg)] shadow-sm" : "border-slate-200 hover:border-slate-300"
            }`}
          >
            <div className="text-3xl font-bold text-[var(--sidebar-bg)]">{tabCounts[key]}</div>
            <div className="text-sm text-slate-500 mt-1">{label}</div>
          </button>
        ))}
      </div>

      <div className="flex gap-2 mb-4">
        {TAB_CONFIG.map(({ key, label, icon: Icon }) => (
          <button
            key={key}
            onClick={() => setSelectedTab(key)}
            className={`flex items-center gap-2 px-3 h-10 rounded-md text-sm font-medium transition-colors ${
              selectedTab === key
                ? "bg-[var(--sidebar-bg)] text-white"
                : "bg-white border border-slate-200 text-slate-600 hover:border-slate-300"
            }`}
          >
            <Icon className="size-4" />
            {label}
            <CountBadge count={tabCounts[key]} active={selectedTab === key} />
          </button>
        ))}
      </div>

      <div className="rounded-lg border border-slate-200 bg-white shadow-sm overflow-hidden">
        <Table>
          <TableHeader className="bg-slate-50">
            <TableRow>
              <TableHead className="w-[140px] text-center text-[var(--sidebar-bg)] font-bold">번호</TableHead>
              <TableHead className="text-[var(--sidebar-bg)] font-bold">제목</TableHead>
              <TableHead className="w-[110px] text-center text-[var(--sidebar-bg)] font-bold">결재</TableHead>
              <TableHead className="w-[120px] text-center text-[var(--sidebar-bg)] font-bold">희망완료일</TableHead>
              <TableHead className="w-[140px] text-center text-[var(--sidebar-bg)] font-bold">결재이력</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>{renderTableBody()}</TableBody>
        </Table>
      </div>

      <div className="text-right text-xs text-slate-400 mt-2">총 {filteredList.length}건</div>

      <CreateWorkRequestDialog
        open={!!detailWorkRequestNo}
        mode="view"
        workRequestNo={detailWorkRequestNo}
        onClose={() => setDetailWorkRequestNo(null)}
        renderFooter={(detail) =>
          detail?.myTurn ? (
            detail.currentActId === "103" ? (
              <>
                <Button
                  variant="outline"
                  className="text-red-500 border-red-200 hover:bg-red-50"
                  onClick={() => setReturnDialogOpen(true)}
                >
                  <XCircle className="size-4" />
                  반려
                </Button>
                <Button
                  className="bg-[var(--sidebar-bg)] hover:bg-[var(--sidebar-bg)]/90 text-white"
                  onClick={() => handleCreateWorkOrder(detail)}
                >
                  <FileEdit className="size-4" />
                  작업지시서 작성
                </Button>
              </>
            ) : (
              <>
                <Button
                  variant="outline"
                  className="text-red-500 border-red-200 hover:bg-red-50"
                  onClick={() => setReturnDialogOpen(true)}
                >
                  <XCircle className="size-4" />
                  반려
                </Button>
                <Button
                  className="bg-[var(--sidebar-bg)] hover:bg-[var(--sidebar-bg)]/90 text-white"
                  onClick={() => setApproveDialogOpen(true)}
                >
                  <CheckCircle className="size-4" />
                  승인
                </Button>
              </>
            )
          ) : (
            <Button variant="outline" onClick={() => setDetailWorkRequestNo(null)}>
              닫기
            </Button>
          )
        }
      />
      <ApproveDialog
        open={approveDialogOpen}
        workRequestNo={detailWorkRequestNo}
        onClose={() => setApproveDialogOpen(false)}
        onConfirmed={handleDecisionConfirmed}
      />
      <ReturnDialog
        open={returnDialogOpen}
        workRequestNo={detailWorkRequestNo}
        onClose={() => setReturnDialogOpen(false)}
        onConfirmed={handleDecisionConfirmed}
      />
      <ApprovalHistoryDialog
        open={!!historyDialogItem}
        history={historyDialogItem?.approvalHistory ?? []}
        onClose={() => setHistoryDialogItem(null)}
      />
      <CreateWorkRequestDialog
        open={createRequestDialogOpen}
        onClose={() => setCreateRequestDialogOpen(false)}
        onCreated={() => {
          setCreateRequestDialogOpen(false);
          loadList();
        }}
      />
    </div>
  );
};

export default WorksRequestMyMain;
