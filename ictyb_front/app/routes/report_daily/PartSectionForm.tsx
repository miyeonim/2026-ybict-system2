import React, { useRef } from "react";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import type { PartSectionContent } from "@hooks/report_daily/type";
import { downloadAttach } from "@hooks/report_daily/ReportDailyController";
import PersonCountTable from "@routes/report_daily/PersonCountTable";

interface PartSectionFormProps {
  section: PartSectionContent;
  onChange?: (section: PartSectionContent) => void;
  /** 서버에 저장된 첨부파일 다운로드에 사용 (아직 아무도 제출하지 않은 날짜라면 없음) */
  reportId?: number | null;
}

const formatBytes = (bytes: number) => {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
};

const TEXT_SECTIONS: {
  key: "efficiencyContent" | "mainInstructionContent" | "wasErrorContent" | "meetingSchedule" | "specialNotes";
  label: string;
  rows: number;
}[] = [
  { key: "efficiencyContent", label: "유지보수 관리 효율화 및 주요 배포내용", rows: 3 },
  { key: "mainInstructionContent", label: "주요 작업지시 내용", rows: 3 },
  { key: "wasErrorContent", label: "WAS 오류 내역 (10건 이상 발생건)", rows: 3 },
  { key: "meetingSchedule", label: "회의예정", rows: 2 },
  { key: "specialNotes", label: "특이사항", rows: 2 },
];

export const STATUS_LABEL: Record<PartSectionContent["status"], string> = {
  DRAFT: "작성 전",
  SUBMITTED: "파트장 결재 대기",
  PART_APPROVED: "부장 결재 대기",
  FINAL_APPROVED: "승인 완료",
  REJECTED: "반려됨",
};

export default function PartSectionForm({
  section,
  onChange,
  reportId,
}: PartSectionFormProps) {
  const fileInputRef = useRef<HTMLInputElement>(null);

  const editable = section.canEdit;

  const update = (patch: Partial<PartSectionContent>) => {
    onChange?.({ ...section, ...patch });
  };

  const handleChangePerson = (index: number, field: "inProgress" | "delayed" | "distributed", value: number) => {
    update({ people: section.people.map((p, i) => (i === index ? { ...p, [field]: value } : p)) });
  };

  const handleFileAdd = (newFiles: FileList | null) => {
    if (!newFiles) return;
    update({
      attachments: [
        ...section.attachments,
        ...Array.from(newFiles).map((f) => ({ name: f.name, size: f.size, file: f })),
      ],
    });
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  const handleFileRemove = (index: number) => {
    update({ attachments: section.attachments.filter((_, i) => i !== index) });
  };

  return (
    <div className="flex flex-col gap-4">
      <StatusBanner section={section} />

      {/* 인원별 작업지시 및 배포 건수 */}
      <div className="flex flex-col gap-2">
        <SectionLabel>인원별 작업지시 및 배포 건수</SectionLabel>
        <PersonCountTable
          people={section.people}
          editable={editable}
          onChangePerson={handleChangePerson}
        />
      </div>

      {/* 텍스트 섹션들 */}
      {TEXT_SECTIONS.map((ts) => (
        <div key={ts.key} className="flex flex-col gap-1.5">
          <SectionLabel>{ts.label}</SectionLabel>
          {editable ? (
            <Textarea
              value={section[ts.key]}
              onChange={(e) => update({ [ts.key]: e.target.value } as Partial<PartSectionContent>)}
              placeholder="내용을 입력하세요"
              rows={ts.rows}
            />
          ) : (
            <p className="text-sm text-slate-700 whitespace-pre-wrap bg-slate-50 border border-border rounded-lg px-3 py-2 min-h-[40px]">
              {section[ts.key] || "내용 없음"}
            </p>
          )}
        </div>
      ))}

      {/* 첨부파일 */}
      <div className="flex flex-col gap-1.5">
        <SectionLabel>첨부파일</SectionLabel>
        {editable && (
          <>
            <Button
              type="button"
              variant="outline"
              onClick={() => fileInputRef.current?.click()}
              className="w-fit"
            >
              📎 파일 선택
            </Button>
            <input
              ref={fileInputRef}
              type="file"
              multiple
              className="hidden"
              onChange={(e) => handleFileAdd(e.target.files)}
            />
          </>
        )}
        {section.attachments.length === 0 ? (
          <p className="text-sm text-muted-foreground">첨부된 파일이 없습니다.</p>
        ) : (
          <ul className="flex flex-col gap-1.5 mt-1">
            {section.attachments.map((file, idx) => {
              const downloadable = !editable && file.seq && reportId != null;
              return (
                <li
                  key={idx}
                  className="flex items-center gap-2 bg-slate-50 border border-slate-200 rounded-md px-3 py-2 text-sm"
                >
                  {downloadable ? (
                    <button
                      type="button"
                      onClick={() => downloadAttach(reportId!, section.partId, file.seq!, file.name)}
                      className="flex-1 truncate text-left text-[#3A6499] hover:underline"
                    >
                      📎 {file.name}
                    </button>
                  ) : (
                    <span className="flex-1 truncate text-slate-700">📎 {file.name}</span>
                  )}
                  <span className="text-slate-400 text-xs whitespace-nowrap">{formatBytes(file.size)}</span>
                  {editable && (
                    <button
                      type="button"
                      onClick={() => handleFileRemove(idx)}
                      className="text-slate-400 hover:text-red-500 transition-colors text-xs px-1"
                    >
                      ✕
                    </button>
                  )}
                </li>
              );
            })}
          </ul>
        )}
      </div>
    </div>
  );
}

function StatusBanner({ section }: { section: PartSectionContent }) {
  if (section.status === "REJECTED") {
    return (
      <div className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-xs text-red-600 space-y-0.5">
        <p className="font-semibold">
          반려됨 ({section.rejectedByRole === "HEAD" ? "부장" : "파트장"} {section.rejectedByName})
        </p>
        {section.rejectReason && <p>사유: {section.rejectReason}</p>}
        {section.canEdit && <p className="text-red-500">내용을 수정한 뒤 다시 제출해주세요.</p>}
      </div>
    );
  }

  if (section.status === "DRAFT" && section.myRole === "VIEWER") {
    return (
      <div className="rounded-lg border border-border bg-slate-50 px-3 py-2 text-xs text-muted-foreground">
        아직 작성되지 않았습니다.
      </div>
    );
  }

  if (section.status === "SUBMITTED" && section.myRole !== "LEADER") {
    return (
      <div className="rounded-lg border border-border bg-slate-50 px-3 py-2 text-xs text-muted-foreground">
        파트장 결재 대기 중입니다. (작성자: {section.authorName})
      </div>
    );
  }

  if (section.status === "PART_APPROVED" && section.myRole !== "HEAD") {
    return (
      <div className="rounded-lg border border-border bg-slate-50 px-3 py-2 text-xs text-muted-foreground">
        부장 최종 결재 대기 중입니다. (파트장 승인: {section.partLeaderName})
      </div>
    );
  }

  if (section.status === "FINAL_APPROVED") {
    return (
      <div className="rounded-lg border border-emerald-200 bg-emerald-50 px-3 py-2 text-xs text-emerald-600">
        최종 승인 완료 (파트장 {section.partLeaderName} / 부장 {section.headName})
      </div>
    );
  }

  return null;
}

function SectionLabel({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex items-center gap-1.5 text-sm font-semibold text-[#1C2D4F]">
      <span className="inline-block w-2 h-2 rounded-full border border-[#3A6499]" />
      {children}
    </div>
  );
}
