import { useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { ChevronLeft } from "lucide-react";
import type { PartSectionContent } from "@hooks/report_daily/type";
import { fetchReportByDate, submitPart, approvePart, rejectPart } from "@hooks/report_daily/ReportDailyController";
import PartSectionForm, { STATUS_LABEL } from "@routes/report_daily/PartSectionForm";
import { useAuthContext } from "@routes/common/jwt/AuthContext";

export default function ReportDailyView() {
  const navigate = useNavigate();
  const { date } = useParams();
  const { user } = useAuthContext();

  const [reportId, setReportId] = useState<number | null>(null);
  const [partIds, setPartIds] = useState<string[]>([]);
  const [sections, setSections] = useState<Record<string, PartSectionContent>>({});
  const [activePartId, setActivePartId] = useState("");
  const [loading, setLoading] = useState(true);
  const [actionSubmitting, setActionSubmitting] = useState(false);
  const [showRejectInput, setShowRejectInput] = useState(false);
  const [rejectReason, setRejectReason] = useState("");

  const load = useCallback(async () => {
    if (!date || !user) return;
    setLoading(true);
    try {
      const data = await fetchReportByDate(date, user.userEmpno);
      setReportId(data.reportId);
      const map: Record<string, PartSectionContent> = {};
      data.parts.forEach((p) => { map[p.partId] = p; });
      setSections(map);
      setPartIds(data.parts.map((p) => p.partId));
      setActivePartId((prev) => (prev && map[prev] ? prev : (data.parts[0]?.partId ?? "")));
    } catch (e) {
      console.error("영업 점검일지 조회 실패", e);
    } finally {
      setLoading(false);
    }
  }, [date, user]);

  useEffect(() => {
    load();
  }, [load]);

  const handleSectionChange = (updated: PartSectionContent) => {
    setSections((prev) => ({ ...prev, [updated.partId]: updated }));
  };

  const handleSubmit = async () => {
    if (!date || !user) return;
    const section = sections[activePartId];
    if (!section) return;
    setActionSubmitting(true);
    try {
      await submitPart(date, section, user);
      await load();
    } catch (e) {
      alert(e instanceof Error ? e.message : "제출 중 오류가 발생했습니다.");
    } finally {
      setActionSubmitting(false);
    }
  };

  const handleApprove = async () => {
    if (!reportId || !user) return;
    setActionSubmitting(true);
    try {
      await approvePart(reportId, activePartId, user);
      await load();
    } catch (e) {
      alert(e instanceof Error ? e.message : "승인 중 오류가 발생했습니다.");
    } finally {
      setActionSubmitting(false);
    }
  };

  const handleRejectConfirm = async () => {
    if (!rejectReason.trim()) {
      alert("반려 사유를 입력해주세요.");
      return;
    }
    if (!reportId || !user) return;
    setActionSubmitting(true);
    try {
      await rejectPart(reportId, activePartId, user, rejectReason.trim());
      setRejectReason("");
      setShowRejectInput(false);
      await load();
    } catch (e) {
      alert(e instanceof Error ? e.message : "반려 중 오류가 발생했습니다.");
    } finally {
      setActionSubmitting(false);
    }
  };

  const handleTabChange = (partId: string) => {
    setActivePartId(partId);
    setShowRejectInput(false);
    setRejectReason("");
  };

  if (loading) {
    return (
      <div className="min-h-screen p-4 space-y-4" style={{ backgroundColor: "#F0F3F8" }}>
        <p className="text-sm text-muted-foreground">불러오는 중...</p>
      </div>
    );
  }

  const activeSection = sections[activePartId];

  return (
    <div className="min-h-screen p-4 space-y-4" style={{ backgroundColor: "#F0F3F8" }}>

      {/* ── 최상단 헤더 바 ── */}
      <div className="flex items-center gap-2">
        <button
          onClick={() => navigate("/report_daily")}
          className="flex items-center gap-1 text-sm text-muted-foreground hover:text-[#1C2D4F] transition-colors"
        >
          <ChevronLeft size={16} /> 목록으로
        </button>
        <h1 className="text-lg font-bold text-[#1C2D4F] ml-2">영업 점검일지 - {date}</h1>
      </div>

      {/* 작성일 */}
      <Card className="border border-border/60 shadow-md">
        <CardContent className="px-5 py-4 flex items-center text-sm text-muted-foreground">
          <span>작성일 <b className="text-[#1C2D4F]">{date}</b></span>
        </CardContent>
      </Card>

      {/* 파트별 내용 */}
      <Card className="border border-border/60 shadow-md">
        <CardHeader className="pb-2 pt-4 px-5">
          <div className="flex items-start justify-between gap-3 flex-wrap">
            <div>
              <CardTitle className="text-sm font-semibold">파트별 점검일지</CardTitle>
              <p className="text-xs text-muted-foreground mt-1">본인 파트만 작성할 수 있으며, 다른 파트는 읽기전용으로 표시됩니다.</p>
            </div>
            <div className="flex gap-2 shrink-0">
              {activeSection?.canEdit && (
                <Button
                  onClick={handleSubmit}
                  disabled={actionSubmitting}
                  className="bg-[#1C2D4F] hover:bg-[#3A6499] text-white min-w-[80px]"
                >
                  {actionSubmitting ? "제출 중..." : "제출"}
                </Button>
              )}
              {(activeSection?.canApprove || activeSection?.canReject) && !showRejectInput && (
                <>
                  <Button
                    variant="outline"
                    disabled={actionSubmitting}
                    onClick={() => setShowRejectInput(true)}
                    className="text-red-500 border-red-300 hover:bg-red-50"
                  >
                    반려
                  </Button>
                  <Button
                    onClick={handleApprove}
                    disabled={actionSubmitting}
                    className="bg-[#1C2D4F] hover:bg-[#3A6499] text-white"
                  >
                    승인
                  </Button>
                </>
              )}
            </div>
          </div>

          {showRejectInput && (
            <div className="flex flex-col gap-2 mt-3">
              <Textarea
                value={rejectReason}
                onChange={(e) => setRejectReason(e.target.value)}
                placeholder="반려 사유를 입력하세요"
                rows={2}
              />
              <div className="flex gap-2 justify-end">
                <Button
                  variant="outline"
                  disabled={actionSubmitting}
                  onClick={() => { setShowRejectInput(false); setRejectReason(""); }}
                >
                  취소
                </Button>
                <Button
                  disabled={actionSubmitting}
                  onClick={handleRejectConfirm}
                  className="bg-red-500 hover:bg-red-600 text-white"
                >
                  반려 확정
                </Button>
              </div>
            </div>
          )}
        </CardHeader>
        <CardContent className="px-5 pb-5 pt-2">
          <Tabs value={activePartId} onValueChange={handleTabChange}>
            <TabsList variant="line" className="border-b border-border w-full justify-start h-auto p-0 flex-wrap">
              {partIds.map((partId) => {
                const sec = sections[partId];
                return (
                  <TabsTrigger key={partId} value={partId} className="px-4 py-2 text-sm gap-1.5">
                    {sec?.partNm ?? partId}
                    {sec && (
                      <span
                        className={`text-[10px] font-normal ${
                          sec.status === "REJECTED"
                            ? "text-red-500"
                            : sec.status === "FINAL_APPROVED"
                            ? "text-emerald-600"
                            : "text-muted-foreground"
                        }`}
                      >
                        ({STATUS_LABEL[sec.status]})
                      </span>
                    )}
                  </TabsTrigger>
                );
              })}
            </TabsList>
          </Tabs>

          <div className="pt-4">
            {activeSection && (
              <PartSectionForm
                section={activeSection}
                reportId={reportId}
                onChange={handleSectionChange}
              />
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
