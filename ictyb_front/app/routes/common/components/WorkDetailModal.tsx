import { useEffect, useRef, useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import {
  MessageSquare,
  FileText,
  CheckSquare,
  Plus,
  Paperclip,
  ChevronUp,
  ChevronDown,
  User,
  X,
} from "lucide-react";
import { useAuthContext } from "@routes/common/jwt/AuthContext";
import {
  fetchDiscussions,
  createDiscussion,
  addComment,
  downloadCommentAttach,
} from "@hooks/work_opinion/WorkOpinionController";
import type { DiscussionItem } from "@hooks/work_opinion/type";
import {
  fetchWorkDetail,
  downloadWorkAttach,
  downloadWorkResultAttach,
  fetchNextCandidates,
  submitApproval,
} from "@hooks/work_my/WorksMyController";
import type { WorksMyDetail, WorksMyCandidate } from "@routes/works_my/WorksMyDto";

export type ModalTab = "기본정보" | "업무협의" | "작업완료결재";

/** 작업결과 보고(109) 이후 단계 - 결재자는 작업완료결재 탭에서 조치사항을 확인해야만 승인할 수 있다 */
export const POST_WORK_RESULT_ACT_IDS = ["111", "114"];

export interface WorkDetailItem {
  workOrderNo: string;
  title: string;
  department: string;
  part: string;
  status: string;
  approvalStatus: string;
  dueDt: string;
  workType?: string;
  managerName?: string;
  regDt?: string;
}

interface Props {
  item: WorkDetailItem | null;
  onClose: () => void;
  footer?: React.ReactNode;
  /** 상세 정보(등록 정보/첨부파일/작업결과) 로드 완료 시 호출됨 - 부모 화면에서 현재 단계 등을 참조할 때 사용 */
  onDetailLoaded?: (detail: WorksMyDetail | null) => void;
  /** 조치사항 제출(=109단계 승인) 완료 시 호출됨. 전달되지 않으면 조치사항 작성 폼은 표시되지 않는다. */
  onWorkResultSubmitted?: (workOrderNo: string) => void;
  /** 현재 활성 탭이 바뀔 때마다 호출됨 - 부모 화면에서 탭에 따라 승인 버튼 활성화 여부를 결정할 때 사용 */
  onTabChange?: (tab: ModalTab) => void;
  /** 새 업무협의(피드백) 등록 완료 시 호출됨 - 부모 화면의 목록/피드백 탭 상태를 새로고침할 때 사용 */
  onDiscussionCreated?: (workOrderNo: string) => void;
}

const formatBytes = (bytes: number) => {
  if (Number.isNaN(bytes)) return "";
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
};

const TABS: { key: ModalTab; label: string; icon: React.ElementType }[] = [
  { key: "기본정보", label: "기본 정보", icon: FileText },
  { key: "업무협의", label: "업무 협의", icon: MessageSquare },
  { key: "작업완료결재", label: "작업완료 결재", icon: CheckSquare },
];

const BasicInfoRow: React.FC<{ label: string; children: React.ReactNode }> = ({
  label,
  children,
}) => (
  <div className="flex items-center gap-3 py-2.5 border-b border-slate-100">
    <span className="w-20 shrink-0 text-sm text-slate-400">{label}</span>
    <span className="text-sm text-slate-700 truncate">{children}</span>
  </div>
);

// 작업완료 결재 탭: 조치사항(작업결과) 조회 및 (109단계 작업자 본인 차례일 때) 작성/제출
const WorkResultSection: React.FC<{
  detail: WorksMyDetail | null;
  onSubmitted?: (workOrderNo: string) => void;
}> = ({ detail, onSubmitted }) => {
  const [resultText, setResultText] = useState("");
  const [candidates, setCandidates] = useState<WorksMyCandidate[]>([]);
  const [selectedSabun, setSelectedSabun] = useState("");
  const [loadingCandidates, setLoadingCandidates] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [resultFiles, setResultFiles] = useState<File[]>([]);
  const resultFileInputRef = useRef<HTMLInputElement>(null);

  const canWrite =
    !!detail && !detail.workResult && detail.myTurn && detail.currentActId === "109" && !!onSubmitted;

  useEffect(() => {
    if (!canWrite || !detail) return;
    setLoadingCandidates(true);
    fetchNextCandidates(detail.workOrderNo)
      .then((res) => setCandidates(res.candidates))
      .catch(() => setError("다음 결재자 후보 조회에 실패했습니다."))
      .finally(() => setLoadingCandidates(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [canWrite, detail?.workOrderNo]);

  const handleSubmit = async () => {
    if (!detail || !resultText.trim()) return;
    const next = candidates.find((c) => c.sabun === selectedSabun);
    if (!next) {
      setError("다음 결재자를 선택하세요.");
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      await submitApproval(detail.workOrderNo, next, resultText.trim(), resultFiles);
      onSubmitted?.(detail.workOrderNo);
    } catch (e: any) {
      setError(e.message ?? "조치사항 제출 중 오류가 발생했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  const handleResultFileAdd = (newFiles: FileList | null) => {
    if (!newFiles) return;
    setResultFiles((prev) => [...prev, ...Array.from(newFiles)]);
  };

  const handleResultFileRemove = (index: number) => {
    setResultFiles((prev) => prev.filter((_, i) => i !== index));
  };

  if (detail?.workResult) {
    return (
      <div className="flex flex-col gap-2">
        <p className="text-sm font-semibold text-slate-700 flex items-center gap-2">
          <CheckSquare className="size-4" />
          조치사항
        </p>
        <div className="flex items-center gap-1.5 text-xs text-slate-400">
          <User className="size-3" />
          <span>{detail.workResult.workerName}</span>
          {detail.workResult.regDt && (
            <>
              <span>·</span>
              <span>{formatDetailDate(detail.workResult.regDt)}</span>
            </>
          )}
        </div>
        <p className="text-sm text-slate-700 whitespace-pre-wrap bg-slate-50 border border-slate-200 rounded-lg px-3 py-2">
          {detail.workResult.result}
        </p>
        {detail.workResult.attachments.length > 0 && (
          <ul className="flex flex-col gap-1.5 mt-1">
            {detail.workResult.attachments.map((att) => (
              <li
                key={att.seq}
                className="flex items-center gap-2 bg-slate-50 border border-slate-200 rounded-md px-3 py-2 text-sm"
              >
                <button
                  type="button"
                  onClick={() =>
                    downloadWorkResultAttach(detail.workOrderNo, att.seq, att.realFileName)
                  }
                  className="flex-1 truncate text-left text-[#3A6499] hover:underline"
                >
                  {att.realFileName}
                </button>
                <span className="text-slate-400 text-xs whitespace-nowrap">
                  {formatBytes(Number(att.fileSize))}
                </span>
              </li>
            ))}
          </ul>
        )}
      </div>
    );
  }

  if (!canWrite) {
    return (
      <div className="text-center py-10 text-sm text-slate-400">
        작업완료 결재 정보가 없습니다.
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-3">
      <p className="text-sm font-semibold text-slate-700">조치사항 작성</p>
      <Textarea
        value={resultText}
        onChange={(e) => setResultText(e.target.value)}
        placeholder="조치사항(작업결과)을 입력하세요"
        className="min-h-32"
      />

      <div className="flex flex-col gap-1.5">
        <input
          ref={resultFileInputRef}
          type="file"
          multiple
          className="hidden"
          onChange={(e) => {
            handleResultFileAdd(e.target.files);
            if (resultFileInputRef.current) resultFileInputRef.current.value = "";
          }}
        />
        <Button
          type="button"
          variant="outline"
          onClick={() => resultFileInputRef.current?.click()}
          className="w-fit"
        >
          <Paperclip className="size-4" />
          첨부파일
        </Button>
        {resultFiles.length > 0 && (
          <ul className="flex flex-col gap-1.5">
            {resultFiles.map((file, idx) => (
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
                  onClick={() => handleResultFileRemove(idx)}
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
        <p className="text-xs text-slate-400">다음 결재자</p>
        {loadingCandidates ? (
          <p className="text-xs text-slate-400">불러오는 중...</p>
        ) : candidates.length === 0 ? (
          <p className="text-xs text-slate-400">지정 가능한 결재자 후보가 없습니다.</p>
        ) : (
          <div className="flex flex-col gap-1.5">
            {candidates.map((c) => (
              <button
                key={c.sabun}
                type="button"
                onClick={() => setSelectedSabun(c.sabun)}
                className={`text-left px-3 py-2 rounded-md border text-sm transition-colors ${
                  selectedSabun === c.sabun
                    ? "border-[var(--sidebar-bg)] bg-[var(--sidebar-bg)]/5"
                    : "border-slate-200 hover:border-slate-300"
                }`}
              >
                <span className="font-medium text-slate-800">{c.name}</span>
                <span className="text-slate-400 ml-2 text-xs">{c.roleNm}</span>
              </button>
            ))}
          </div>
        )}
      </div>

      {error && (
        <div className="text-xs text-red-500 bg-red-50 px-3 py-2 rounded-md border border-red-200">
          {error}
        </div>
      )}

      <Button
        className="bg-[var(--sidebar-bg)] hover:bg-[var(--sidebar-bg)]/90 text-white w-fit"
        disabled={submitting || !resultText.trim() || !selectedSabun}
        onClick={handleSubmit}
      >
        제출
      </Button>
    </div>
  );
};

const formatDetailDate = (dt: string) => (dt && dt.length >= 8 ? dt.slice(0, 8) : dt);

const WorkDetailModal: React.FC<Props> = ({
  item,
  onClose,
  footer,
  onDetailLoaded,
  onWorkResultSubmitted,
  onTabChange,
  onDiscussionCreated,
}) => {
  const { user } = useAuthContext();

  const [activeTab, setActiveTab] = useState<ModalTab>("기본정보");
  const [discussions, setDiscussions] = useState<DiscussionItem[]>([]);
  const [loadingDisc, setLoadingDisc] = useState(false);
  const [discError, setDiscError] = useState<string | null>(null);

  const [expandedIds, setExpandedIds] = useState<Set<string>>(new Set());
  const [commentInputs, setCommentInputs] = useState<Record<string, string>>({});
  const [commentFiles, setCommentFiles] = useState<Record<string, File[]>>({});
  const [pendingFileOpnId, setPendingFileOpnId] = useState<string | null>(null);
  const commentFileInputRef = useRef<HTMLInputElement>(null);
  const [submittingDisc, setSubmittingDisc] = useState(false);
  const [submittingCmnt, setSubmittingCmnt] = useState<Record<string, boolean>>({});

  const [newDiscTitle, setNewDiscTitle] = useState("");
  const [showNewDiscForm, setShowNewDiscForm] = useState(false);
  const [newDiscFiles, setNewDiscFiles] = useState<File[]>([]);
  const newDiscFileInputRef = useRef<HTMLInputElement>(null);

  const [detail, setDetail] = useState<WorksMyDetail | null>(null);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const [detailError, setDetailError] = useState<string | null>(null);

  const formatDate = (dt: string) => {
    if (!dt || dt.length < 8) return dt;
    if (dt.includes("-")) return dt.slice(0, 10);
    return `${dt.slice(0, 4)}-${dt.slice(4, 6)}-${dt.slice(6, 8)}`;
  };

  // 모달이 열릴 때 등록 정보(지시내용/첨부파일/작업결과 등) 상세 로드
  useEffect(() => {
    if (!item) {
      setDetail(null);
      onDetailLoaded?.(null);
      return;
    }
    setLoadingDetail(true);
    setDetailError(null);
    fetchWorkDetail(item.workOrderNo)
      .then((data) => {
        setDetail(data);
        onDetailLoaded?.(data);
        // 작업결과 보고(109) 이후 단계는 결재자가 조치사항을 반드시 확인하도록 작업완료결재 탭으로 이동시킨다.
        if (data.currentActId && POST_WORK_RESULT_ACT_IDS.includes(data.currentActId)) {
          setActiveTab("작업완료결재");
        }
      })
      .catch(() => setDetailError("상세 정보를 불러오지 못했습니다."))
      .finally(() => setLoadingDetail(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [item?.workOrderNo]);

  // 활성 탭이 바뀔 때마다 부모 화면에 알린다 (승인 버튼 활성화 여부 등에 사용)
  useEffect(() => {
    onTabChange?.(activeTab);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeTab]);

  // 모달이 열리고 업무협의 탭 진입 시 데이터 로드
  useEffect(() => {
    if (!item || activeTab !== "업무협의") return;
    setLoadingDisc(true);
    setDiscError(null);
    fetchDiscussions(item.workOrderNo)
      .then((data) => {
        setDiscussions(data);
        if (data.length > 0) {
          setExpandedIds(new Set([data[0].opnId]));
        }
      })
      .catch(() => setDiscError("협의 목록을 불러오지 못했습니다."))
      .finally(() => setLoadingDisc(false));
  }, [item?.workOrderNo, activeTab]);

  const toggleExpand = (id: string) => {
    setExpandedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const handleCreateDiscussion = async () => {
    if (!item || !newDiscTitle.trim() || submittingDisc) return;
    setSubmittingDisc(true);
    setDiscError(null);
    try {
      const created = await createDiscussion(
        {
          instrNo: item.workOrderNo,
          opnTitle: newDiscTitle.trim(),
          wrtrEmpno: user?.userEmpno ?? "",
          wrtrNm: user?.empNm ?? "",
          wrtrRoleNm: user?.depTitle ?? "",
        },
        newDiscFiles,
      );
      setDiscussions((prev) => [created, ...prev]);
      setExpandedIds((prev) => new Set([...prev, created.opnId]));
      setNewDiscTitle("");
      setNewDiscFiles([]);
      setShowNewDiscForm(false);
      onDiscussionCreated?.(item.workOrderNo);
    } catch (e) {
      setDiscError("협의 등록에 실패했습니다. (" + (e instanceof Error ? e.message : "오류") + ")");
    } finally {
      setSubmittingDisc(false);
    }
  };

  const handleNewDiscFileAdd = (newFiles: FileList | null) => {
    if (!newFiles) return;
    setNewDiscFiles((prev) => [...prev, ...Array.from(newFiles)]);
  };

  const handleNewDiscFileRemove = (index: number) => {
    setNewDiscFiles((prev) => prev.filter((_, i) => i !== index));
  };

  const handleAddComment = async (opnId: string) => {
    const text = commentInputs[opnId]?.trim();
    if (!text || submittingCmnt[opnId]) return;
    setSubmittingCmnt((prev) => ({ ...prev, [opnId]: true }));
    try {
      const comment = await addComment(
        {
          opnId,
          cmntCtt: text,
          wrtrEmpno: user?.userEmpno ?? "",
          wrtrNm: user?.empNm ?? "",
          wrtrRoleNm: user?.depTitle ?? "",
        },
        commentFiles[opnId] ?? [],
      );
      setDiscussions((prev) =>
        prev.map((d) =>
          d.opnId === opnId
            ? { ...d, comments: [...d.comments, comment] }
            : d,
        ),
      );
      setCommentInputs((prev) => ({ ...prev, [opnId]: "" }));
      setCommentFiles((prev) => ({ ...prev, [opnId]: [] }));
    } catch (e) {
      setDiscError("댓글 등록에 실패했습니다. (" + (e instanceof Error ? e.message : "오류") + ")");
    } finally {
      setSubmittingCmnt((prev) => ({ ...prev, [opnId]: false }));
    }
  };

  const handleCommentFileAdd = (opnId: string, newFiles: FileList | null) => {
    if (!newFiles) return;
    setCommentFiles((prev) => ({
      ...prev,
      [opnId]: [...(prev[opnId] ?? []), ...Array.from(newFiles)],
    }));
  };

  const handleCommentFileRemove = (opnId: string, index: number) => {
    setCommentFiles((prev) => ({
      ...prev,
      [opnId]: (prev[opnId] ?? []).filter((_, i) => i !== index),
    }));
  };

  const breadcrumb = item
    ? `${item.workOrderNo} · ${item.department}${item.part ? ` > ${item.part}` : ""}`
    : "";

  return (
    <Dialog open={!!item} onOpenChange={(open) => !open && onClose()}>
      <DialogContent
        showCloseButton={false}
        className="max-w-6xl sm:max-w-6xl max-h-[95vh] p-0 gap-0 overflow-hidden flex flex-col"
      >
        {/* 다크 헤더 */}
        <div className="bg-[var(--sidebar-bg)] px-5 py-4 flex items-start justify-between">
          <div className="flex-1 min-w-0">
            <p className="text-xs text-white/60 mb-1">{breadcrumb}</p>
            <DialogTitle className="text-base font-semibold text-white leading-snug">
              {item?.title}
            </DialogTitle>
          </div>
          <button
            onClick={onClose}
            className="text-white/70 hover:text-white ml-4 mt-0.5 shrink-0"
          >
            <X className="size-5" />
          </button>
        </div>

        {/* 탭 바 */}
        <div className="flex border-b border-slate-200 bg-white">
          {TABS.map(({ key, label, icon: Icon }) => (
            <button
              key={key}
              onClick={() => setActiveTab(key)}
              className={`flex items-center gap-1.5 px-5 py-3 text-sm font-medium border-b-2 transition-colors ${
                activeTab === key
                  ? "border-[var(--sidebar-bg)] text-[var(--sidebar-bg)]"
                  : "border-transparent text-slate-500 hover:text-slate-700"
              }`}
            >
              <Icon className="size-4" />
              {label}
            </button>
          ))}
        </div>

        {/* 콘텐츠 - 탭 전환 시 팝업 크기가 흔들리지 않도록 높이를 고정한다 */}
        <div className="h-[65vh] overflow-y-auto p-5">
          {/* 기본 정보 */}
          {activeTab === "기본정보" && item && (
            <div>
              <div className="grid grid-cols-2 gap-x-8">
              {(
                [
                  { label: "번호", value: item.workOrderNo },
                  { label: "부서", value: item.department },
                  { label: "파트", value: item.part },
                  item.workType ? { label: "유형", value: item.workType } : null,
                  item.managerName
                    ? { label: "담당자", value: item.managerName }
                    : null,
                  { label: "상태", value: item.status },
                  { label: "결재", value: item.approvalStatus },
                  item.regDt
                    ? { label: "등록일", value: formatDate(item.regDt) }
                    : null,
                  detail?.targetDepNm
                    ? { label: "대상부서", value: detail.targetDepNm }
                    : null,
                  detail?.serviceTypeLabel
                    ? { label: "서비스유형", value: detail.serviceTypeLabel }
                    : null,
                  detail?.workTypeLabel
                    ? { label: "작업유형", value: detail.workTypeLabel }
                    : null,
                  detail?.workGubunLabel
                    ? { label: "작업구분", value: detail.workGubunLabel }
                    : null,
                  detail?.workLevel
                    ? { label: "작업레벨", value: detail.workLevel }
                    : null,
                  detail?.workPeriod
                    ? { label: "처리기간", value: `${detail.workPeriod}일` }
                    : null,
                  { label: "완료예정일", value: formatDate(detail?.expectedFinishedDt || item.dueDt) },
                ] as ({ label: string; value: string } | null)[]
              )
                .filter(Boolean)
                .map((row) => (
                  <BasicInfoRow key={row!.label} label={row!.label}>
                    {row!.value}
                  </BasicInfoRow>
                ))}
              </div>

              {loadingDetail && (
                <div className="text-center py-6 text-sm text-slate-400">
                  <span className="inline-block w-4 h-4 border-2 border-slate-300 border-t-[var(--sidebar-bg)] rounded-full animate-spin mr-2 align-middle" />
                  불러오는 중...
                </div>
              )}

              {detailError && (
                <div className="text-xs text-red-500 bg-red-50 px-3 py-2 rounded-md border border-red-200 mt-3">
                  {detailError}
                </div>
              )}

              {detail?.changeReason && (
                <div className="mt-4">
                  <p className="text-sm text-slate-400 mb-1.5">지시내용</p>
                  <p className="text-sm text-slate-700 whitespace-pre-wrap bg-slate-50 border border-slate-200 rounded-lg px-3 py-2">
                    {detail.changeReason}
                  </p>
                </div>
              )}

              {detail && detail.attachments.length > 0 && (
                <div className="mt-4">
                  <p className="text-sm text-slate-400 mb-1.5 flex items-center gap-1">
                    <Paperclip className="size-3.5" />
                    첨부파일
                  </p>
                  <ul className="flex flex-col gap-1.5">
                    {detail.attachments.map((att) => (
                      <li
                        key={att.seq}
                        className="flex items-center gap-2 bg-slate-50 border border-slate-200 rounded-md px-3 py-2 text-sm"
                      >
                        <button
                          type="button"
                          onClick={() =>
                            downloadWorkAttach(detail.workOrderNo, att.seq, att.realFileName)
                          }
                          className="flex-1 truncate text-left text-[#3A6499] hover:underline"
                        >
                          {att.realFileName}
                        </button>
                        <span className="text-slate-400 text-xs whitespace-nowrap">
                          {formatBytes(Number(att.fileSize))}
                        </span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          )}

          {/* 업무 협의 */}
          {activeTab === "업무협의" && (
            <div className="flex flex-col gap-4">
              {/* 댓글 첨부파일 선택용 숨김 input (스레드 공용) */}
              <input
                ref={commentFileInputRef}
                type="file"
                multiple
                className="hidden"
                onChange={(e) => {
                  if (pendingFileOpnId) handleCommentFileAdd(pendingFileOpnId, e.target.files);
                  if (commentFileInputRef.current) commentFileInputRef.current.value = "";
                }}
              />
              {/* 헤더 + 새 협의 버튼 */}
              <div className="flex items-center justify-between">
                <h3 className="flex items-center gap-2 text-sm font-semibold text-slate-700">
                  <MessageSquare className="size-4" />
                  업무 협의
                </h3>
                {!showNewDiscForm && (
                  <Button
                    size="sm"
                    className="bg-[var(--sidebar-bg)] hover:bg-[var(--sidebar-bg)]/90 text-white"
                    onClick={() => setShowNewDiscForm(true)}
                  >
                    <Plus className="size-3.5" />새 협의 시작
                  </Button>
                )}
              </div>

              {/* 새 협의 입력 폼 */}
              {showNewDiscForm && (
                <div className="rounded-lg border border-[var(--sidebar-bg)]/30 bg-slate-50 p-3 flex flex-col gap-2">
                  <input
                    type="text"
                    placeholder="협의 제목을 입력하세요..."
                    value={newDiscTitle}
                    onChange={(e) => setNewDiscTitle(e.target.value)}
                    onKeyDown={(e) => e.key === "Enter" && handleCreateDiscussion()}
                    autoFocus
                    className="w-full px-3 py-2 text-sm rounded-md border border-slate-200 placeholder:text-slate-400 focus:outline-none focus:border-[var(--sidebar-bg)]"
                  />
                  <input
                    ref={newDiscFileInputRef}
                    type="file"
                    multiple
                    className="hidden"
                    onChange={(e) => {
                      handleNewDiscFileAdd(e.target.files);
                      if (newDiscFileInputRef.current) newDiscFileInputRef.current.value = "";
                    }}
                  />
                  {newDiscFiles.length > 0 && (
                    <ul className="flex flex-col gap-1">
                      {newDiscFiles.map((file, idx) => (
                        <li
                          key={idx}
                          className="flex items-center gap-2 bg-white border border-slate-200 rounded-md px-2 py-1 text-xs"
                        >
                          <span className="flex-1 truncate text-slate-700">{file.name}</span>
                          <span className="text-slate-400 whitespace-nowrap">
                            {formatBytes(file.size)}
                          </span>
                          <button
                            type="button"
                            onClick={() => handleNewDiscFileRemove(idx)}
                            className="text-slate-400 hover:text-red-500 transition-colors"
                          >
                            <X className="size-3" />
                          </button>
                        </li>
                      ))}
                    </ul>
                  )}
                  <div className="flex items-center justify-between gap-2">
                    <button
                      type="button"
                      onClick={() => newDiscFileInputRef.current?.click()}
                      className="flex items-center gap-1.5 text-xs text-slate-500 hover:text-slate-700"
                    >
                      <Paperclip className="size-3.5" />
                      첨부파일
                    </button>
                    <div className="flex gap-2">
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => {
                          setShowNewDiscForm(false);
                          setNewDiscTitle("");
                          setNewDiscFiles([]);
                        }}
                      >
                        취소
                      </Button>
                      <Button
                        size="sm"
                        className="bg-[var(--sidebar-bg)] hover:bg-[var(--sidebar-bg)]/90 text-white"
                        disabled={submittingDisc || !newDiscTitle.trim()}
                        onClick={handleCreateDiscussion}
                      >
                        등록
                      </Button>
                    </div>
                  </div>
                </div>
              )}

              {/* 오류 */}
              {discError && (
                <div className="text-xs text-red-500 bg-red-50 px-3 py-2 rounded-md border border-red-200">
                  {discError}
                </div>
              )}

              {/* 로딩 */}
              {loadingDisc && (
                <div className="text-center py-8 text-sm text-slate-400">
                  <span className="inline-block w-4 h-4 border-2 border-slate-300 border-t-[var(--sidebar-bg)] rounded-full animate-spin mr-2 align-middle" />
                  불러오는 중...
                </div>
              )}

              {/* 협의 없음 */}
              {!loadingDisc && discussions.length === 0 && !showNewDiscForm && (
                <div className="text-center py-10 text-sm text-slate-400">
                  등록된 협의가 없습니다.
                </div>
              )}

              {/* 협의 스레드 목록 */}
              {!loadingDisc &&
                discussions.map((disc) => {
                  const isExpanded = expandedIds.has(disc.opnId);
                  const commentInput = commentInputs[disc.opnId] ?? "";
                  const isSubmitting = submittingCmnt[disc.opnId] ?? false;

                  return (
                    <div
                      key={disc.opnId}
                      className="rounded-lg border border-slate-200 overflow-hidden"
                    >
                      {/* 스레드 헤더 */}
                      <div className="bg-slate-50 px-4 py-3">
                        <div className="flex items-start justify-between gap-2">
                          <div className="flex items-start gap-2">
                            <MessageSquare className="size-4 text-slate-400 mt-0.5 shrink-0" />
                            <span className="text-sm font-medium text-slate-800">
                              {disc.opnTitle}
                            </span>
                          </div>
                          <button
                            onClick={() => toggleExpand(disc.opnId)}
                            className="text-slate-400 hover:text-slate-600 shrink-0"
                          >
                            {isExpanded ? (
                              <ChevronUp className="size-4" />
                            ) : (
                              <ChevronDown className="size-4" />
                            )}
                          </button>
                        </div>
                        <div className="flex items-center gap-1.5 mt-1 ml-6 text-xs text-slate-400">
                          <User className="size-3" />
                          <span>{disc.wrtrNm}</span>
                          <span>·</span>
                          <span>{disc.regDt ? disc.regDt.slice(0, 10) : ""}</span>
                          <span>·</span>
                          <span>댓글 {disc.comments.length}개</span>
                        </div>
                      </div>

                      {/* 댓글 목록 + 입력 */}
                      {isExpanded && (
                        <>
                          <div className="divide-y divide-slate-100 bg-white">
                            {disc.comments.map((comment) => (
                              <div
                                key={comment.cmntId}
                                className="px-4 py-3 flex gap-3"
                              >
                                <div className="size-8 rounded-full bg-slate-200 flex items-center justify-center shrink-0">
                                  <User className="size-4 text-slate-500" />
                                </div>
                                <div className="flex-1">
                                  <div className="flex items-center gap-2 mb-1">
                                    <span className="text-sm font-medium text-slate-800">
                                      {comment.wrtrNm}
                                    </span>
                                    {comment.wrtrRoleNm && (
                                      <span className="text-xs px-1.5 py-0.5 rounded bg-slate-100 text-slate-500">
                                        {comment.wrtrRoleNm}
                                      </span>
                                    )}
                                    <span className="text-xs text-slate-400">
                                      {comment.regDt
                                        ? comment.regDt.slice(0, 10)
                                        : ""}
                                    </span>
                                  </div>
                                  <p className="text-sm text-slate-600">
                                    {comment.cmntCtt}
                                  </p>
                                  {comment.attachments.length > 0 && (
                                    <ul className="flex flex-col gap-1 mt-1.5">
                                      {comment.attachments.map((att) => (
                                        <li key={att.seqNo}>
                                          <button
                                            type="button"
                                            onClick={() =>
                                              downloadCommentAttach(
                                                comment.cmntId,
                                                att.seqNo,
                                                att.realFileName,
                                              )
                                            }
                                            className="flex items-center gap-1 text-xs text-[#3A6499] hover:underline"
                                          >
                                            <Paperclip className="size-3" />
                                            {att.realFileName}
                                          </button>
                                        </li>
                                      ))}
                                    </ul>
                                  )}
                                </div>
                              </div>
                            ))}
                          </div>

                          {/* 댓글 입력 */}
                          <div className="px-4 pt-3 pb-1 bg-white border-t border-slate-100">
                            <input
                              type="text"
                              placeholder="댓글을 입력하세요..."
                              value={commentInput}
                              onChange={(e) =>
                                setCommentInputs((prev) => ({
                                  ...prev,
                                  [disc.opnId]: e.target.value,
                                }))
                              }
                              onKeyDown={(e) => {
                                if (e.key === "Enter") handleAddComment(disc.opnId);
                              }}
                              className="w-full px-3 py-2 text-sm rounded-md border border-slate-200 placeholder:text-slate-400 focus:outline-none focus:border-[var(--sidebar-bg)]"
                            />
                          </div>
                          {(commentFiles[disc.opnId] ?? []).length > 0 && (
                            <ul className="px-4 pt-1 flex flex-col gap-1 bg-white">
                              {(commentFiles[disc.opnId] ?? []).map((file, idx) => (
                                <li
                                  key={idx}
                                  className="flex items-center gap-2 bg-slate-50 border border-slate-200 rounded-md px-2 py-1 text-xs"
                                >
                                  <span className="flex-1 truncate text-slate-700">{file.name}</span>
                                  <span className="text-slate-400 whitespace-nowrap">
                                    {formatBytes(file.size)}
                                  </span>
                                  <button
                                    type="button"
                                    onClick={() => handleCommentFileRemove(disc.opnId, idx)}
                                    className="text-slate-400 hover:text-red-500 transition-colors"
                                  >
                                    <X className="size-3" />
                                  </button>
                                </li>
                              ))}
                            </ul>
                          )}
                          <div className="px-4 py-2 flex items-center justify-between bg-white">
                            <button
                              type="button"
                              onClick={() => {
                                setPendingFileOpnId(disc.opnId);
                                commentFileInputRef.current?.click();
                              }}
                              className="flex items-center gap-1.5 text-xs text-slate-500 hover:text-slate-700"
                            >
                              <Paperclip className="size-3.5" />
                              첨부파일
                            </button>
                            <Button
                              size="sm"
                              className="bg-[var(--sidebar-bg)] hover:bg-[var(--sidebar-bg)]/90 text-white"
                              disabled={isSubmitting || !commentInput.trim()}
                              onClick={() => handleAddComment(disc.opnId)}
                            >
                              등록
                            </Button>
                          </div>
                        </>
                      )}
                    </div>
                  );
                })}
            </div>
          )}

          {/* 작업완료 결재 */}
          {activeTab === "작업완료결재" && (
            <WorkResultSection detail={detail} onSubmitted={onWorkResultSubmitted} />
          )}
        </div>

        {/* 푸터 */}
        {footer && (
          <DialogFooter className="px-5 py-3">{footer}</DialogFooter>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default WorkDetailModal;
