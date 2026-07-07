import React, { useEffect, useRef, useState } from 'react';
import type { QnaRegisterForm } from '~/routes/qna/list/QnaDto';
import { registerQna } from '~/hooks/qna/QnaController';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog';
import { useAuthContext } from "@routes/common/jwt/AuthContext";    //사용자정보가져오기 


interface QnaRegisterModalProps {
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

const INITIAL_FORM: QnaRegisterForm = {
  noticeTitle: '',
  noticeContents: '',
  noticeDepCd: '',
  regUserSabun: '',
  regUserDepCd: '',
  regUserName: '',
  endDt: '',
  priority: 0,
};

const MAX_FILE_SIZE_MB = 10;
const MAX_FILE_COUNT = 5;

type FormErrors = Partial<Record<keyof QnaRegisterForm, string>>;

const QnaRegisterModal: React.FC<QnaRegisterModalProps> = ({ open, onClose, onSuccess }) => {
  const [form, setForm] = useState<QnaRegisterForm>(INITIAL_FORM);
  const [files, setFiles] = useState<File[]>([]);
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<FormErrors>({});
  const [submitError, setSubmitError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const { user } = useAuthContext();                                     //사용자 정보 가져오기 


  // ── 모달이 열릴 때 사용자 정보 자동 세팅 ────────────────────────
  useEffect(() => {
    if (open && user) {
      setForm((prev) => ({
        ...prev,
        regUserName: user.empNm || '',
        regUserSabun: user.userEmpno || '',
        noticeDepCd: user.depTitle || '',
      }));
    } else if (!open) {
      setForm(INITIAL_FORM);
    }
  }, [open, user]);
  // ── 모달 닫기 시 상태 초기화 ──────────────────────────────────
  const handleClose = () => {
    if (loading) return;
    setForm(INITIAL_FORM);
    setFiles([]);
    setErrors({});
    setSubmitError(null);
    onClose();
  };

  // ── 입력 변경 ─────────────────────────────────────────────────
  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
    setErrors((prev) => ({ ...prev, [name]: undefined }));
  };

  // ── 파일 추가 ─────────────────────────────────────────────────
  const handleFileAdd = (newFiles: FileList | null) => {
    if (!newFiles) return;
    const selected = Array.from(newFiles);

    if (files.length + selected.length > MAX_FILE_COUNT) {
      alert(`첨부파일은 최대 ${MAX_FILE_COUNT}개까지 가능합니다.`);
      return;
    }
    if (selected.some((f) => f.size > MAX_FILE_SIZE_MB * 1024 * 1024)) {
      alert(`파일 크기는 최대 ${MAX_FILE_SIZE_MB}MB까지 가능합니다.`);
      return;
    }

    setFiles((prev) => [...prev, ...selected]);
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  const handleFileRemove = (index: number) => {
    setFiles((prev) => prev.filter((_, i) => i !== index));
  };

  const formatBytes = (bytes: number) => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  };

  // ── 유효성 검사 ───────────────────────────────────────────────
  const validate = (): boolean => {
    const newErrors: FormErrors = {};
    if (!form.noticeTitle.trim())    newErrors.noticeTitle    = '제목을 입력해주세요.';
    if (!form.noticeContents.trim()) newErrors.noticeContents = '내용을 입력해주세요.';
    if (!form.regUserName.trim())    newErrors.regUserName    = '작성자 성명을 입력해주세요.';
    if (!form.regUserSabun.trim())   newErrors.regUserSabun   = '사번을 입력해주세요.';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // ── 저장 제출 ─────────────────────────────────────────────────
  const handleSubmit = async () => {
    if (!validate()) return;

    setLoading(true);
    setSubmitError(null);
    try {
      await registerQna(form, files);
      setForm(INITIAL_FORM);
      setFiles([]);
      onSuccess(); // 목록 재조회 + 모달 닫기
    } catch (e: any) {
      setSubmitError(e.message ?? '저장 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={(o) => !o && handleClose()}>
      <DialogContent className="max-w-lg max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="text-lg font-bold text-[var(--sidebar-bg)]">
            Q&amp;A 등록
          </DialogTitle>
        </DialogHeader>

        <div className="flex flex-col gap-4 py-2">
          {/* 서버 에러 */}
          {submitError && (
            <div className="bg-red-50 text-red-600 border border-red-200 rounded-lg px-4 py-3 text-sm">
              {submitError}
            </div>
          )}

          {/* 제목 */}
          <div className="flex flex-col gap-1.5">
            <Label htmlFor="noticeTitle">
              제목 <span className="text-red-500">*</span>
            </Label>
            <Input
              id="noticeTitle"
              name="noticeTitle"
              value={form.noticeTitle}
              onChange={handleChange}
              placeholder="제목을 입력하세요"
              maxLength={100}
              className={errors.noticeTitle ? 'border-red-400 focus-visible:ring-red-400' : ''}
            />
            {errors.noticeTitle && (
              <span className="text-xs text-red-500">{errors.noticeTitle}</span>
            )}
          </div>

          {/* 작성자 + 사번 */}
          <div className="grid grid-cols-2 gap-3">
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="regUserName">
                작성자 <span className="text-red-500">*</span>
              </Label>
              <Input
                id="regUserName"
                name="regUserName"
                value={form.regUserName}
                onChange={handleChange}
                placeholder="성명"
                maxLength={12}
                className={errors.regUserName ? 'border-red-400 focus-visible:ring-red-400' : ''}
                readOnly
              />
              {errors.regUserName && (
                <span className="text-xs text-red-500">{errors.regUserName}</span>
              )}
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="regUserSabun">
                사번 <span className="text-red-500">*</span>
              </Label>
              <Input
                id="regUserSabun"
                name="regUserSabun"
                value={form.regUserSabun}
                onChange={handleChange}
                placeholder="사번 5자리"
                maxLength={5}
                className={errors.regUserSabun ? 'border-red-400 focus-visible:ring-red-400' : ''}
                readOnly
              />
              {errors.regUserSabun && (
                <span className="text-xs text-red-500">{errors.regUserSabun}</span>
              )}
            </div>
          </div>

          {/* 부서코드 + 게시 종료일 */}
          <div className="grid grid-cols-2 gap-3">
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="noticeDepCd">부서명</Label>
              <Input
                id="noticeDepCd"
                name="noticeDepCd"
                value={form.noticeDepCd}
                onChange={handleChange}
                placeholder="부서명"
                maxLength={16}
                readOnly
              />
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="endDt">게시 종료일</Label>
              <Input
                id="endDt"
                name="endDt"
                type="date"
                value={form.endDt}
                onChange={handleChange}
              />
            </div>
          </div>

          {/* 내용 */}
          <div className="flex flex-col gap-1.5">
            <Label htmlFor="noticeContents">
              내용 <span className="text-red-500">*</span>
            </Label>
            <Textarea
              id="noticeContents"
              name="noticeContents"
              value={form.noticeContents}
              onChange={handleChange}
              placeholder="내용을 입력하세요"
              rows={5}
              maxLength={4000}
              className={errors.noticeContents ? 'border-red-400 focus-visible:ring-red-400' : ''}
            />
            <div className="text-xs text-slate-400 text-right">
              {form.noticeContents.length} / 4000
            </div>
            {errors.noticeContents && (
              <span className="text-xs text-red-500">{errors.noticeContents}</span>
            )}
          </div>

          {/* 첨부파일 */}
          <div className="flex flex-col gap-1.5">
            <Label>
              첨부파일{' '}
              <span className="text-slate-400 font-normal text-xs">
                (최대 {MAX_FILE_COUNT}개, 각 {MAX_FILE_SIZE_MB}MB 이하)
              </span>
            </Label>

            {/* 드롭존 */}
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
            <input
              ref={fileInputRef}
              type="file"
              multiple
              className="hidden"
              onChange={(e) => handleFileAdd(e.target.files)}
            />

            {/* 선택된 파일 목록 */}
            {files.length > 0 && (
              <ul className="flex flex-col gap-1.5 mt-1">
                {files.map((file, idx) => (
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
                      className="text-slate-400 hover:text-red-500 transition-colors text-xs px-1"
                    >
                      ✕
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>

        <DialogFooter className="gap-2">
          <Button
            variant="outline"
            onClick={handleClose}
            disabled={loading}
          >
            취소
          </Button>
          <Button
            onClick={handleSubmit}
            disabled={loading}
            className="bg-[var(--sidebar-bg)] hover:bg-[var(--accent-main)] text-white min-w-[80px]"
          >
            {loading ? (
              <span className="flex items-center gap-2">
                <span className="w-4 h-4 border-2 border-white/40 border-t-white rounded-full animate-spin" />
                저장 중...
              </span>
            ) : (
              '저장'
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default QnaRegisterModal;
