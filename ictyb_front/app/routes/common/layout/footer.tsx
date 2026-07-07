export default function Footer() {
  return (
    <footer 
      className="w-full py-8 flex flex-col items-center justify-center"
      style={{ background: "#F0F3F8" }} // 연한 블루 그레이 배경 유지
    >
      <div className="w-full max-w-md h-[1px] bg-slate-200/60 mb-4" />
      <p 
        className="text-[11px] font-medium tracking-wide" 
        style={{ color: "#4A7AAA" }} // 미드 블루 톤 적용
      >
        © 2026 DASHBOARD REPORT. ALL RIGHTS RESERVED.
      </p>
    </footer>
  );
}