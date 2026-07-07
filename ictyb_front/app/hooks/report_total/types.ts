

////////////[ DeptCompletionChartController ]//////////////////
// 백엔드의 DeptPartDTO와 매핑
export interface DeptPart {
  label: string;  // 파트 이름
  total: number;  // 총 건수
  done: number;   // 완료 건수
  pending: number; // 미처리 건수
  pct: number;    // 완료율
}

// 백엔드의 DeptSectionDTO와 매핑
export interface DeptSection {
  title: string;          // 부서명
  icon: string;           // 아이콘
  data: DeptPart[];       // 해당 부서의 파트 리스트
}

////////////[ MonthlyChartController ]//////////////////

export interface ChartData {
  month: string;
  currentYY: number | null;
  prevYY: number | null;
}

////////////[ RankChartController ]//////////////////
export interface RankItem {
  num: number;
  name: string;
  sub: string;
  total: number;
  done: number;
  pending: number;  
  pct: number;
}

////////////[ AlertController ]//////////////////
export interface AlertItem {
  /** 알림의 종류 (장기 미처리 또는 마감 임박) */
  type: "long" | "due";
  
  /** UI에 표시될 태그명 (예: "장기 미처리", "마감 임박") */
  tag: string;
  
  /** 담당 부서명 (예: "배전·GIS") */
  dept: string;
  
  /** 작업지시서 제목 */
  title: string;
  
  /** 우측에 표시될 날짜/경과일 정보 (예: "10개월 경과", "2일 후 마감") */
  date: string;
  
  /** [선택적] 백엔드 조회용 고유 인스턴스 ID */
  instId?: string;
  
  /** [선택적] 백엔드 조회용 고유 요청 ID */
  reqId?: string;
}