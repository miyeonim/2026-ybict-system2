import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { ChevronLeft } from "lucide-react";
import { isPartSectionFilled } from "@hooks/report_daily/type";
import type { SalesDailyReportDetail } from "@hooks/report_daily/type";
import { fetchReportDetail } from "@hooks/report_daily/ReportDailyController";
import PartSectionForm from "@routes/report_daily/PartSectionForm";

export default function ReportDailyDetail() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [report, setReport] = useState<SalesDailyReportDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [activePartId, setActivePartId] = useState<string>("");

  useEffect(() => {
    if (!id) return;
    fetchReportDetail(Number(id))
      .then((data) => {
        setReport(data);
        if (data.parts.length > 0) setActivePartId(data.parts[0].partId);
      })
      .catch((e) => console.error("영업 점검일지 상세 조회 실패", e))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) {
    return (
      <div className="min-h-screen p-4 space-y-4" style={{ backgroundColor: "#F0F3F8" }}>
        <p className="text-sm text-muted-foreground">불러오는 중...</p>
      </div>
    );
  }

  if (!report) {
    return (
      <div className="min-h-screen p-4 space-y-4" style={{ backgroundColor: "#F0F3F8" }}>
        <button
          onClick={() => navigate("/report_daily")}
          className="flex items-center gap-1 text-sm text-muted-foreground hover:text-[#1C2D4F] transition-colors"
        >
          <ChevronLeft size={16} /> 목록으로
        </button>
        <p className="text-sm text-muted-foreground">해당 점검일지를 찾을 수 없습니다.</p>
      </div>
    );
  }

  const activeSection = report.parts.find((p) => p.partId === activePartId) ?? report.parts[0];

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
          <h1 className="text-lg font-bold text-[#1C2D4F] ml-2">영업 점검일지 상세</h1>
        </div>
        <Button variant="outline" onClick={() => navigate("/report_daily")}>닫기</Button>
      </div>

      {/* 작성일 / 작성자 */}
      <Card className="border border-border/60 shadow-md">
        <CardContent className="px-5 py-4 flex items-center justify-between text-sm text-muted-foreground">
          <span>작성일 <b className="text-[#1C2D4F]">{report.reportDate}</b></span>
          <span>작성자 <b className="text-[#1C2D4F]">{report.authorName}</b></span>
        </CardContent>
      </Card>

      {/* 파트별 내용 */}
      <Card className="border border-border/60 shadow-md">
        <CardHeader className="pb-2 pt-4 px-5">
          <CardTitle className="text-sm font-semibold">파트별 점검일지 내역</CardTitle>
        </CardHeader>
        <CardContent className="px-5 pb-5 pt-2">
          <Tabs value={activePartId} onValueChange={setActivePartId}>
            <TabsList variant="line" className="border-b border-border w-full justify-start h-auto p-0 flex-wrap">
              {report.parts.map((sec) => (
                <TabsTrigger key={sec.partId} value={sec.partId} className="px-4 py-2 text-sm gap-1.5">
                  {sec.partNm}
                  {isPartSectionFilled(sec) && (
                    <span className="inline-block w-1.5 h-1.5 rounded-full bg-[#3A6499]" />
                  )}
                </TabsTrigger>
              ))}
            </TabsList>
          </Tabs>

          <div className="pt-4">
            <PartSectionForm
              section={activeSection}
              reportId={report.reportId}
            />
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
