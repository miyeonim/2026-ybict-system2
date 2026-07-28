import { useEffect, useRef, useState } from "react";
import type {
  WorksMyCandidate,
  WorksMyCreateOptions,
  WorksMyCreateWorkRequestReq,
} from "./WorksMyDto";
import {
  fetchCreateOptions,
  fetchRequestInitialApproverCandidates,
  fetchReservedWorkOrderNo,
  createWorkRequest,
} from "~/hooks/work_my/WorksMyController";
import { fetchWorkRequestDetail, downloadRequestAttach } from "@hooks/work_request_my/WorksRequestMyController";
import type { WorksRequestMyDetail } from "@routes/works_request_my/WorksRequestMyDto";
import { useAuthContext } from "@routes/common/jwt/AuthContext";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { DatePickerField } from "@/components/ui/date-picker-field";
import { Textarea } from "@/components/ui/textarea";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Plus, Paperclip, X } from "lucide-react";

const formatBytes = (bytes: number) => {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
};

const formatDateTime = (dt: string | null) => {
  if (!dt || dt.length < 14) return dt ?? "-";
  return `${dt.slice(0, 4)}-${dt.slice(4, 6)}-${dt.slice(6, 8)} ${dt.slice(8, 10)}:${dt.slice(10, 12)}:${dt.slice(12, 14)}`;
};

const EMPTY_REQUEST_FORM: WorksMyCreateWorkRequestReq = {
  workRequestNo: "",
  changeTitle: "",
  chgRsnCtt: "",
  changeReason: "",
  serviceType: "",
  systemCd: "",
  expectedDt: "",
  isSecret: "N",
  oppbClYn: "Y",
  userSecretContent: "",
  attachExpireDate: "",
  initialApproverSabun: "",
  initialApproverName: "",
};

