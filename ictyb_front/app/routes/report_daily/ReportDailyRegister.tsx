import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { ChevronLeft } from "lucide-react";
import { useAuthContext } from "@routes/common/jwt/AuthContext";
import { createEmptyPartSection, isPartSectionFilled } from "@hooks/report_daily/type";
import type { PartOption, PartSectionContent } from "@hooks/report_daily/type";
import { fetchPartOptions, fetchPersonStats, registerReport } from "@hooks/report_daily/ReportDailyController";
import PartSectionForm from "@routes/report_daily/PartSectionForm";

const todayStr = () => {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
};

export default function ReportDailyRegister() {
  const navigate = useNavigate();
  const { user } = useAuthContext();

  const [date, setDate] = useState(todayStr());
  const [parts, setParts] = useState<PartOption[]>([]);
  const [activePartId, setActivePartId] = useState<string>("");
  const [sections, setSections] = useState<Record<string, PartSectionContent>>({});
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchPartOptions()
      .then(async (options) => {
        setParts(options);
        if (options.length === 0) return;
        setActivePartId(options[0].partId);

        const initialSections: Record<string, PartSectionContent> = {};
        await Promise.all(
          options.map(async (p) => {
            const people = await fetchPersonStats(p.partId).catch(() => []);
            initialSections[p.partId] = { ...createEmptyPartSection(p), people };
          })
        );
        setSections(initialSections);
      })
      .catch((e) => console.error("영업 점검일지 파트 목록 조회 실패", e))
      .finally(() => setLoading(false));
  }, []);

  const handleSectionChange = (updated: PartSectionContent) => {
    setSections((prev) => ({ ...prev, [updated.partId]: updated }));
  };

  const handleSubmit = async () => {
    const filledCount = parts.filter((p) => sections[p.partId] && isPartSectionFilled(sections[p.partId])).length;
    if (filledCount === 0) {
      alert("최소 1개 파트 이상 내용을 입력해주세요.");
      return;
    }
    setSubmitting(true);
    try {
      await registerReport(date, parts.map((p) => sections[p.partId]));
      navigate("/report_daily");
    } catch (e) {
      console.error("영업 점검일지 등록 실패", e);
      alert("영업 점검일지 등록 중 오류가 발생했습니다.");
    } finally {
      setSubmitting(false);
    }
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
      <div className="flex items-center justify-between flex-wrap gap-3">
        <div className="flex items-center gap-2">
          <button
            onClick={() => navigate("/report_daily")}
            className="flex items-center gap-1 text-sm text-muted-foreground hover:text-[#1C2D4F] transition-colors"
          >
            <ChevronLeft size={16} /> 목록으로
          </button>
          <h1 className="text-lg font-bold text-[#1C2D4F] ml-2">영업 점검일지 등록</h1>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" onClick={() => navigate("/report_daily")}>취소</Button>
          <Button onClick={handleSubmit} disabled={submitting} className="bg-[#1C2D4F] hover:bg-[#3A6499] text-white min-w-[80px]">
            {submitting ? "등록 중..." : "등록"}
          </Button>
        </div>
      </div>

      {/* 작성일 / 작성자 */}
      <Card className="border border-border/60 shadow-md">
        <CardContent className="px-5 py-4">
          <div className="grid grid-cols-2 gap-3 max-w-md">
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="reportDate">작성일</Label>
              <Input id="reportDate" type="date" value={date} onChange={(e) => setDate(e.target.value)} />
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="reportAuthor">작성자</Label>
              <Input id="reportAuthor" value={user?.empNm || ""} readOnly />
            </div>
          </div>
        </CardContent>
      </Card>

      {/* 파트별 입력 */}
      <Card className="border border-border/60 shadow-md">
        <CardHeader className="pb-2 pt-4 px-5">
          <CardTitle className="text-sm font-semibold">파트별 점검일지 작성</CardTitle>
          <p className="text-xs text-muted-foreground">모든 파트를 한 번에 작성한 뒤 상단 등록 버튼으로 한꺼번에 제출합니다.</p>
        </CardHeader>
        <CardContent className="px-5 pb-5 pt-2">
          <Tabs value={activePartId} onValueChange={setActivePartId}>
            <TabsList variant="line" className="border-b border-border w-full justify-start h-auto p-0 flex-wrap">
              {parts.map((p) => (
                <TabsTrigger key={p.partId} value={p.partId} className="px-4 py-2 text-sm gap-1.5">
                  {p.partNm}
                  {sections[p.partId] && isPartSectionFilled(sections[p.partId]) && (
                    <span className="inline-block w-1.5 h-1.5 rounded-full bg-[#3A6499]" />
                  )}
                </TabsTrigger>
              ))}
            </TabsList>
          </Tabs>

          <div className="pt-4">
            {activeSection && (
              <PartSectionForm
                section={activeSection}
                editable
                onChange={handleSectionChange}
              />
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
