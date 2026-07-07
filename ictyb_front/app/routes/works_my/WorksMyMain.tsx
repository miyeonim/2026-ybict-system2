import { useEffect, useMemo, useRef, useState } from "react";
import type {
  WorksMyListItem,
  WorksMyStatus,
  WorksMyApprovalStatus,
  WorksMyTabKey,
  WorksMyCandidate,
  WorksMyCreateOptions,
  WorksMyCreateRequest,
  WorksMyDetail,
} from "./WorksMyDto";
import {
  fetchWorksMyList,
  fetchNextCandidates,
  submitApproval,
  submitReturn,
  fetchCreateOptions,
  fetchInitialApproverCandidates,
  createWorkOrder,
} from "~/hooks/work_my/WorksMyController";

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
  Dialog,
  DialogContent,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { Textarea } from "@/components/ui/textarea";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import WorkDetailModal, { POST_WORK_RESULT_ACT_IDS } from "@routes/common/components/WorkDetailModal";
import type { WorkDetailItem, ModalTab } from "@routes/common/components/WorkDetailModal";
import {
  ClipboardCheck,
  MessageSquareText,
  Activity,
  CheckCircle2,
  CheckCircle,
  XCircle,
  Plus,
  Paperclip,
  X,
  History as HistoryIcon,
} from "lucide-react";

const STATUS_COLOR: Record<WorksMyStatus, string> = {
  접수: "var(--status-new)",
  "처리 중": "var(--status-progress)",
  완료: "var(--status-done)",
  협의: "#94A3B8",
};

const APPROVAL_COLOR: Record<WorksMyApprovalStatus, string> = {
  "결재 대기": "#F59E0B",
  "결재 완료": "var(--status-done)",
  미요청: "#94A3B8",
};

const TAB_CONFIG: {
  key: WorksMyTabKey;
  label: string;
  icon: React.ElementType;
}[] = [
  { key: "결재대기", label: "결재대기", icon: ClipboardCheck },
  { key: "피드백", label: "피드백", icon: MessageSquareText },
  { key: "진행중", label: "진행중", icon: Activity },
  { key: "처리내역", label: "처리내역", icon: CheckCircle2 },
];

