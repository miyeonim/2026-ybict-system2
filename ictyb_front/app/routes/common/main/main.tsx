import { Card, CardTitle } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import { User, AlertCircle, CheckCircle2, PieChart as PieIcon, BarChart2 } from "lucide-react";
import { PieChart, Pie, Cell, ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip, Legend } from "recharts";
import { useAuthContext } from "@routes/common/jwt/AuthContext"; // 🌟 로그인을한 사용자 정보 가져오기

const statusData = [
  { name: "접수", value: 8, color: "#7AAAC8" },
  { name: "처리 중", value: 7, color: "#4A7AAA" },
  { name: "완료", value: 6, color: "#3A6080" }, 
  { name: "피드백", value: 3, color: "#1C2D4F" },
];

const deptData = [
  { name: "영업", 접수: 3, 처리중: 3, 완료: 2, 피드백: 1 },
  { name: "배전", 접수: 3, 처리중: 2, 완료: 2, 피드백: 1 },
  { name: "기술", 접수: 2, 처리중: 2, 완료: 1, 피드백: 2 },
];

const deadlines = [
  { title: "전기요금 청구 오류 긴급 수정 1", due: "오늘" },
  { title: "전기요금 청구 오류 긴급 수정 2", due: "내일" },
  { title: "전기요금 청구 오류 긴급 수정 3", due: "2일 후" },
];

export default function Dashboard() {
  const { user } = useAuthContext(); //로그인한 사용자정보 가져오기


  return (
    <>
    {/* 
      <header className="flex justify-between items-center mb-10">
        <div className="flex items-center gap-3 bg-white px-6 py-3 rounded-xl shadow-sm border font-bold">
          <User size={20} /> <span>{user ? `${user.empNm} (${user.userEmpno})` : "ICT 담당자 (KEPCO)"}</span>
        </div>
      </header>

      */}

      {/* 1. 통계 카드 (크기 확대) */}
      <div className="grid grid-cols-5 gap-6 mb-10">
        {[ {l:"전체 지시서", v:24}, {l:"접수", v:8}, {l:"처리 중", v:7}, {l:"완료", v:6}, {l:"피드백 대기", v:3} ].map((s, i) => (
          <Card key={i} className="text-center p-6 border-[#DDE4EE]">
            <div className="text-4xl font-black text-[#1C2D4F]">{s.v}</div>
            <div className="text-sm text-gray-500 font-bold uppercase mt-0.5 tracking-widest">{s.l}</div>
          </Card>
        ))}
      </div>

      {/* 2. 차트 섹션 (범례 배치) */}
      <div className="grid grid-cols-2 gap-8 mb-8">
        <Card className="p-8">
          <CardTitle className="text-lg font-bold mb-6 flex items-center gap-3"><PieIcon size={20}/> 상태별 현황</CardTitle>
          <div className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie data={statusData} innerRadius={70} outerRadius={100} dataKey="value">
                  {statusData.map((e, i) => <Cell key={i} fill={e.color} />)}
                </Pie>
                <Tooltip />
                <Legend verticalAlign="middle" align="right" layout="vertical" />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </Card>

        <Card className="p-8">
          <CardTitle className="text-lg font-bold mb-6 flex items-center gap-3"><BarChart2 size={20}/> 부서별 처리 현황</CardTitle>
          <ResponsiveContainer width="100%" height={280}>
            <BarChart layout="vertical" data={deptData}>
              <XAxis type="number" hide />
              <YAxis dataKey="name" type="category" fontSize={14} fontWeight={600} />
              <Tooltip />
              <Bar dataKey="접수" stackId="a" fill="#7AAAC8" />
              <Bar dataKey="처리중" stackId="a" fill="#4A7AAA" />
              <Bar dataKey="완료" stackId="a" fill="#3A6080" />
            </BarChart>
          </ResponsiveContainer>
        </Card>
      </div>

      {/* 3. 전체 처리율 & 마감 임박 */}
      <div className="grid grid-cols-2 gap-8">
        <Card className="p-8">
          <CardTitle className="text-lg font-bold mb-6 flex items-center gap-3"><CheckCircle2 size={20} className="text-green-600"/> 전체 처리율 (25%)</CardTitle>
          <Progress value={25} className="h-6" />
          <p className="text-sm font-bold text-gray-600 mt-4">완료 6건 / 전체 24건</p>
        </Card>
        <Card className="p-8">
          <CardTitle className="text-lg font-bold mb-6 flex items-center gap-3 text-[#3A6499]"><AlertCircle size={20}/> 마감 임박 (3일 이내)</CardTitle>
          <div className="space-y-3">
            {deadlines.map((d, i) => (
              <div key={i} className="text-sm font-semibold p-4 bg-white border rounded-lg flex justify-between">
                {d.title} <span className="font-bold text-[#3A6499]">{d.due}</span>
              </div>
            ))}
          </div>
        </Card>
      </div>
    </>
  );
}