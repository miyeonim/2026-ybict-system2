import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow,
} from "@/components/ui/table";
import { ClipboardList, Paperclip } from "lucide-react";
import { fetchReportList } from "@hooks/report_daily/ReportDailyController";
import type { SalesDailyReportListItem } from "@hooks/report_daily/type";

export default function ReportDailyMain() {
  const navigate = useNavigate();
  const [reports, setReports] = useState<SalesDailyReportListItem[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchReportList()
      .then(setReports)
      .catch((e) => console.error("영업 점검일지 목록 조회 실패", e))
      .finally(() => setLoading(false));
  }, []);

  const sortedReports = useMemo(
    () => [...reports].sort((a, b) => (a.reportDate < b.reportDate ? 1 : -1)),
    [reports]
  );

  return (
    <div className="min-h-screen p-4 space-y-4" style={{ backgroundColor: "#F0F3F8" }}>

      {/* ── 최상단 헤더 바 ── */}
      <div className="flex items-center justify-between flex-wrap gap-3">
        <h1 className="text-lg font-bold text-[#1C2D4F]">영업 점검일지</h1>
        <Button
          onClick={() => navigate("/report_daily/register")}
          className="bg-[#1C2D4F] hover:bg-[#3A6499] text-white shadow-md transition-all"
        >
          + 영업 점검일지 등록
        </Button>
      </div>

      {/* ── 점검일지 리스트 ── */}
      <Card className="border border-border/60 shadow-md">
        <CardHeader className="pb-2 pt-4 px-5 flex flex-row items-center justify-between">
          <CardTitle className="text-sm font-semibold flex items-center gap-1.5">
            <ClipboardList size={16} style={{ color: "#3A6499" }} /> 영업 점검일지 목록
          </CardTitle>
          <span className="text-xs text-muted-foreground">총 {sortedReports.length}건</span>
        </CardHeader>
        <CardContent className="px-0 pb-4 pt-1">
          <div className="rounded-lg border border-border/60 bg-white mx-5 overflow-hidden">
            <Table>
              <TableHeader className="bg-slate-50">
                <TableRow>
                  <TableHead className="w-[120px] text-center text-[#1C2D4F] font-bold">작성일</TableHead>
                  <TableHead className="w-[100px] text-center text-[#1C2D4F] font-bold">작성자</TableHead>
                  <TableHead className="text-center text-[#1C2D4F] font-bold">포함 파트</TableHead>
                  <TableHead className="w-[80px] text-center text-[#1C2D4F] font-bold">진행중</TableHead>
                  <TableHead className="w-[80px] text-center text-[#1C2D4F] font-bold">일정지연</TableHead>
                  <TableHead className="w-[70px] text-center text-[#1C2D4F] font-bold">첨부</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {loading ? (
                  <TableRow>
                    <TableCell colSpan={6} className="text-center py-10 text-slate-400">
                      불러오는 중...
                    </TableCell>
                  </TableRow>
                ) : sortedReports.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} className="text-center py-10 text-slate-400">
                      등록된 영업 점검일지가 없습니다.
                    </TableCell>
                  </TableRow>
                ) : (
                  sortedReports.map((report) => (
                    <TableRow
                      key={report.reportId}
                      className="cursor-pointer hover:bg-slate-50 transition-colors"
                      onClick={() => navigate(`/report_daily/${report.reportId}`)}
                    >
                      <TableCell className="text-center">{report.reportDate}</TableCell>
                      <TableCell className="text-center">{report.authorName}</TableCell>
                      <TableCell>
                        <div className="flex flex-wrap gap-1 justify-center">
                          {report.partNames.map((partNm) => (
                            <Badge
                              key={partNm}
                              variant="outline"
                              className="text-[10px] font-semibold text-[#3A6499] border-[#3A6499]/40"
                            >
                              {partNm}
                            </Badge>
                          ))}
                        </div>
                      </TableCell>
                      <TableCell className="text-center font-semibold text-[#3A6499]">{report.totalInProgress}</TableCell>
                      <TableCell className="text-center font-semibold text-red-500">{report.totalDelayed}</TableCell>
                      <TableCell className="text-center text-muted-foreground">
                        {report.attachmentCount > 0 ? (
                          <span className="inline-flex items-center gap-1 text-xs">
                            <Paperclip size={12} /> {report.attachmentCount}
                          </span>
                        ) : (
                          "-"
                        )}
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