/** 결재대기와 피드백은 동시에 해당될 수 있어(예: 내 결재 차례인데 협의도 걸린 건) 한 건이 여러 탭에 함께 표시된다. */
const resolveTabs = (item: WorksMyListItem): WorksMyTabKey[] => {
  const tabs: WorksMyTabKey[] = [];
  if (item.approvalStatus === "결재 대기") tabs.push("결재대기");
  if (item.status === "협의") tabs.push("피드백");
  if (tabs.length > 0) return tabs;
  if (item.status === "완료") return ["처리내역"];
  return ["진행중"];
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

const formatDate = (dt: string) => {
  if (!dt || dt.length < 8) return dt;
  return `${dt.slice(0, 4)}-${dt.slice(4, 6)}-${dt.slice(6, 8)}`;
};

// yyyyMMddHHmmss -> yyyy-MM-dd HH:mm:ss (결재이력 표시용, 시분초까지)
const formatDateTime = (dt: string) => {
  if (!dt || dt.length < 14) return formatDate(dt);
  return `${dt.slice(0, 4)}-${dt.slice(4, 6)}-${dt.slice(6, 8)} ${dt.slice(8, 10)}:${dt.slice(10, 12)}:${dt.slice(12, 14)}`;
};

// 각 항목의 결재이력을 보여주는 팝업을 여는 버튼 (목록 화면에서는 버튼만 표시)
const ApprovalHistoryButton: React.FC<{
  history: WorksMyListItem["approvalHistory"];
  onOpen: () => void;
}> = ({ history, onOpen }) => {
  if (!history || history.length === 0) {
    return <span className="text-xs text-slate-300">-</span>;
  }
  return (
    <Button
      variant="outline"
      size="sm"
      className="h-7 px-2.5 text-xs"
      onClick={onOpen}
    >
      <HistoryIcon className="size-3.5" />
      {history.length}건 보기
    </Button>
  );
};

// 결재이력을 처리 순서대로(일렬로) 보여주는 팝업
const ApprovalHistoryDialog: React.FC<{
  open: boolean;
  history: WorksMyListItem["approvalHistory"];
  onClose: () => void;
}> = ({ open, history, onClose }) => (
  <Dialog open={open} onOpenChange={(v) => !v && onClose()}>
    <DialogContent className="max-w-md">
      <DialogTitle>결재이력</DialogTitle>
      <div className="flex flex-col gap-3 py-2 max-h-96 overflow-y-auto">
        {(history ?? []).map((h, idx) => (
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
                      : "text-emerald-600 font-medium ml-1"
                  }
                >
                  {h.signLabel}
                </span>
              </div>
              <div className="text-xs text-slate-400 mt-0.5">{formatDateTime(h.regDt)}</div>
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

const toDetailItem = (item: WorksMyListItem): WorkDetailItem => ({
  workOrderNo: item.workOrderNo,
  title: item.title,
  department: item.department,
  part: item.part,
  status: item.status,
  approvalStatus: item.approvalStatus,
  dueDt: item.dueDt,
});

// 승인: 다음 단계 담당자를 지정하는 다이얼로그
const ApproveDialog: React.FC<{
  open: boolean;
  workOrderNo: string | null;
  onClose: () => void;
  onConfirmed: (workOrderNo: string) => void;
}> = ({ open, workOrderNo, onClose, onConfirmed }) => {
  const [candidates, setCandidates] = useState<WorksMyCandidate[]>([]);
  const [selectedSabun, setSelectedSabun] = useState<string>("");
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!open || !workOrderNo) return;
    setSelectedSabun("");
    setError(null);
    setLoading(true);
    fetchNextCandidates(workOrderNo)
      .then((res) => setCandidates(res.candidates))
      .catch((e: any) =>
        setError(e.message ?? "다음 단계 담당자 후보 조회에 실패했습니다."),
      )
      .finally(() => setLoading(false));
  }, [open, workOrderNo]);

  const handleConfirm = async () => {
    if (!workOrderNo) return;
    const isFinalStage = !loading && candidates.length === 0;
    const next = candidates.find((c) => c.sabun === selectedSabun);
    if (!isFinalStage && !next) return;

    setSubmitting(true);
    setError(null);
    try {
      await submitApproval(workOrderNo, next ?? { sabun: "", name: "", roleNm: "" });
      onConfirmed(workOrderNo);
    } catch (e: any) {
      setError(e.message ?? "결재 승인 처리 중 오류가 발생했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="max-w-md">
        <DialogTitle>다음 단계 담당자 지정</DialogTitle>

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
        ) : candidates.length === 0 ? (
          <p className="text-sm text-slate-500 py-4">
            마지막 결재 단계입니다. 승인하면 이 건은 완료 처리됩니다.
          </p>
        ) : (
          <div className="flex flex-col gap-2 py-2">
            <p className="text-xs text-slate-500">
              승인 후 이 건을 처리할 다음 담당자를 선택하세요.
            </p>
            <div className="flex flex-col gap-1.5 max-h-64 overflow-y-auto">
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
            disabled={
              submitting || loading || (candidates.length > 0 && !selectedSabun)
            }
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

// 반려(반송): 이전 단계로 되돌리며 사유를 입력받는 다이얼로그
const ReturnDialog: React.FC<{
  open: boolean;
  workOrderNo: string | null;
  onClose: () => void;
  onConfirmed: (workOrderNo: string) => void;
}> = ({ open, workOrderNo, onClose, onConfirmed }) => {
  const [reason, setReason] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!open) return;
    setReason("");
    setError(null);
  }, [open]);

  const handleConfirm = async () => {
    if (!workOrderNo || !reason.trim()) return;
    setSubmitting(true);
    setError(null);
    try {
      await submitReturn(workOrderNo, reason.trim());
      onConfirmed(workOrderNo);
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
            disabled={submitting || !reason.trim()}
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

const formatBytes = (bytes: number) => {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
};

const formatDateYmd = (d: Date) =>
  `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;

// 처리기간(일)을 오늘 기준으로 더해 완료예정일(yyyy-MM-dd)을 계산
const calcExpectedFinishedDt = (periodDays: string): string | null => {
  const n = Number(periodDays);
  if (!periodDays.trim() || !Number.isFinite(n) || n < 0) return null;
  const d = new Date();
  d.setDate(d.getDate() + n);
  return formatDateYmd(d);
};

const EMPTY_CREATE_FORM: WorksMyCreateRequest = {
  changeTitle: "",
  changeReason: "",
  serviceType: "",
  workType: "",
  workGubun: "",
  workLevel: "",
  workPeriod: "",
  expectedFinishedDt: "",
  targetDepCd: "",
  initialApproverSabun: "",
  initialApproverName: "",
};

// 업무지시서 등록: 지시서 작성 + 최초 결재자(한전 파트장) 지정
const CreateWorkOrderDialog: React.FC<{
  open: boolean;
  onClose: () => void;
  onCreated: () => void;
}> = ({ open, onClose, onCreated }) => {
  const [form, setForm] = useState<WorksMyCreateRequest>(EMPTY_CREATE_FORM);
  const [options, setOptions] = useState<WorksMyCreateOptions | null>(null);
  const [candidates, setCandidates] = useState<WorksMyCandidate[]>([]);
  const [attachments, setAttachments] = useState<File[]>([]);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (!open) return;
    setForm(EMPTY_CREATE_FORM);
    setAttachments([]);
    setError(null);
    setLoading(true);
    Promise.all([fetchCreateOptions(), fetchInitialApproverCandidates()])
      .then(([opts, cands]) => {
        setOptions(opts);
        setCandidates(cands);
      })
      .catch((e: any) =>
        setError(e.message ?? "등록 폼 정보를 불러오지 못했습니다."),
      )
      .finally(() => setLoading(false));
  }, [open]);

  const update = (field: keyof WorksMyCreateRequest, value: string) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleFileAdd = (newFiles: FileList | null) => {
    if (!newFiles) return;
    setAttachments((prev) => [...prev, ...Array.from(newFiles)]);
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  const handleFileRemove = (index: number) => {
    setAttachments((prev) => prev.filter((_, i) => i !== index));
  };

  const handleSubmit = async () => {
    if (!form.changeTitle.trim() || !form.targetDepCd || !form.initialApproverSabun)
      return;
    setSubmitting(true);
    setError(null);
    try {
      await createWorkOrder(form, attachments);
      onCreated();
    } catch (e: any) {
      setError(e.message ?? "업무지시서 등록 중 오류가 발생했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  const codeSelect = (
    label: string,
    field: keyof WorksMyCreateRequest,
    codeOptions: WorksMyCreateOptions["serviceTypeOptions"] | undefined,
  ) => (
    <div className="flex flex-col gap-1.5">
      <Label>{label}</Label>
      <Select
        value={form[field]}
        onValueChange={(v: string) => update(field, v)}
      >
        <SelectTrigger className="w-full">
          <SelectValue placeholder="선택하세요" />
        </SelectTrigger>
        <SelectContent>
          {(codeOptions ?? []).map((opt) => (
            <SelectItem key={opt.code} value={opt.code}>
              {opt.label}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  );

  return (
    <Dialog open={open} onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="max-w-6xl sm:max-w-6xl max-h-[95vh] overflow-y-auto">
        <DialogTitle>업무지시서 등록</DialogTitle>

        {error && (
          <div className="text-xs text-red-500 bg-red-50 px-3 py-2 rounded-md border border-red-200">
            {error}
          </div>
        )}

        {loading ? (
          <div className="text-center py-8 text-sm text-slate-400">
            <span className="inline-block w-4 h-4 border-2 border-slate-300 border-t-[var(--sidebar-bg)] rounded-full animate-spin mr-2 align-middle" />
            불러오는 중...
          </div>
        ) : (
          <div className="flex flex-col gap-4 py-2">
            <div className="flex flex-col gap-1.5">
              <Label>제목</Label>
              <Input
                value={form.changeTitle}
                onChange={(e) => update("changeTitle", e.target.value)}
                placeholder="지시 제목을 입력하세요"
              />
            </div>

            <div className="flex flex-col gap-1.5">
              <Label>지시내용</Label>
              <Textarea
                value={form.changeReason}
                onChange={(e) => update("changeReason", e.target.value)}
                placeholder="지시 내용을 입력하세요"
                className="min-h-60"
              />
            </div>

            {codeSelect("대상 부서 (이 일을 처리할 KDN 부서)", "targetDepCd", options?.departmentOptions)}

            <div className="grid grid-cols-2 gap-3">
              {codeSelect("서비스유형", "serviceType", options?.serviceTypeOptions)}
              {codeSelect("작업유형", "workType", options?.workTypeOptions)}
              {codeSelect("작업구분", "workGubun", options?.workGubunOptions)}
              {codeSelect("작업레벨", "workLevel", options?.workLevelOptions)}
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div className="flex flex-col gap-1.5">
                <Label>처리기간(일)</Label>
                <Input
                  type="number"
                  min="0"
                  value={form.workPeriod}
                  onChange={(e) => {
                    const value = e.target.value;
                    if (Number(value) < 0) return;
                    const calculated = calcExpectedFinishedDt(value);
                    setForm((prev) => ({
                      ...prev,
                      workPeriod: value,
                      ...(calculated ? { expectedFinishedDt: calculated } : {}),
                    }));
                  }}
                  placeholder="예: 7"
                />
              </div>
              <div className="flex flex-col gap-1.5">
                <Label>완료예정일</Label>
                <Input
                  type="date"
                  value={form.expectedFinishedDt}
                  onChange={(e) => update("expectedFinishedDt", e.target.value)}
                  max="9999-12-31"
                />
              </div>
            </div>

            <div className="flex flex-col gap-1.5">
              <Label>첨부파일</Label>
              <Button
                type="button"
                variant="outline"
                onClick={() => fileInputRef.current?.click()}
                className="w-fit"
              >
                <Paperclip className="size-4" />
                파일 선택
              </Button>
              <input
                ref={fileInputRef}
                type="file"
                multiple
                className="hidden"
                onChange={(e) => handleFileAdd(e.target.files)}
              />
              {attachments.length > 0 && (
                <ul className="flex flex-col gap-1.5 mt-1">
                  {attachments.map((file, idx) => (
                    <li
                      key={idx}
                      className="flex items-center gap-2 bg-slate-50 border border-slate-200 rounded-md px-3 py-2 text-sm"
                    >
                      <span className="flex-1 truncate text-slate-700">{file.name}</span>
                      <span className="text-slate-400 text-xs whitespace-nowrap">
                        {formatBytes(file.size)}
                      </span>
                      <button
                        type="button"
                        onClick={() => handleFileRemove(idx)}
                        className="text-slate-400 hover:text-red-500 transition-colors"
                      >
                        <X className="size-3.5" />
                      </button>
                    </li>
                  ))}
                </ul>
              )}
            </div>

            <div className="flex flex-col gap-1.5">
              <Label>최초 결재자 (한전 파트장)</Label>
              <div className="flex flex-col gap-1.5 max-h-40 overflow-y-auto">
                {candidates.length === 0 && (
                  <p className="text-xs text-slate-400">
                    지정 가능한 결재자 후보가 없습니다.
                  </p>
                )}
                {candidates.map((c) => (
                  <button
                    key={c.sabun}
                    onClick={() => {
                      update("initialApproverSabun", c.sabun);
                      update("initialApproverName", c.name);
                    }}
                    className={`text-left px-3 py-2 rounded-md border text-sm transition-colors ${
                      form.initialApproverSabun === c.sabun
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
          </div>
        )}

        <DialogFooter>
          <Button variant="outline" onClick={onClose} disabled={submitting}>
            취소
          </Button>
          <Button
            className="bg-[var(--sidebar-bg)] hover:bg-[var(--sidebar-bg)]/90 text-white"
            disabled={
              submitting ||
              loading ||
              !form.changeTitle.trim() ||
              !form.targetDepCd ||
              !form.initialApproverSabun
            }
            onClick={handleSubmit}
          >
            <Plus className="size-4" />
            등록
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

const WorksMyMain: React.FC = () => {
  const [list, setList] = useState<WorksMyListItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedTab, setSelectedTab] = useState<WorksMyTabKey>("결재대기");
  const [detailItem, setDetailItem] = useState<WorksMyListItem | null>(null);
  const [currentDetail, setCurrentDetail] = useState<WorksMyDetail | null>(null);
  const [currentModalTab, setCurrentModalTab] = useState<ModalTab>("기본정보");
  const [approveDialogOpen, setApproveDialogOpen] = useState(false);
  const [returnDialogOpen, setReturnDialogOpen] = useState(false);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [historyDialogItem, setHistoryDialogItem] = useState<WorksMyListItem | null>(null);

  const loadList = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchWorksMyList();
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

  const itemsWithTabs = useMemo(
    () => list.map((item) => ({ item, tabs: resolveTabs(item) })),
    [list],
  );

  const tabCounts = useMemo(() => {
    const counts: Record<WorksMyTabKey, number> = {
      결재대기: 0,
      피드백: 0,
      진행중: 0,
      처리내역: 0,
    };
    itemsWithTabs.forEach(({ tabs }) => {
      tabs.forEach((tab) => {
        counts[tab] += 1;
      });
    });
    return counts;
  }, [itemsWithTabs]);

  const filteredList = useMemo(
    () =>
      itemsWithTabs
        .filter(({ tabs }) => tabs.includes(selectedTab))
        .map(({ item }) => item),
    [itemsWithTabs, selectedTab],
  );

  const handleRowClick = (item: WorksMyListItem) => {
    setDetailItem(item);
  };

  // 결재 처리(승인/반송/조치사항 제출) 완료 후: 목록을 다시 불러와 모든 다이얼로그를 닫는다.
  const handleDecisionConfirmed = (_workOrderNo: string) => {
    setApproveDialogOpen(false);
    setReturnDialogOpen(false);
    setDetailItem(null);
    setCurrentDetail(null);
    setCurrentModalTab("기본정보");
    loadList();
  };

  // 작업결과 보고(109) 이후 단계는 결재자가 작업완료결재 탭에서 조치사항을 확인해야만 승인/반려 버튼을 누를 수 있다.
  const requiresWorkResultReview =
    !!currentDetail?.currentActId && POST_WORK_RESULT_ACT_IDS.includes(currentDetail.currentActId);
  const decisionBlockedByTab = requiresWorkResultReview && currentModalTab !== "작업완료결재";

  const renderTableBody = () => {
    if (loading) {
      return (
        <TableRow>
          <TableCell colSpan={7} className="text-center py-10 text-slate-400">
            <span className="inline-block w-5 h-5 border-2 border-slate-300 border-t-[var(--sidebar-bg)] rounded-full animate-spin mr-2 align-middle" />
            불러오는 중...
          </TableCell>
        </TableRow>
      );
    }

    if (filteredList.length === 0) {
      return (
        <TableRow>
          <TableCell colSpan={7} className="text-center py-10 text-slate-400">
            해당 분류의 업무지시서가 없습니다.
          </TableCell>
        </TableRow>
      );
    }

    return filteredList.map((item) => (
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
        <TableCell className="text-center">
          {item.department}·{item.part}
        </TableCell>
        <TableCell className="text-center">
          <DotBadge
            label={item.approvalStatus}
            color={APPROVAL_COLOR[item.approvalStatus]}
          />
        </TableCell>
        <TableCell className="text-center">
          <DotBadge label={item.status} color={STATUS_COLOR[item.status]} />
        </TableCell>
        <TableCell className="text-center">{formatDate(item.dueDt)}</TableCell>
        <TableCell onClick={(e) => e.stopPropagation()} className="text-center cursor-default">
          <ApprovalHistoryButton
            history={item.approvalHistory}
            onOpen={() => setHistoryDialogItem(item)}
          />
        </TableCell>
      </TableRow>
    ));
  };

  return (
    <div className="w-full bg-[var(--page-bg)] p-6 rounded-xl border border-slate-200">
      <div className="flex items-start justify-between mb-6 gap-3 flex-wrap">
        <div>
          <p className="text-sm text-slate-500">
            한전 담당자 · 나와 관련된 작업지시서를 관리합니다.
          </p>
        </div>
        <Button
          className="bg-[var(--sidebar-bg)] hover:bg-[var(--sidebar-bg)]/90 text-white"
          onClick={() => setCreateDialogOpen(true)}
        >
          <Plus className="size-4" />
          업무지시서 등록
        </Button>
      </div>

      {error && (
        <div className="bg-red-50 text-red-600 p-4 rounded-lg mb-4 text-sm border border-red-200">
          {error}
        </div>
      )}

      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
        {TAB_CONFIG.map(({ key, label }) => (
          <button
            key={key}
            onClick={() => setSelectedTab(key)}
            className={`text-left p-4 rounded-xl border bg-white transition-colors ${
              selectedTab === key
                ? "border-[var(--sidebar-bg)] shadow-sm"
                : "border-slate-200 hover:border-slate-300"
            }`}
          >
            <div className="text-3xl font-bold text-[var(--sidebar-bg)]">
              {tabCounts[key]}
            </div>
            <div className="text-sm text-slate-500 mt-1">{label}</div>
          </button>
        ))}
      </div>

      <Tabs
        value={selectedTab}
        onValueChange={(v: string) => setSelectedTab(v as WorksMyTabKey)}
        className="mb-6"
      >
        <TabsList variant="line" className="h-10">
          {TAB_CONFIG.map(({ key, label, icon: Icon }) => (
            <TabsTrigger key={key} value={key} className="gap-2 text-base px-3">
              <Icon className="size-4" />
              {label}
              <CountBadge count={tabCounts[key]} active={selectedTab === key} />
            </TabsTrigger>
          ))}
        </TabsList>
      </Tabs>

      <div className="rounded-lg border border-slate-200 bg-white shadow-sm overflow-hidden">
        <Table>
          <TableHeader className="bg-slate-50">
            <TableRow>
              <TableHead className="w-[120px] text-center text-[var(--sidebar-bg)] font-bold">
                번호
              </TableHead>
              <TableHead className="text-[var(--sidebar-bg)] font-bold">
                제목
              </TableHead>
              <TableHead className="w-[140px] text-center text-[var(--sidebar-bg)] font-bold">
                부서·파트
              </TableHead>
              <TableHead className="w-[110px] text-center text-[var(--sidebar-bg)] font-bold">
                결재
              </TableHead>
              <TableHead className="w-[100px] text-center text-[var(--sidebar-bg)] font-bold">
                상태
              </TableHead>
              <TableHead className="w-[120px] text-center text-[var(--sidebar-bg)] font-bold">
                마감일
              </TableHead>
              <TableHead className="w-[220px] text-[var(--sidebar-bg)] font-bold">
                결재이력
              </TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>{renderTableBody()}</TableBody>
        </Table>
      </div>

      <div className="text-right text-xs text-slate-400 mt-2">
        총 {filteredList.length}건
      </div>

      <WorkDetailModal
        key={detailItem?.workOrderNo}
        item={detailItem ? toDetailItem(detailItem) : null}
        onClose={() => setDetailItem(null)}
        onDetailLoaded={setCurrentDetail}
        onWorkResultSubmitted={handleDecisionConfirmed}
        onTabChange={setCurrentModalTab}
        onDiscussionCreated={loadList}
        footer={
          detailItem?.approvalStatus === "결재 대기" ? (
            currentDetail?.currentActId === "109" ? (
              <>
                <Button
                  size="lg"
                  variant="outline"
                  className="h-11 px-6 text-base text-red-500 border-red-200 hover:bg-red-50"
                  onClick={() => setReturnDialogOpen(true)}
                >
                  <XCircle className="size-5" />
                  반려
                </Button>
                <span className="text-xs text-slate-400 self-center">
                  작업완료 결재 탭에서 조치사항을 작성해 제출하세요.
                </span>
              </>
            ) : (
              <>
                <Button
                  size="lg"
                  variant="outline"
                  className="h-11 px-6 text-base text-red-500 border-red-200 hover:bg-red-50"
                  disabled={decisionBlockedByTab}
                  title={
                    decisionBlockedByTab
                      ? "작업완료 결재 탭에서 조치사항을 확인한 후 반려할 수 있습니다."
                      : undefined
                  }
                  onClick={() => setReturnDialogOpen(true)}
                >
                  <XCircle className="size-5" />
                  반려
                </Button>
                <Button
                  size="lg"
                  className="h-11 px-6 text-base bg-[var(--sidebar-bg)] hover:bg-[var(--sidebar-bg)]/90 text-white"
                  disabled={decisionBlockedByTab}
                  title={
                    decisionBlockedByTab
                      ? "작업완료 결재 탭에서 조치사항을 확인한 후 승인할 수 있습니다."
                      : undefined
                  }
                  onClick={() => setApproveDialogOpen(true)}
                >
                  <CheckCircle className="size-5" />
                  승인
                </Button>
              </>
            )
          ) : (
            <Button variant="outline" onClick={() => setDetailItem(null)}>
              닫기
            </Button>
          )
        }
      />

      <ApproveDialog
        open={approveDialogOpen}
        workOrderNo={detailItem?.workOrderNo ?? null}
        onClose={() => setApproveDialogOpen(false)}
        onConfirmed={handleDecisionConfirmed}
      />
      <ReturnDialog
        open={returnDialogOpen}
        workOrderNo={detailItem?.workOrderNo ?? null}
        onClose={() => setReturnDialogOpen(false)}
        onConfirmed={handleDecisionConfirmed}
      />
      <ApprovalHistoryDialog
        open={!!historyDialogItem}
        history={historyDialogItem?.approvalHistory ?? []}
        onClose={() => setHistoryDialogItem(null)}
      />
      <CreateWorkOrderDialog
        open={createDialogOpen}
        onClose={() => setCreateDialogOpen(false)}
        onCreated={() => {
          setCreateDialogOpen(false);
          loadList();
        }}
      />
    </div>
  );
};

export default WorksMyMain;