// 작업 요청서 등록: 요청서 작성(100) 완료 + 요청서 승인(102, 한전 비IT부서 파트장) 결재 대기 생성
// 102/103 승인·반송 화면과 103 접수 후 지시서 자동 생성 전환은 별도 작업으로 남겨두고, 이 다이얼로그는
// 등록까지만 처리한다. IT담당자/작업책임자는 결재 진행에 따라 나중에 채워지는 항목이라 이 화면에서는
// 입력할 수 없는 안내용 placeholder로만 보여준다.
//
// [2026-07-23] mode="view"를 추가해 이미 등록된 요청서를 "다시 조회"할 때도 등록 시점과 같은 화면
// (동일 컴포넌트)을 그대로 재사용하되 모든 입력을 disabled 처리한다. workRequestNo로 상세를 조회해
// form을 채우고, 요청서 승인자 후보 선택 영역(등록 전용 기능)은 숨긴다. 하단 버튼은 승인/반려 등
// 결재 액션이 상세를 조회하는 화면(작업요청서 목록, 작업지시서 원본요청서 보기)마다 달라 부모가
// renderFooter로 넘겨준다.
const CreateWorkRequestDialog: React.FC<{
  open: boolean;
  onClose: () => void;
  onCreated?: () => void;
  mode?: "create" | "view";
  workRequestNo?: string | null;
  renderFooter?: (detail: WorksRequestMyDetail | null) => React.ReactNode;
}> = ({ open, onClose, onCreated, mode = "create", workRequestNo = null, renderFooter }) => {
  const { user } = useAuthContext();
  const readOnly = mode === "view";
  const [form, setForm] = useState<WorksMyCreateWorkRequestReq>(EMPTY_REQUEST_FORM);
  const [detail, setDetail] = useState<WorksRequestMyDetail | null>(null);
  const [options, setOptions] = useState<WorksMyCreateOptions | null>(null);
  const [candidates, setCandidates] = useState<WorksMyCandidate[]>([]);
  const [attachments, setAttachments] = useState<File[]>([]);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (!open) return;
    setError(null);

    if (readOnly) {
      setDetail(null);
      if (!workRequestNo || !user) return;
      setLoading(true);
      fetchWorkRequestDetail(workRequestNo, user.userEmpno)
        .then((d) => {
          setDetail(d);
          setForm({
            workRequestNo: d.workRequestNo,
            changeTitle: d.changeTitle,
            chgRsnCtt: d.chgRsnCtt,
            changeReason: d.changeReason,
            serviceType: d.serviceType ?? "",
            systemCd: d.systemCd ?? "",
            expectedDt: d.expectedDt ?? "",
            isSecret: d.isSecret ?? "N",
            oppbClYn: d.oppbClYn ?? "Y",
            userSecretContent: d.userSecretContent ?? "",
            attachExpireDate: d.attachExpireDate ?? "",
            initialApproverSabun: "",
            initialApproverName: "",
          });
        })
        .catch((e: any) => setError(e.message ?? "상세 조회에 실패했습니다."))
        .finally(() => setLoading(false));
      return;
    }

    if (!user) return;
    setForm(EMPTY_REQUEST_FORM);
    setAttachments([]);
    setLoading(true);
    Promise.all([
      fetchCreateOptions(),
      fetchRequestInitialApproverCandidates(user.depId),
      fetchReservedWorkOrderNo(),
    ])
      .then(([opts, cands, workRequestNo]) => {
        setOptions(opts);
        setCandidates(cands);
        setForm((prev) => ({ ...prev, workRequestNo }));
      })
      .catch((e: any) =>
        setError(e.message ?? "등록 폼 정보를 불러오지 못했습니다."),
      )
      .finally(() => setLoading(false));
  }, [open, readOnly, workRequestNo, user]);

  const update = (field: keyof WorksMyCreateWorkRequestReq, value: string) => {
    if (readOnly) return;
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
    if (!user || !form.changeTitle.trim() || !form.initialApproverSabun) return;
    setSubmitting(true);
    setError(null);
    try {
      await createWorkRequest(user, form, attachments);
      onCreated?.();
    } catch (e: any) {
      setError(e.message ?? "작업 요청서 등록 중 오류가 발생했습니다.");
    } finally {
      setSubmitting(false);
    }
  };

  // 조회 모드에서는 등록 화면처럼 코드표를 새로 불러오지 않고, 상세 조회 결과의 코드/라벨로
  // Select에 표시할 옵션을 즉석에서 구성한다(선택은 어차피 disabled라 다른 항목은 필요 없다).
  const unitSystemOptions = readOnly
    ? detail?.systemCd
      ? [{ code: detail.systemCd, label: detail.systemCdLabel ?? detail.systemCd, businessField: detail.businessField ?? "", drsImptYn: "" }]
      : []
    : options?.unitSystemOptions ?? [];
  const serviceTypeOptions = readOnly
    ? detail?.serviceType
      ? [{ code: detail.serviceType, label: detail.serviceTypeLabel ?? detail.serviceType }]
      : []
    : options?.serviceTypeOptions ?? [];

  const selectedUnitSystem = unitSystemOptions.find((o) => o.code === form.systemCd);
  const businessField = readOnly ? detail?.businessField ?? "" : selectedUnitSystem?.businessField ?? "";

  return (
    <Dialog open={open} onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="max-w-5xl sm:max-w-5xl max-h-[95vh] overflow-y-auto">
        <div className="text-xs font-medium text-slate-400">
          요청번호 {form.workRequestNo || "발급 중..."}
        </div>
        <DialogTitle>{readOnly ? "작업 요청서 상세" : "작업 요청서 등록"}</DialogTitle>

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
            <div className="grid grid-cols-2 gap-3">
              <div className="flex flex-col gap-1.5">
                <Label>요청자 (사번)</Label>
                <Input
                  value={
                    readOnly
                      ? detail
                        ? `${detail.regUserName} (${detail.regUserSabun}) · ${detail.regUserDepNm ?? "-"}`
                        : ""
                      : user
                      ? `${user.empNm} (${user.userEmpno})`
                      : ""
                  }
                  disabled
                  readOnly
                />
              </div>
              <div className="flex flex-col gap-1.5">
                <Label>요청 일시</Label>
                <Input
                  value={readOnly ? (detail ? formatDateTime(detail.regDt) : "") : "등록 시 자동으로 기록됩니다"}
                  disabled
                  readOnly
                />
              </div>
              <div className="flex flex-col gap-1.5">
                <Label>처리 상태</Label>
                <Input
                  value={
                    readOnly
                      ? detail
                        ? detail.currentActId
                          ? "결재 진행 중"
                          : "완료"
                        : ""
                      : "등록 후 결재 진행에 따라 표시됩니다"
                  }
                  disabled
                  readOnly
                />
              </div>
              <div className="flex flex-col gap-1.5">
                <Label>희망 완료일</Label>
                <DatePickerField
                  value={form.expectedDt}
                  onChange={(v) => update("expectedDt", v)}
                  className="w-full"
                  disabled={readOnly}
                />
              </div>
              <div className="flex flex-col gap-1.5">
                <Label>단위시스템 (업무분야)</Label>
                <Select
                  value={form.systemCd}
                  onValueChange={(v: string) => update("systemCd", v)}
                  disabled={readOnly}
                >
                  <SelectTrigger className="w-full">
                    <SelectValue placeholder="선택하세요" />
                  </SelectTrigger>
                  <SelectContent>
                    {unitSystemOptions.map((opt) => (
                      <SelectItem key={opt.code} value={opt.code}>
                        {opt.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="flex flex-col gap-1.5">
                <Label>업무 분야</Label>
                <Input
                  value={businessField}
                  disabled
                  readOnly
                  placeholder="단위시스템을 선택하면 자동으로 채워집니다"
                />
              </div>
              <div className="flex flex-col gap-1.5">
                <Label>서비스유형</Label>
                <Select
                  value={form.serviceType}
                  onValueChange={(v: string) => update("serviceType", v)}
                  disabled={readOnly}
                >
                  <SelectTrigger className="w-full">
                    <SelectValue placeholder="선택하세요" />
                  </SelectTrigger>
                  <SelectContent>
                    {serviceTypeOptions.map((opt) => (
                      <SelectItem key={opt.code} value={opt.code}>
                        {opt.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="flex flex-col gap-1.5">
                <Label>IT담당자</Label>
                <Input
                  value={
                    readOnly
                      ? detail?.sendItName
                        ? `${detail.sendItName} (${detail.sendItSabun})`
                        : "결재 진행 후 배정됩니다"
                      : "결재 진행 후 배정됩니다"
                  }
                  disabled
                  readOnly
                />
              </div>
              <div className="flex flex-col gap-1.5">
                <Label>작업책임자</Label>
                <Input
                  value={
                    readOnly
                      ? detail?.workName
                        ? `${detail.workName} (${detail.workSabun})`
                        : "결재 진행 후 자동으로 지정됩니다"
                      : "결재 진행 후 자동으로 지정됩니다"
                  }
                  disabled
                  readOnly
                />
              </div>
            </div>

            <div className="border-t border-slate-200 pt-4 flex flex-col gap-4">
              <p className="text-sm font-medium text-slate-700">요청 내역</p>

              <div className="flex flex-col gap-1.5">
                <Label>제목</Label>
                <Input
                  value={form.changeTitle}
                  onChange={(e) => update("changeTitle", e.target.value)}
                  placeholder="요청 제목을 입력하세요"
                  disabled={readOnly}
                />
              </div>

              <div className="flex flex-col gap-1.5">
                <Label>목적 및 근거</Label>
                <Textarea
                  value={form.chgRsnCtt}
                  onChange={(e) => update("chgRsnCtt", e.target.value)}
                  placeholder="요청 목적 및 근거를 입력하세요"
                  className="min-h-32"
                  disabled={readOnly}
                />
              </div>

              <div className="flex flex-col gap-1.5">
                <Label>요청사항</Label>
                <Textarea
                  value={form.changeReason}
                  onChange={(e) => update("changeReason", e.target.value)}
                  placeholder="요청사항을 입력하세요"
                  className="min-h-32"
                  disabled={readOnly}
                />
              </div>

              <div className="flex flex-col gap-1.5">
                <Label>첨부파일</Label>
                {readOnly ? (
                  detail && detail.attachments.length === 0 ? (
                    <span className="text-xs text-slate-300">첨부파일이 없습니다.</span>
                  ) : (
                    <ul className="flex flex-col gap-1.5">
                      {(detail?.attachments ?? []).map((a) => (
                        <li key={a.seq}>
                          <button
                            type="button"
                            className="flex items-center gap-1.5 text-sm text-[var(--sidebar-bg)] hover:underline"
                            onClick={() => downloadRequestAttach(detail!.workRequestNo, a.seq, a.realFileName)}
                          >
                            <Paperclip className="size-3.5" />
                            {a.realFileName}
                          </button>
                        </li>
                      ))}
                    </ul>
                  )
                ) : (
                  <>
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
                  </>
                )}
              </div>
            </div>

            <div className="flex flex-col gap-2 border border-slate-200 rounded-lg p-3">
              <div className="flex flex-wrap items-start gap-4">
                <div className="flex items-center gap-2 shrink-0">
                  <Checkbox
                    id="create-request-is-secret"
                    checked={form.isSecret === "Y"}
                    disabled={readOnly}
                    onCheckedChange={(checked) =>
                      update("isSecret", checked === true ? "Y" : "N")
                    }
                  />
                  <Label htmlFor="create-request-is-secret" className="cursor-pointer whitespace-nowrap">
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
                    disabled={readOnly || form.isSecret !== "Y"}
                    className="flex flex-col gap-1.5 shrink-0"
                  >
                    <div className="flex items-center gap-1.5">
                      <RadioGroupItem value="Y" id="create-request-oppb-y" />
                      <Label htmlFor="create-request-oppb-y" className="cursor-pointer font-normal whitespace-nowrap">
                        1. 대외 게시용
                      </Label>
                    </div>
                    <div className="flex items-center gap-1.5">
                      <RadioGroupItem value="N" id="create-request-oppb-n" />
                      <Label htmlFor="create-request-oppb-n" className="cursor-pointer font-normal whitespace-nowrap">
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
                    disabled={readOnly || form.isSecret !== "Y"}
                    placeholder="예: 성명, 연락처"
                  />
                </div>
              </div>

              <div className="flex items-center gap-2">
                <Label className="whitespace-nowrap">파기일</Label>
                <DatePickerField
                  value={form.attachExpireDate}
                  onChange={(v) => update("attachExpireDate", v)}
                  disabled={readOnly || form.isSecret !== "Y"}
                />
              </div>
            </div>

            {!readOnly && (
              <div className="flex flex-col gap-1.5">
                <Label>요청서 승인자 (한전 비IT부서 파트장)</Label>
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
            )}
          </div>
        )}

        <DialogFooter>
          {readOnly ? (
            renderFooter ? (
              renderFooter(detail)
            ) : (
              <Button variant="outline" onClick={onClose}>
                닫기
              </Button>
            )
          ) : (
            <>
              <Button variant="outline" onClick={onClose} disabled={submitting}>
                취소
              </Button>
              <Button
                className="bg-[var(--sidebar-bg)] hover:bg-[var(--sidebar-bg)]/90 text-white"
                disabled={submitting || loading || !form.changeTitle.trim() || !form.initialApproverSabun}
                onClick={handleSubmit}
              >
                <Plus className="size-4" />
                등록
              </Button>
            </>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default CreateWorkRequestDialog;
