
export type DeptKey = '전체' | '영업' | '배전' | '기술' | '마이';

export interface WorkPartBarRow {
  name: string;
  완료: number;
  미완료: number;
}

export interface WorkPartSummaryResponse {
  done: number;
  notDone: number;
  receivedTotal: number | null;
  barRows: WorkPartBarRow[];
}

interface FetchSummaryParams {
  year: number;
  type: DeptKey;
  sabun?: string;
}


export const COLOR = {
  // 텍스트/배경 기본
  white: "#FFFFFF",
  navy: "#1C2D4F", // 제목, 강조 텍스트
  steel: "#64748B", // 보조 텍스트 (부서명, 페이지 표시 등)
 
  // 배경/보더
  bgLight: "#F0F3F8", // 페이지 배경
  border: "#E8EEF6", // 카드/구분선 보더
 
  // 상태 색상
  received: "#7AAAC8", // 접수
  progress: "#4A7AAA", // 처리중 (레거시)
  done: "#1C2D4F", // 완료
  notDone: "#4A7AAA", // 미완료
  danger: "#C0392B", // 마감 임박 등 위험/경고
} as const;
 