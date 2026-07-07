import { useState } from "react";
import { useNavigate } from "react-router";
import { useAuth } from "@hooks/common/jwt/useAuthController";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";

const PALETTE = {
  navy:    "#1C2D4F",
  accent:  "#3A6499",
  pageBg:  "#F0F3F8",
  border:  "#DDE4EE",
  textMut: "#5A7090",
};

export default function JwtLogin() {
  //1. 로그인 관련 정보
  const [userEmpno, setUserEmpno] = useState("");
  const [password, setPassword] = useState("");
  const { login, loading } = useAuth();

  // 2. 팝업 상태와 메시지 상태 추가
  const [errorOpen, setErrorOpen] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const handleLogin = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const result = await login(userEmpno, password);
    
    if (result.success) {
      console.log("로그인 성공 및 세션 보존 완료");
    } else {
      setErrorMessage(result.message);
      setErrorOpen(true);
    }
  };

  return (
    <div className="flex justify-center items-center min-h-screen" style={{ backgroundColor: PALETTE.pageBg }}>
      <Card className="w-[400px] shadow-xl border-none">
        <CardHeader className="text-center pb-8 border-b" style={{ borderColor: PALETTE.border }}>
          <CardTitle className="text-2xl font-black" style={{ color: PALETTE.navy }}>영배 ICT서비스 종합관리 시스템</CardTitle>
        </CardHeader>
        
        <CardContent className="pt-8">
          <form onSubmit={handleLogin} className="grid gap-6">
            <div className="grid gap-2">
              <Label htmlFor="empno" className="font-bold" style={{ color: PALETTE.textMut }}>사번</Label>
              <Input 
                id="empno" 
                className="h-12 border-[#DDE4EE] focus-visible:ring-[#3A6499]"
                placeholder="사번을 입력하십시오" 
                value={userEmpno}
                onChange={(e) => setUserEmpno(e.target.value)} 
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="password" className="font-bold" style={{ color: PALETTE.textMut }}>비밀번호</Label>
              <Input 
                id="password" 
                type="password" 
                className="h-12 border-[#DDE4EE] focus-visible:ring-[#3A6499]"
                placeholder="비밀번호를 입력하십시오" 
                value={password}
                onChange={(e) => setPassword(e.target.value)} 
              />
            </div>

            <Button 
              type="submit" 
              className="h-12 font-bold text-base transition-all" 
              style={{ backgroundColor: PALETTE.accent }}
              disabled={loading}
            >
              {loading ? "입장 중..." : "로그인"}
            </Button>

            {/* 비밀번호 찾기 버튼 추가 */}
            <div className="text-center mt-[-10px]">
              <button 
                type="button" 
                onClick={() => alert("비밀번호 찾기 기능을 구현하소서.")}
                className="text-sm font-medium hover:underline" 
                style={{ color: PALETTE.textMut }}
              >
                비밀번호를 잊으셨습니까?
              </button>
            </div>
          </form>
        </CardContent>
      </Card>



      {/* 3. 로그인 실패 시 보여질 AlertDialog */}
      <AlertDialog open={errorOpen} onOpenChange={setErrorOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>로그인 알림</AlertDialogTitle>
            <AlertDialogDescription>{errorMessage}</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogAction onClick={() => setErrorOpen(false)}>확인</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

    </div>
  );
}