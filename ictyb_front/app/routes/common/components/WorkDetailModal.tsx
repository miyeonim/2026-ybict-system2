import { useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import {
  Dialog,
  DialogContent,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
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
import { Checkbox } from "@/components/ui/checkbox";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { DatePickerField } from "@/components/ui/date-picker-field";
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
  Pencil,
  Trash2,
  History as HistoryIcon,
  FileDown,
  FileSearch,
  CalendarClock,
} from "lucide-react";
import { useAuthContext } from "@routes/common/jwt/AuthContext";
import {
  fetchDiscussions,
  createDiscussion,
  addComment,
  downloadCommentAttach,
  markDiscussionsRead,
} from "@hooks/work_opinion/WorkOpinionController";
import type { DiscussionItem } from "@hooks/work_opinion/type";
import {
  fetchWorkDetail,
  downloadWorkAttach,
  downloadWorkResultAttach,
  fetchNextCandidates,
  submitApproval,
  fetchCreateOptions,
  updateWorkOrder,
  deleteWorkOrder,
  extendWorkPeriod,
} from "@hooks/work_my/WorksMyController";
import type {
  WorksMyDetail,
  WorksMyCandidate,
  WorksMyCreateOptions,
  WorksMyUpdateRequest,
  WorksMySecretInfo,
} from "@routes/works_my/WorksMyDto";
import CreateWorkRequestDialog from "@routes/works_my/CreateWorkRequestDialog";

export type ModalTab = "기본정보" | "업무협의";

export interface WorkDetailItem {
  workOrderNo: string;
  title: string;
  department: string;
  dueDt: string;
  part?: string;
  status?: string;
  approvalStatus?: string;
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
  /** 업무협의 탭 진입해 협의 목록을 읽음 처리한 뒤 호출됨 - 부모 화면의 협의 탭 new! 배지를 새로고침할 때 사용 */
  onDiscussionsRead?: (workOrderNo: string) => void;
  /** 삭제 완료 시 호출됨. 전달되지 않으면 삭제 버튼은 표시되지 않는다. */
  onDeleted?: (workOrderNo: string) => void;
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
];

const BasicInfoRow: React.FC<{ label: string; children: React.ReactNode }> = ({
  label,
  children,
}) => (
  <div className="flex items-start gap-3 py-2.5 border-b border-slate-100">
    <span className="w-20 shrink-0 text-sm text-slate-400 pt-0.5">{label}</span>
    <span className="text-sm text-slate-700 break-words">{children}</span>
  </div>
);

// 조치사항(작업결과) 섹션: 기본 정보 탭 하단에 표시. 조회 및 (109단계 작업자 본인 차례일 때) 작성/제출
const WorkResultSection: React.FC<{
  detail: WorksMyDetail | null;
  onSubmitted?: (workOrderNo: string) => void;
}> = ({ detail, onSubmitted }) => {
  const { user } = useAuthContext();
  const [resultText, setResultText] = useState("");
  const [candidates, setCandidates] = useState<WorksMyCandidate[]>([]);
  const [selectedSabun, setSelectedSabun] = useState("");
  const [loadingCandidates, setLoadingCandidates] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [resultFiles, setResultFiles] = useState<File[]>([]);
  const resultFileInputRef = useRef<HTMLInputElement>(null);
  const [resultIsSecret, setResultIsSecret] = useState("N");
  const [resultOppbClYn, setResultOppbClYn] = useState("Y");
  const [resultUserSecretContent, setResultUserSecretContent] = useState("");
  const [resultAttachExpireDate, setResultAttachExpireDate] = useState("");

  const canWrite =
    !!detail && !detail.workResult && detail.myTurn && detail.currentActId === "109" && !!onSubmitted;

  useEffect(() => {
    if (!canWrite || !detail || !user) return;
    setLoadingCandidates(true);
    fetchNextCandidates(detail.workOrderNo, user.userEmpno)
      .then((res) => {
        setCandidates(res.candidates);
        // 후보가 한전 반송자 한 명으로 고정된 경우(한전이 실제 작업자에게 직접 반송한 재작업 건)처럼
        // 후보가 1명뿐이면 선택할 필요 없이 바로 선택된 상태로 둔다.
        if (res.candidates.length === 1) {
          setSelectedSabun(res.candidates[0].sabun);
        }
      })
      .catch(() => setError("다음 결재자 후보 조회에 실패했습니다."))
      .finally(() => setLoadingCandidates(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [canWrite, detail?.workOrderNo]);

  const handleSubmit = async () => {
    if (!detail || !user || !resultText.trim()) return;
    const next = candidates.find((c) => c.sabun === selectedSabun);
    if (!next) {
      setError("다음 결재자를 선택하세요.");
      return;
    }
    setSubmitting(true);
    setError(null);
    try {
      const secretInfo: WorksMySecretInfo = {
        isSecret: resultIsSecret,
        oppbClYn: resultOppbClYn,
        userSecretContent: resultUserSecretContent,
        attachExpireDate: resultAttachExpireDate,
      };
      await submitApproval(detail.workOrderNo, user, next, resultText.trim(), resultFiles, secretInfo);
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

        <div className="flex flex-col gap-2 border border-slate-200 rounded-lg p-3">
          <div className="flex flex-wrap items-start gap-4">
            <div className="flex items-center gap-2 shrink-0">
              <Checkbox
                id="result-view-is-secret"
                checked={detail.workResult.isSecret === "Y"}
                disabled
              />
              <Label htmlFor="result-view-is-secret" className="whitespace-nowrap">
                개인정보 포함 여부
              </Label>
            </div>

            <div className="flex items-start gap-3 shrink-0">
              <p className="text-xs text-slate-500 leading-relaxed w-64 shrink-0">
                <span className="font-medium text-slate-600">개인정보보호법 제2조(정의)</span>
                <br />
                "개인정보"란 살아있는 개인을 알아볼 수 있는 정보(성명, 주민번호 등)로서 다른
                정보와 쉽게 결합하여 개인을 알아볼 수 있는 것도 포함함.
              </p>

              <RadioGroup
                value={detail.workResult.oppbClYn ?? ""}
                disabled
                className="flex flex-col gap-1.5 shrink-0"
              >
                <div className="flex items-center gap-1.5">
                  <RadioGroupItem value="Y" id="result-view-oppb-y" />
                  <Label htmlFor="result-view-oppb-y" className="font-normal whitespace-nowrap">
                    1. 대외 게시용
                  </Label>
                </div>
                <div className="flex items-center gap-1.5">
                  <RadioGroupItem value="N" id="result-view-oppb-n" />
                  <Label htmlFor="result-view-oppb-n" className="font-normal whitespace-nowrap">
                    2. 기타(1번 제외)
                  </Label>
                </div>
              </RadioGroup>
            </div>

            <div className="flex items-center gap-2 flex-1 min-w-48">
              <Label className="whitespace-nowrap shrink-0">개인정보 포함 항목</Label>
              <Input
                value={detail.workResult.userSecretContent ?? ""}
                disabled
                readOnly
                placeholder="예: 성명, 연락처"
              />
            </div>
          </div>

          <div className="flex items-center gap-2">
            <Label className="whitespace-nowrap">파기일</Label>
            <DatePickerField
              value={detail.workResult.attachExpireDate ?? ""}
              onChange={() => {}}
              disabled
            />
          </div>
        </div>
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
        <div
          className="border-2 border-dashed border-slate-300 rounded-lg px-4 py-6 text-center text-sm text-slate-500 cursor-pointer hover:border-[var(--sidebar-bg)] hover:text-[var(--sidebar-bg)] hover:bg-slate-50 transition-colors"
          onClick={() => resultFileInputRef.current?.click()}
          onDragOver={(e) => e.preventDefault()}
          onDrop={(e) => {
            e.preventDefault();
            handleResultFileAdd(e.dataTransfer.files);
          }}
        >
          📁 클릭하거나 파일을 여기로 드래그하세요
        </div>
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

      <div className="flex flex-col gap-2 border border-slate-200 rounded-lg p-3">
        <div className="flex flex-wrap items-start gap-4">
          <div className="flex items-center gap-2 shrink-0">
            <Checkbox
              id="result-is-secret"
              checked={resultIsSecret === "Y"}
              onCheckedChange={(checked) =>
                setResultIsSecret(checked === true ? "Y" : "N")
              }
            />
            <Label htmlFor="result-is-secret" className="cursor-pointer whitespace-nowrap">
              개인정보 포함 여부
            </Label>
          </div>

          <div className="flex items-start gap-3 shrink-0">
            <p className="text-xs text-slate-500 leading-relaxed w-64 shrink-0">
              <span className="font-medium text-slate-600">개인정보보호법 제2조(정의)</span>
              <br />
              "개인정보"란 살아있는 개인을 알아볼 수 있는 정보(성명, 주민번호 등)로서 다른
              정보와 쉽게 결합하여 개인을 알아볼 수 있는 것도 포함함.
            </p>

            <RadioGroup
              value={resultOppbClYn}
              onValueChange={(v: string) => setResultOppbClYn(v)}
              disabled={resultIsSecret !== "Y"}
              className="flex flex-col gap-1.5 shrink-0"
            >
              <div className="flex items-center gap-1.5">
                <RadioGroupItem value="Y" id="result-oppb-y" />
                <Label htmlFor="result-oppb-y" className="cursor-pointer font-normal whitespace-nowrap">
                  1. 대외 게시용
                </Label>
              </div>
              <div className="flex items-center gap-1.5">
                <RadioGroupItem value="N" id="result-oppb-n" />
                <Label htmlFor="result-oppb-n" className="cursor-pointer font-normal whitespace-nowrap">
                  2. 기타(1번 제외)
                </Label>
              </div>
            </RadioGroup>
          </div>

          <div className="flex items-center gap-2 flex-1 min-w-48">
            <Label className="whitespace-nowrap shrink-0">개인정보 포함 항목</Label>
            <Input
              value={resultUserSecretContent}
              onChange={(e) => setResultUserSecretContent(e.target.value)}
              disabled={resultIsSecret !== "Y"}
              placeholder="예: 성명, 연락처"
            />
          </div>
        </div>

        <div className="flex items-center gap-2">
          <Label className="whitespace-nowrap">파기일</Label>
          <DatePickerField
            value={resultAttachExpireDate}
            onChange={(v) => setResultAttachExpireDate(v)}
            disabled={resultIsSecret !== "Y"}
          />
        </div>
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

const EMPTY_UPDATE_FORM: WorksMyUpdateRequest = {
  changeTitle: "",
  changeReason: "",
  systemCd: "",
  serviceType: "",
  workType: "",
  workGubun: "",
  workLevel: "",
  workPeriod: "",
  workDuration: "",
  expectedFinishedDt: "",
  drsImptYn: "",
  isSecret: "N",
  oppbClYn: "Y",
  userSecretContent: "",
  attachExpireDate: "",
  removeAttachSeqs: [],
};

// 기본정보 탭: 작업지시서 수정 폼 (104/106 단계의 현재 결재자 = 한전 담당자만 진입 가능)
const WorkOrderEditSection: React.FC<{
  detail: WorksMyDetail;
  onCancel: () => void;
  onSaved: (updated: WorksMyDetail) => void;
}> = ({ detail, onCancel, onSaved }) => {
  const { user } = useAuthContext();
  const [form, setForm] = useState<WorksMyUpdateRequest>(EMPTY_UPDATE_FORM);
  const [options, setOptions] = useState<WorksMyCreateOptions | null>(null);
  const [loadingOptions, setLoadingOptions] = useState(true);
  const [newFiles, setNewFiles] = useState<File[]>([]);
  const [removedSeqs, setRemovedSeqs] = useState<Set<string>>(new Set());
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    setForm({
      changeTitle: detail.changeTitle ?? "",
      changeReason: detail.changeReason ?? "",
      systemCd: detail.systemCd ?? "",
      serviceType: detail.serviceType ?? "",
      workType: detail.workType ?? "",
      workGubun: detail.workGubun ?? "",
      workLevel: detail.workLevel ?? "",
      workPeriod: detail.workPeriod ?? "",
      workDuration: detail.workDuration ?? "",
      expectedFinishedDt: detail.expectedFinishedDt ?? "",
      drsImptYn: detail.drsImptYn ?? "",
      isSecret: detail.isSecret ?? "N",
      oppbClYn: detail.oppbClYn ?? "Y",
      userSecretContent: detail.userSecretContent ?? "",
      attachExpireDate: detail.attachExpireDate ?? "",
      removeAttachSeqs: [],
    });
    setNewFiles([]);
    setRemovedSeqs(new Set());
    setError(null);
    setLoadingOptions(true);
    fetchCreateOptions()
      .then(setOptions)
      .catch(() => setError("수정 폼 옵션을 불러오지 못했습니다."))
      .finally(() => setLoadingOptions(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [detail.workOrderNo]);

  const update = (field: keyof WorksMyUpdateRequest, value: string) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  // 단위시스템을 선택하면 업무분야/DRS영향여부를 its_system_info 값 기준으로 자동 완성한다.
  const handleUnitSystemChange = (systemCd: string) => {
    const opt = options?.unitSystemOptions.find((o) => o.code === systemCd);
    setForm((prev) => ({
      ...prev,
      systemCd,
      drsImptYn: opt?.drsImptYn ?? "",
    }));
  };

  const handleFileAdd = (files: FileList | null) => {
    if (!files) return;
    setNewFiles((prev) => [...prev, ...Array.from(files)]);
  };

  const handleFileRemove = (index: number) => {
    setNewFiles((prev) => prev.filter((_, i) => i !== index));
  };

  const toggleRemoveExisting = (seq: string) => {
    setRemovedSeqs((prev) => {
      const next = new Set(prev);
      if (next.has(seq)) next.delete(seq);
      else next.add(seq);
      return next;
    });
  };

  const handleSubmit = async () => {
    if (!user || !form.changeTitle.trim() || submitting) return;
    setSubmitting(true);
    setError(null);
    try {
      await updateWorkOrder(
        detail.workOrderNo,
        user.userEmpno,
        { ...form, removeAttachSeqs: Array.from(removedSeqs) },
        newFiles,
      );
      const updated = await fetchWorkDetail(detail.workOrderNo, user.userEmpno);
      onSaved(updated);
    } catch (e: any) {
      setError(e.message ?? "작업지시서 수정 중 오류가 발생했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  const codeSelect = (
    label: string,
    field: keyof WorksMyUpdateRequest,
    codeOptions: WorksMyCreateOptions["serviceTypeOptions"] | undefined,
  ) => (
    <div className="flex flex-col gap-1.5">
      <Label>{label}</Label>
      <Select value={form[field] as string} onValueChange={(v: string) => update(field, v)}>
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
    <div className="flex flex-col gap-4">
      {error && (
        <div className="text-xs text-red-500 bg-red-50 px-3 py-2 rounded-md border border-red-200">
          {error}
        </div>
      )}

      <div className="flex flex-col gap-1.5">
        <Label>제목</Label>
        <Input value={form.changeTitle} onChange={(e) => update("changeTitle", e.target.value)} />
      </div>

      <div className="flex flex-col gap-1.5">
        <Label>지시내용</Label>
        <Textarea
          value={form.changeReason}
          onChange={(e) => update("changeReason", e.target.value)}
          className="min-h-40"
        />
      </div>

      <div className="grid grid-cols-2 gap-3">
        <div className="flex flex-col gap-1.5">
          <Label>단위시스템</Label>
          <Select value={form.systemCd} onValueChange={handleUnitSystemChange}>
            <SelectTrigger className="w-full">
              <SelectValue placeholder="선택하세요" />
            </SelectTrigger>
            <SelectContent>
              {(options?.unitSystemOptions ?? []).map((opt) => (
                <SelectItem key={opt.code} value={opt.code}>
                  {opt.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div className="flex flex-col gap-1.5">
          <Label>업무분야</Label>
          <Input
            value={options?.unitSystemOptions.find((o) => o.code === form.systemCd)?.businessField ?? ""}
            disabled
            readOnly
            placeholder="단위시스템을 선택하면 자동으로 채워집니다"
          />
        </div>
        {codeSelect("서비스유형", "serviceType", options?.serviceTypeOptions)}
        {codeSelect("작업유형", "workType", options?.workTypeOptions)}
        {codeSelect("작업구분", "workGubun", options?.workGubunOptions)}
        {codeSelect("작업레벨", "workLevel", options?.workLevelOptions)}
        <div className="flex flex-col gap-1.5">
          <Label>DRS영향</Label>
          <Input
            value={options?.drsImptOptions.find((o) => o.code === form.drsImptYn)?.label ?? ""}
            disabled
            readOnly
            placeholder="단위시스템을 선택하면 자동으로 채워집니다"
          />
        </div>
      </div>

      <div className="grid grid-cols-2 gap-3">
        <div className="flex flex-col gap-1.5">
          <Label>처리기간(일)</Label>
          <Input
            type="number"
            min="0"
            value={form.workPeriod}
            onChange={(e) => update("workPeriod", e.target.value)}
          />
        </div>
        <div className="flex flex-col gap-1.5">
          <Label>처리예정일</Label>
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
        {detail.attachments.length > 0 && (
          <ul className="flex flex-col gap-1.5">
            {detail.attachments.map((att) => {
              const marked = removedSeqs.has(att.seq);
              return (
                <li
                  key={att.seq}
                  className={`flex items-center gap-2 border rounded-md px-3 py-2 text-sm ${
                    marked ? "bg-red-50 border-red-200" : "bg-slate-50 border-slate-200"
                  }`}
                >
                  <span
                    className={`flex-1 truncate ${
                      marked ? "line-through text-slate-400" : "text-slate-700"
                    }`}
                  >
                    {att.realFileName}
                  </span>
                  <button
                    type="button"
                    onClick={() => toggleRemoveExisting(att.seq)}
                    className={`text-xs shrink-0 ${
                      marked ? "text-slate-500 hover:text-slate-700" : "text-red-500 hover:text-red-700"
                    }`}
                  >
                    {marked ? "삭제 취소" : "삭제"}
                  </button>
                </li>
              );
            })}
          </ul>
        )}
        <input
          ref={fileInputRef}
          type="file"
          multiple
          className="hidden"
          onChange={(e) => {
            handleFileAdd(e.target.files);
            if (fileInputRef.current) fileInputRef.current.value = "";
          }}
        />
        <Button
          type="button"
          variant="outline"
          onClick={() => fileInputRef.current?.click()}
          className="w-fit"
        >
          <Paperclip className="size-4" />
          파일 추가
        </Button>
        <div
          className="border-2 border-dashed border-slate-300 rounded-lg px-4 py-6 text-center text-sm text-slate-500 cursor-pointer hover:border-[var(--sidebar-bg)] hover:text-[var(--sidebar-bg)] hover:bg-slate-50 transition-colors"
          onClick={() => fileInputRef.current?.click()}
          onDragOver={(e) => e.preventDefault()}
          onDrop={(e) => {
            e.preventDefault();
            handleFileAdd(e.dataTransfer.files);
          }}
        >
          📁 클릭하거나 파일을 여기로 드래그하세요
        </div>
        {newFiles.length > 0 && (
          <ul className="flex flex-col gap-1.5 mt-1">
            {newFiles.map((file, idx) => (
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

      <div className="flex flex-col gap-2 border border-slate-200 rounded-lg p-3">
        <div className="flex flex-wrap items-start gap-4">
          <div className="flex items-center gap-2 shrink-0">
            <Checkbox
              id="edit-is-secret"
              checked={form.isSecret === "Y"}
              onCheckedChange={(checked) =>
                update("isSecret", checked === true ? "Y" : "N")
              }
            />
            <Label htmlFor="edit-is-secret" className="cursor-pointer whitespace-nowrap">
              개인정보 포함 여부
            </Label>
          </div>

          <div className="flex items-start gap-3 shrink-0">
            <p className="text-xs text-slate-500 leading-relaxed w-64 shrink-0">
              <span className="font-medium text-slate-600">개인정보보호법 제2조(정의)</span>
              <br />
              "개인정보"란 살아있는 개인을 알아볼 수 있는 정보(성명, 주민번호 등)로서 다른
              정보와 쉽게 결합하여 개인을 알아볼 수 있는 것도 포함함.
            </p>

            <RadioGroup
              value={form.oppbClYn}
              onValueChange={(v: string) => update("oppbClYn", v)}
              disabled={form.isSecret !== "Y"}
              className="flex flex-col gap-1.5 shrink-0"
            >
              <div className="flex items-center gap-1.5">
                <RadioGroupItem value="Y" id="edit-oppb-y" />
                <Label htmlFor="edit-oppb-y" className="cursor-pointer font-normal whitespace-nowrap">
                  1. 대외 게시용
                </Label>
              </div>
              <div className="flex items-center gap-1.5">
                <RadioGroupItem value="N" id="edit-oppb-n" />
                <Label htmlFor="edit-oppb-n" className="cursor-pointer font-normal whitespace-nowrap">
                  2. 기타(1번 제외)
                </Label>
              </div>
            </RadioGroup>
          </div>

          <div className="flex items-center gap-2 flex-1 min-w-48">
            <Label className="whitespace-nowrap shrink-0">개인정보 포함 항목</Label>
            <Input
              value={form.userSecretContent}
              onChange={(e) => update("userSecretContent", e.target.value)}
              disabled={form.isSecret !== "Y"}
              placeholder="예: 성명, 연락처"
            />
          </div>
        </div>

        <div className="flex items-center gap-2">
          <Label className="whitespace-nowrap">파기일</Label>
          <DatePickerField
            value={form.attachExpireDate}
            onChange={(v) => update("attachExpireDate", v)}
            disabled={form.isSecret !== "Y"}
          />
        </div>
      </div>

      <div className="flex items-center gap-2 justify-end">
        <Button variant="outline" onClick={onCancel} disabled={submitting}>
          취소
        </Button>
        <Button
          className="bg-[var(--sidebar-bg)] hover:bg-[var(--sidebar-bg)]/90 text-white"
          disabled={submitting || loadingOptions || !form.changeTitle.trim() || !form.systemCd}
          onClick={handleSubmit}
        >
          저장
        </Button>
      </div>
    </div>
  );
};

const formatDetailDate = (dt: string | null) => (dt && dt.length >= 8 ? dt.slice(0, 8) : dt);

// yyyyMMddHHmmss -> yy.MM.dd HH:mm (작성자/승인자 박스 표시용, 초 단위 생략)
const formatBoxDateTime = (dt: string | null) => {
  if (!dt || dt.length < 12) return formatDetailDate(dt);
  return `${dt.slice(2, 4)}.${dt.slice(4, 6)}.${dt.slice(6, 8)} ${dt.slice(8, 10)}:${dt.slice(10, 12)}`;
};

// approvalHistory에서 해당 단계(actIdNm)의 가장 최근 "승인" 처리 항목을 찾는다.
// 반려된 항목이나 아직 처리 전인 대기(결재대기) 항목은 제외한다 - 재상신으로 같은 단계가
// 여러 번 기록될 수 있으므로 뒤에서부터 찾아 가장 최신 승인 건을 취한다.
const findApprovedEntry = (
  history: WorksMyDetail["approvalHistory"] | undefined,
  actIdNm: string,
) => {
  if (!history) return null;
  for (let i = history.length - 1; i >= 0; i--) {
    const h = history[i];
    if (h.actIdNm === actIdNm && h.signLabel === "승인") return h;
  }
  return null;
};

// 작성자(104)/승인자(106) 이름 + 승인시각을 나란히 보여주는 요약 박스.
// 106이 아직 승인 전이면 이름/시각 모두 공백으로 둔다.
// variant="dark"는 남색 헤더 위에 얹히는 반투명 스타일 (기본은 밝은 배경용).
const WriterApproverBox: React.FC<{
  history: WorksMyDetail["approvalHistory"];
  variant?: "light" | "dark";
}> = ({ history, variant = "light" }) => {
  const writer = findApprovedEntry(history, "지시서 작성");
  const approver = findApprovedEntry(history, "지시서 승인");
  const dark = variant === "dark";

  const Column: React.FC<{ label: string; entry: WorksMyDetail["approvalHistory"][number] | null }> = ({
    label,
    entry,
  }) => (
    <div className="shrink-0 min-w-[116px] flex flex-col">
      <div
        className={`px-4 py-1 text-[11px] font-medium text-center border-b ${
          dark
            ? "bg-white/10 text-white/70 border-white/20"
            : "bg-slate-50 text-slate-500 border-slate-200"
        }`}
      >
        {label}
      </div>
      <div className={`px-4 pt-1.5 text-sm text-center whitespace-nowrap font-medium ${dark ? "text-white" : "text-slate-800"}`}>
        {entry?.name ?? "-"}
      </div>
      <div className={`px-4 pb-1.5 text-xs text-center whitespace-nowrap ${dark ? "text-white/60" : "text-slate-400"}`}>
        {entry?.regDt ? formatBoxDateTime(entry.regDt) : "-"}
      </div>
    </div>
  );

  return (
    <div
      className={`shrink-0 flex rounded-md overflow-hidden border divide-x ${
        dark ? "border-white/20 divide-white/20" : "border-slate-200 divide-slate-200"
      }`}
    >
      <Column label="작성자" entry={writer} />
      <Column label="승인자" entry={approver} />
    </div>
  );
};

// yyyyMMddHHmmss -> yyyy-MM-dd HH:mm:ss (결재이력 팝업 표시용, 시분초까지)
const formatHistoryDateTime = (dt: string | null) => {
  if (!dt || dt.length < 14) return formatDetailDate(dt);
  return `${dt.slice(0, 4)}-${dt.slice(4, 6)}-${dt.slice(6, 8)} ${dt.slice(8, 10)}:${dt.slice(10, 12)}:${dt.slice(12, 14)}`;
};

// 결재이력을 처리 순서대로(일렬로) 보여주는 팝업 (작업지시서 세부 화면 우측 상단 버튼에서 연다)
const ApprovalHistoryDialog: React.FC<{
  open: boolean;
  history: WorksMyDetail["approvalHistory"] | undefined;
  onClose: () => void;
}> = ({ open, history, onClose }) => {
  const items = history ?? [];
  return (
    <Dialog open={open} onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="max-w-md">
        <DialogTitle className="text-center">결재이력</DialogTitle>
        <div className="flex flex-col gap-3 py-2 max-h-96 overflow-y-auto">
          {items.length === 0 && (
            <p className="text-sm text-slate-400 text-center py-4">결재 이력이 없습니다.</p>
          )}
          {items.map((h, idx) => (
            <div key={idx} className="flex items-start gap-3">
              <div className="flex flex-col items-center pt-0.5">
                <span className="flex items-center justify-center w-5 h-5 rounded-full bg-slate-100 text-slate-500 text-[11px] font-semibold">
                  {idx + 1}
                </span>
                {idx < items.length - 1 && (
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
                  {h.regDt ? formatHistoryDateTime(h.regDt) : "결재 대기 중"}
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
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>
            닫기
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

// 이 지시서의 원본이 된 요청서 상세를 읽기 전용으로 보여주는 팝업 (작업지시서 세부 화면 좌측 상단 요청번호에서 연다)
// [2026-07-23] 작업 요청서(MY) 목록의 상세 조회와 동일하게, 요청서 등록 화면(CreateWorkRequestDialog)을
// mode="view"로 재사용한다 - 여기서는 결재 액션이 없는 참조용 팝업이라 닫기 버튼만 보여준다.
const SourceRequestDialog: React.FC<{
  workRequestNo: string | null;
  onClose: () => void;
}> = ({ workRequestNo, onClose }) => (
  <CreateWorkRequestDialog
    open={!!workRequestNo}
    mode="view"
    workRequestNo={workRequestNo}
    onClose={onClose}
    renderFooter={() => (
      <Button variant="outline" onClick={onClose}>
        닫기
      </Button>
    )}
  />
);

const WorkDetailModal: React.FC<Props> = ({
  item,
  onClose,
  footer,
  onDetailLoaded,
  onWorkResultSubmitted,
  onTabChange,
  onDiscussionCreated,
  onDiscussionsRead,
  onDeleted,
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
  const [editingOrder, setEditingOrder] = useState(false);
  const [historyDialogOpen, setHistoryDialogOpen] = useState(false);
  const [sourceRequestDialogOpen, setSourceRequestDialogOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [extendingPeriod, setExtendingPeriod] = useState(false);
  const [extendPeriodInput, setExtendPeriodInput] = useState("");
  const [extendSubmitting, setExtendSubmitting] = useState(false);
  const [extendError, setExtendError] = useState<string | null>(null);

  const formatDate = (dt: string) => {
    if (!dt || dt.length < 8) return dt;
    if (dt.includes("-")) return dt.slice(0, 10);
    return `${dt.slice(0, 4)}-${dt.slice(4, 6)}-${dt.slice(6, 8)}`;
  };

  // "yyyyMMddHHmmss" 또는 "yyyy-MM-dd..." 형식의 날짜 문자열을 Date로 변환한다.
  const parseDt = (dt?: string | null): Date | null => {
    if (!dt) return null;
    const digits = dt.replace(/[-:T]/g, "").replace(/\..*$/, "");
    if (digits.length < 8) return null;
    const date = new Date(
      Number(digits.slice(0, 4)),
      Number(digits.slice(4, 6)) - 1,
      Number(digits.slice(6, 8)),
      Number(digits.slice(8, 10) || "0"),
      Number(digits.slice(10, 12) || "0"),
      Number(digits.slice(12, 14) || "0"),
    );
    return Number.isNaN(date.getTime()) ? null : date;
  };

  // 서버(WorkMyServiceImpl.extendWorkPeriod)와 동일하게 기존 처리예정일에
  // (신규 처리기간 - 기존 처리기간)일만큼 더해 처리예정일 변경 결과를 미리 보여준다.
  const extendPreviewDt = (() => {
    if (!extendingPeriod || !detail || extendPeriodInput.trim() === "") return null;
    const newPeriod = Number(extendPeriodInput);
    const currentPeriod = Number(detail.workPeriod ?? "0");
    if (!Number.isFinite(newPeriod) || !Number.isFinite(currentPeriod)) return null;
    const base = parseDt(detail.expectedFinishedDt) ?? new Date();
    const next = new Date(base);
    next.setDate(next.getDate() + (newPeriod - currentPeriod));
    return next;
  })();

  const formatDateObj = (d: Date) =>
    `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;

  // 모달이 열릴 때 등록 정보(지시내용/첨부파일/작업결과 등) 상세 로드
  useEffect(() => {
    setEditingOrder(false);
    setExtendingPeriod(false);
    setExtendError(null);
    if (!item || !user) {
      setDetail(null);
      onDetailLoaded?.(null);
      return;
    }
    setLoadingDetail(true);
    setDetailError(null);
    fetchWorkDetail(item.workOrderNo, user.userEmpno)
      .then((data) => {
        setDetail(data);
        onDetailLoaded?.(data);
      })
      .catch(() => setDetailError("상세 정보를 불러오지 못했습니다."))
      .finally(() => setLoadingDetail(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [item?.workOrderNo, user]);

  // 활성 탭이 바뀔 때마다 부모 화면에 알린다 (승인 버튼 활성화 여부 등에 사용)
  useEffect(() => {
    onTabChange?.(activeTab);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeTab]);

  // 모달이 열리고 업무협의 탭 진입 시 데이터 로드
  useEffect(() => {
    if (!item || activeTab !== "업무협의") return;
    const workOrderNo = item.workOrderNo;
    setLoadingDisc(true);
    setDiscError(null);
    fetchDiscussions(workOrderNo)
      .then((data) => {
        setDiscussions(data);
        if (data.length > 0) {
          setExpandedIds(new Set([data[0].opnId]));
          // 협의 목록을 실제로 화면에 보여준 시점에, 로그인 사용자 기준으로 일괄 읽음 처리한다.
          if (user) {
            markDiscussionsRead(workOrderNo, user.userEmpno)
              .then(() => onDiscussionsRead?.(workOrderNo))
              .catch(() => {
                /* 읽음 처리 실패는 화면에 노출하지 않는다 (new! 배지가 계속 뜨는 정도의 부작용만 있음) */
              });
          }
        }
      })
      .catch(() => setDiscError("협의 목록을 불러오지 못했습니다."))
      .finally(() => setLoadingDisc(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
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
      setDiscussions((prev) => {
        const updated = prev.map((d) =>
          d.opnId === opnId
            ? { ...d, comments: [...d.comments, comment] }
            : d,
        );
        // 댓글(첨부파일 포함)이 새로 등록된 협의는 최신 활동 순 정렬에 맞춰 맨 위로 올린다.
        const idx = updated.findIndex((d) => d.opnId === opnId);
        if (idx <= 0) return updated;
        const [moved] = updated.splice(idx, 1);
        return [moved, ...updated];
      });
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

  const handleDelete = async () => {
    if (!item || !user || deleting) return;
    if (!confirm("정말 이 업무지시서를 삭제하시겠습니까? 삭제 후에는 되돌릴 수 없습니다.")) return;
    setDeleting(true);
    try {
      await deleteWorkOrder(item.workOrderNo, user.userEmpno);
      onDeleted?.(item.workOrderNo);
    } catch (e) {
      alert("삭제 중 오류가 발생했습니다. (" + (e instanceof Error ? e.message : "오류") + ")");
    } finally {
      setDeleting(false);
    }
  };

  const handleStartExtendPeriod = () => {
    setExtendPeriodInput(detail?.workPeriod ?? "");
    setExtendError(null);
    setExtendingPeriod(true);
  };

  const handleSubmitExtendPeriod = async () => {
    if (!item || !user || extendSubmitting) return;
    setExtendError(null);
    setExtendSubmitting(true);
    try {
      const updated = await extendWorkPeriod(item.workOrderNo, user.userEmpno, extendPeriodInput.trim());
      setDetail(updated);
      onDetailLoaded?.(updated);
      setExtendingPeriod(false);
    } catch (e) {
      setExtendError(e instanceof Error ? e.message : "처리기간 연장 중 오류가 발생했습니다.");
    } finally {
      setExtendSubmitting(false);
    }
  };

  const breadcrumb = item
    ? `${item.workOrderNo} · ${item.department}${item.part ? ` > ${item.part}` : ""}`
    : "";

  // 기본정보 탭 그리드와 PDF 인쇄용 뷰에서 공용으로 쓰는 필드 목록
  const basicInfoRows = item
    ? (
        [
          // 작업지시서를 처음 등록한 사람(=지시서 작성(104) 단계를 완료한 사람)을 IT담당자로 표시, 이름 앞에 소속을 붙인다
          {
            label: "IT담당자",
            value: (() => {
              const writerName = findApprovedEntry(detail?.approvalHistory, "지시서 작성")?.name;
              if (!writerName) return "-";
              return detail?.regUserDepNm ? `${detail.regUserDepNm} ${writerName}` : writerName;
            })(),
          },
          // 작업 요청서를 기반으로 전환된 지시서인 경우에만 원본 요청서 작성자를 요청자로 표시, 아니면 빈 값
          {
            label: "요청자",
            value: detail?.requesterName
              ? `${detail.requesterDepNm ?? "-"} ${detail.requesterName} (${detail.requesterSabun ?? "-"}) · ${detail.requesterTel ?? "-"}`
              : "-",
          },
          { label: "번호", value: item.workOrderNo },
          { label: "부서", value: item.department },
          item.part ? { label: "파트", value: item.part } : null,
          item.workType ? { label: "유형", value: item.workType } : null,
          item.managerName ? { label: "담당자", value: item.managerName } : null,
          item.status ? { label: "상태", value: item.status } : null,
          item.approvalStatus ? { label: "결재", value: item.approvalStatus } : null,
          item.regDt ? { label: "등록일", value: formatDate(item.regDt) } : null,
          detail?.targetDepNm ? { label: "대상부서", value: detail.targetDepNm } : null,
          detail?.systemCdLabel ? { label: "단위시스템", value: detail.systemCdLabel } : null,
          detail?.businessField ? { label: "업무분야", value: detail.businessField } : null,
          detail?.serviceTypeLabel ? { label: "서비스유형", value: detail.serviceTypeLabel } : null,
          detail?.workTypeLabel ? { label: "작업유형", value: detail.workTypeLabel } : null,
          detail?.workGubunLabel ? { label: "작업구분", value: detail.workGubunLabel } : null,
          detail?.workLevel ? { label: "작업레벨", value: detail.workLevel } : null,
          detail?.workPeriod ? { label: "처리기간", value: `${detail.workPeriod}일` } : null,
          { label: "처리예정일", value: formatDate(detail?.expectedFinishedDt || item.dueDt) },
          detail?.hopeFinishedDt ? { label: "희망완료일", value: formatDate(detail.hopeFinishedDt) } : null,
          detail?.drsImptLabel ? { label: "DRS영향", value: detail.drsImptLabel } : null,
        ] as ({ label: string; value: string } | null)[]
      ).filter(
        (row): row is { label: string; value: string } => row !== null,
      )
    : [];

  return (
    <>
    <Dialog open={!!item} onOpenChange={(open) => !open && onClose()}>
      <DialogContent
        showCloseButton={false}
        className="max-w-6xl sm:max-w-6xl max-h-[95vh] p-0 gap-0 overflow-hidden flex flex-col"
      >
        {/* 다크 헤더 */}
        <div className="bg-[var(--sidebar-bg)] px-5 py-5 flex items-center justify-between">
          <div className="flex-1 min-w-0">
            <p className="text-xs text-white/60 mb-1">{breadcrumb}</p>
            <DialogTitle className="text-base font-semibold text-white leading-snug">
              {item?.title}
            </DialogTitle>
          </div>
          {detail && (
            <WriterApproverBox history={detail.approvalHistory} variant="dark" />
          )}
          <button
            onClick={onClose}
            className="text-white/70 hover:text-white ml-4 shrink-0"
          >
            <X className="size-5" />
          </button>
        </div>

        {/* 탭 바 */}
        <div className="flex items-center justify-between border-b border-slate-200 bg-white">
          <div className="flex">
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
          <div className="flex items-center gap-2 mr-4 shrink-0">
            <button
              onClick={() => window.print()}
              disabled={!detail}
              className="flex items-center gap-1.5 text-xs font-medium text-slate-500 hover:text-slate-700 border border-slate-300 hover:border-slate-400 rounded-md px-2.5 py-1.5 shrink-0 transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
            >
              <FileDown className="size-3.5" />
              PDF로 내보내기
            </button>
            <button
              onClick={() => setHistoryDialogOpen(true)}
              className="flex items-center gap-1.5 text-xs font-medium text-slate-500 hover:text-slate-700 border border-slate-300 hover:border-slate-400 rounded-md px-2.5 py-1.5 shrink-0 transition-colors"
            >
              <HistoryIcon className="size-3.5" />
              결재이력
            </button>
          </div>
        </div>

        {/* 콘텐츠 - 탭 전환 시 팝업 크기가 흔들리지 않도록 높이를 고정한다 */}
        <div className="h-[65vh] overflow-y-auto p-5">
          {/* 기본 정보 */}
          {activeTab === "기본정보" && item && (
            <div>
              {((detail?.canEdit && !editingOrder) ||
                (detail?.canDelete && onDeleted) ||
                (detail?.canExtendPeriod && !extendingPeriod)) && (
                <div className="flex justify-end gap-2 mb-3">
                  {detail?.canExtendPeriod && !extendingPeriod && (
                    <Button size="sm" variant="outline" onClick={handleStartExtendPeriod}>
                      <CalendarClock className="size-3.5" />
                      처리기간 연장
                    </Button>
                  )}
                  {detail?.canEdit && !editingOrder && (
                    <Button size="sm" variant="outline" onClick={() => setEditingOrder(true)}>
                      <Pencil className="size-3.5" />
                      작업지시서 수정
                    </Button>
                  )}
                  {detail?.canDelete && onDeleted && (
                    <Button
                      size="sm"
                      variant="outline"
                      className="text-red-500 border-red-200 hover:bg-red-50"
                      disabled={deleting}
                      onClick={handleDelete}
                    >
                      <Trash2 className="size-3.5" />
                      삭제
                    </Button>
                  )}
                </div>
              )}

              {extendingPeriod && detail && (
                <div className="flex flex-col gap-2 border border-slate-200 rounded-lg p-3 mb-3 bg-slate-50">
                  <div className="flex items-end gap-3">
                    <div className="flex flex-col gap-1">
                      <Label>처리기간 (일)</Label>
                      <Input
                        type="number"
                        min={1}
                        className="w-28"
                        value={extendPeriodInput}
                        onChange={(e) => setExtendPeriodInput(e.target.value)}
                      />
                    </div>
                    <div className="flex flex-col gap-1">
                      <Label>처리예정일</Label>
                      <p className="h-9 flex items-center text-sm font-medium text-slate-700">
                        {extendPreviewDt ? formatDateObj(extendPreviewDt) : "-"}
                      </p>
                    </div>
                    <p className="text-xs text-slate-500 pb-2">
                      기존 처리기간({detail.workPeriod ?? "0"}일)보다 늘리면, 처리예정일이 늘어난
                      일수만큼 자동으로 함께 밀립니다.
                    </p>
                  </div>
                  {extendError && <p className="text-xs text-red-500">{extendError}</p>}
                  <div className="flex justify-end gap-2">
                    <Button
                      size="sm"
                      variant="outline"
                      disabled={extendSubmitting}
                      onClick={() => setExtendingPeriod(false)}
                    >
                      취소
                    </Button>
                    <Button size="sm" disabled={extendSubmitting} onClick={handleSubmitExtendPeriod}>
                      저장
                    </Button>
                  </div>
                </div>
              )}

              {editingOrder && detail ? (
                <WorkOrderEditSection
                  detail={detail}
                  onCancel={() => setEditingOrder(false)}
                  onSaved={(updated) => {
                    setDetail(updated);
                    onDetailLoaded?.(updated);
                    setEditingOrder(false);
                  }}
                />
              ) : (
                <>
              {detail?.sourceRequestNo && (
                <div className="flex justify-start mb-3">
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => setSourceRequestDialogOpen(true)}
                  >
                    <FileSearch className="size-3.5" />
                    요청서 조회
                  </Button>
                </div>
              )}
              <div className="grid grid-cols-2 gap-x-8">
              {basicInfoRows.map((row) => (
                <BasicInfoRow key={row.label} label={row.label}>
                  {row.value}
                </BasicInfoRow>
              ))}
              </div>

              {detail && (
                <div className="flex flex-col gap-2 border border-slate-200 rounded-lg p-3">
                  <div className="flex flex-wrap items-start gap-4">
                    <div className="flex items-center gap-2 shrink-0">
                      <Checkbox
                        id="view-is-secret"
                        checked={detail.isSecret === "Y"}
                        disabled
                      />
                      <Label htmlFor="view-is-secret" className="whitespace-nowrap">
                        개인정보 포함 여부
                      </Label>
                    </div>

                    <div className="flex items-start gap-3 shrink-0">
                      <p className="text-xs text-slate-500 leading-relaxed w-64 shrink-0">
                        <span className="font-medium text-slate-600">개인정보보호법 제2조(정의)</span>
                        <br />
                        "개인정보"란 살아있는 개인을 알아볼 수 있는 정보(성명, 주민번호 등)로서 다른
                        정보와 쉽게 결합하여 개인을 알아볼 수 있는 것도 포함함.
                      </p>

                      <RadioGroup
                        value={detail.oppbClYn ?? ""}
                        disabled
                        className="flex flex-col gap-1.5 shrink-0"
                      >
                        <div className="flex items-center gap-1.5">
                          <RadioGroupItem value="Y" id="view-oppb-y" />
                          <Label htmlFor="view-oppb-y" className="font-normal whitespace-nowrap">
                            1. 대외 게시용
                          </Label>
                        </div>
                        <div className="flex items-center gap-1.5">
                          <RadioGroupItem value="N" id="view-oppb-n" />
                          <Label htmlFor="view-oppb-n" className="font-normal whitespace-nowrap">
                            2. 기타(1번 제외)
                          </Label>
                        </div>
                      </RadioGroup>
                    </div>

                    <div className="flex items-center gap-2 flex-1 min-w-48">
                      <Label className="whitespace-nowrap shrink-0">개인정보 포함 항목</Label>
                      <Input
                        value={detail.userSecretContent ?? ""}
                        disabled
                        readOnly
                        placeholder="예: 성명, 연락처"
                      />
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    <Label className="whitespace-nowrap">파기일</Label>
                    <DatePickerField
                      value={detail.attachExpireDate ?? ""}
                      onChange={() => {}}
                      disabled
                    />
                  </div>
                </div>
              )}

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

              <div className="mt-6 pt-4 border-t border-slate-200">
                <WorkResultSection detail={detail} onSubmitted={onWorkResultSubmitted} />
              </div>
                </>
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
                  <Textarea
                    placeholder="협의 제목을 입력하세요..."
                    value={newDiscTitle}
                    onChange={(e) => setNewDiscTitle(e.target.value)}
                    autoFocus
                    className="w-full min-h-[72px] px-3 py-2 text-sm rounded-md border border-slate-200 placeholder:text-slate-400 focus:outline-none focus:border-[var(--sidebar-bg)]"
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
                  <div
                    className="border-2 border-dashed border-slate-300 rounded-lg px-3 py-4 text-center text-xs text-slate-500 cursor-pointer hover:border-[var(--sidebar-bg)] hover:text-[var(--sidebar-bg)] hover:bg-white transition-colors"
                    onClick={() => newDiscFileInputRef.current?.click()}
                    onDragOver={(e) => e.preventDefault()}
                    onDrop={(e) => {
                      e.preventDefault();
                      handleNewDiscFileAdd(e.dataTransfer.files);
                    }}
                  >
                    📁 클릭하거나 파일을 여기로 드래그하세요
                  </div>
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
                            <span className="text-sm font-medium text-slate-800 whitespace-pre-wrap break-words">
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
                                  <p className="text-sm text-slate-600 whitespace-pre-wrap break-words">
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
                            <Textarea
                              placeholder="댓글을 입력하세요..."
                              value={commentInput}
                              onChange={(e) =>
                                setCommentInputs((prev) => ({
                                  ...prev,
                                  [disc.opnId]: e.target.value,
                                }))
                              }
                              className="w-full min-h-[72px] px-3 py-2 text-sm rounded-md border border-slate-200 placeholder:text-slate-400 focus:outline-none focus:border-[var(--sidebar-bg)]"
                            />
                          </div>
                          <div className="px-4 pb-1 bg-white">
                            <div
                              className="border-2 border-dashed border-slate-300 rounded-lg px-3 py-4 text-center text-xs text-slate-500 cursor-pointer hover:border-[var(--sidebar-bg)] hover:text-[var(--sidebar-bg)] hover:bg-slate-50 transition-colors"
                              onClick={() => {
                                setPendingFileOpnId(disc.opnId);
                                commentFileInputRef.current?.click();
                              }}
                              onDragOver={(e) => e.preventDefault()}
                              onDrop={(e) => {
                                e.preventDefault();
                                handleCommentFileAdd(disc.opnId, e.dataTransfer.files);
                              }}
                            >
                              📁 클릭하거나 파일을 여기로 드래그하세요
                            </div>
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
        </div>

        {/* 푸터 */}
        {footer && (
          <DialogFooter className="px-5 py-3">{footer}</DialogFooter>
        )}
      </DialogContent>
    </Dialog>
    <ApprovalHistoryDialog
      open={historyDialogOpen}
      history={detail?.approvalHistory}
      onClose={() => setHistoryDialogOpen(false)}
    />
    <SourceRequestDialog
      workRequestNo={sourceRequestDialogOpen ? (detail?.sourceRequestNo ?? null) : null}
      onClose={() => setSourceRequestDialogOpen(false)}
    />
    {/* PDF 인쇄 전용 뷰(기본정보만) - 화면에는 숨겨져 있다가 window.print() 호출 시에만 렌더링된다.
        body 바로 아래로 포탈해서 렌더링한다: Dialog 트리 안에 두면 나머지 화면을 visibility:hidden으로
        숨겨도 레이아웃 높이는 그대로 남아 인쇄 시 불필요한 빈 페이지가 추가되는 문제가 있었다.
        body의 다른 자식들을 print 시 display:none으로 완전히 제거(app.css)하면 이 문제가 사라진다. */}
    {item && detail && typeof document !== "undefined" && createPortal(
      <div id="print-area" className="hidden print:block p-8 text-black">
        <h1 className="text-lg font-bold mb-1">{item.title}</h1>
        <p className="text-xs text-slate-500 mb-4">{breadcrumb}</p>

        <table className="w-full text-sm border-collapse mb-4">
          <tbody>
            {basicInfoRows.reduce<{ label: string; value: string }[][]>((pairs, row, idx) => {
              if (idx % 2 === 0) pairs.push([row]);
              else pairs[pairs.length - 1].push(row);
              return pairs;
            }, []).map((pair, idx) => (
              <tr key={idx} className="border-b border-slate-200">
                <th className="text-left font-normal text-slate-500 align-top w-24 py-1.5 pr-3">
                  {pair[0].label}
                </th>
                <td className="text-left align-top py-1.5 pr-6">{pair[0].value}</td>
                <th className="text-left font-normal text-slate-500 align-top w-24 py-1.5 pr-3">
                  {pair[1]?.label ?? ""}
                </th>
                <td className="text-left align-top py-1.5">{pair[1]?.value ?? ""}</td>
              </tr>
            ))}
          </tbody>
        </table>

        <div className="text-xs text-slate-700 mb-4">
          개인정보 포함 여부: {detail.isSecret === "Y" ? "예" : "아니오"}
          {detail.isSecret === "Y" && (
            <>
              {" · "}공개구분: {detail.oppbClYn === "Y" ? "대외 게시용" : "기타"}
              {" · "}포함 항목: {detail.userSecretContent || "-"}
              {" · "}파기일: {detail.attachExpireDate || "-"}
            </>
          )}
        </div>

        {detail.changeReason && (
          <div className="mb-4">
            <p className="text-xs text-slate-500 mb-1">지시내용</p>
            <p className="text-sm whitespace-pre-wrap">{detail.changeReason}</p>
          </div>
        )}

        {detail.attachments.length > 0 && (
          <div className="mb-4">
            <p className="text-xs text-slate-500 mb-1">첨부파일</p>
            <ul className="text-sm">
              {detail.attachments.map((att) => (
                <li key={att.seq}>
                  {att.realFileName} ({formatBytes(Number(att.fileSize))})
                </li>
              ))}
            </ul>
          </div>
        )}

        {detail.workResult && (
          <div className="mb-4">
            <p className="text-xs text-slate-500 mb-1">
              조치사항 - {detail.workResult.workerName}
              {detail.workResult.regDt && ` · ${formatDetailDate(detail.workResult.regDt)}`}
            </p>
            <p className="text-sm whitespace-pre-wrap">{detail.workResult.result}</p>
            {detail.workResult.attachments.length > 0 && (
              <ul className="text-sm mt-1">
                {detail.workResult.attachments.map((att) => (
                  <li key={att.seq}>
                    {att.realFileName} ({formatBytes(Number(att.fileSize))})
                  </li>
                ))}
              </ul>
            )}
          </div>
        )}

        <div className="grid grid-cols-2 gap-4 mb-4 text-sm">
          <div>
            <p className="text-xs text-slate-500 mb-1">작성자</p>
            {(() => {
              const w = findApprovedEntry(detail.approvalHistory, "지시서 작성");
              return (
                <p>
                  {w?.name ?? "-"}
                  {w?.regDt && ` (${formatBoxDateTime(w.regDt)})`}
                </p>
              );
            })()}
          </div>
          <div>
            <p className="text-xs text-slate-500 mb-1">승인자</p>
            {(() => {
              const a = findApprovedEntry(detail.approvalHistory, "지시서 승인");
              return (
                <p>
                  {a?.name ?? "-"}
                  {a?.regDt && ` (${formatBoxDateTime(a.regDt)})`}
                </p>
              );
            })()}
          </div>
        </div>
      </div>,
      document.body,
    )}
    </>
  );
};

export default WorkDetailModal;
