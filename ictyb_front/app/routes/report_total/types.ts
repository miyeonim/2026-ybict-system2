// ─── 공통 색상 팔레트 ────────────────────────────────────────────
export const COLOR = {
  navy:    "#1C2D4F",
  steel:   "#7AAAC8",
  mid:     "#4A7AAA",
  slate:   "#3A6080",
  accent:  "#3A6499",
  pageBg:  "#F0F3F8",
  trackBg: "#E2EAF2",
  white:   "#ffffff",
  border:  "rgba(28,45,79,0.12)",
};

export function getBarColor(pct: number): string {
  if (pct >= 80) return COLOR.navy;
  if (pct >= 65) return COLOR.slate;
  if (pct >= 45) return COLOR.mid;
  return COLOR.steel;
}

// ─── 완료율 ───────────────────────────────────────────────────
export interface DeptPart {
  label: string;     //라벨
  total: number;     //총
  pct: number;       //퍼센트
  done?: number;     //완료 건수
  pending?: number;  //미완료 건수
}

export interface DeptSection {
  title: string;
  icon: string;
  data: DeptPart[];
}

// ─── 월별 ───────────────────────────────────────────────────
export interface MonthlyData {
  year: string;
  month: number;
  cnt: number; 
}

export interface ChartData {
  month: string;
  currentYY: number | null;
  prevYY: number | null;
}

// ─── 랭크 ───────────────────────────────────────────────────
export interface RankItem {
  num: number;
  name: string;      // 부서 이름
  sub: string;       // 분야
  total: number;     // 총 건수
  done: number;      // 완료 건수
  pending: number;   // 미완료 건수
  pct: number;       // 완료율 %
}

