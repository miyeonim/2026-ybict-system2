export interface ReportDetailDto {
  code: string;
  name: string;
  startDate: string; // 백엔드에서 yyyy-MM-dd 문자열로 넘어오므로 string 권장
  endDate: string;
  duration: number;
  department: string;
  status: string;
  approval: string;
}

export interface SelectedWork extends ReportDetailDto {
  dept: string;
  assignee: string;
  status: string;
  approval: string;
}

// 2. 날짜 연산 유틸리티
// string("yyyy-MM-dd")을 Date 객체로 변환하여 계산용으로 사용
const parseDate = (dateStr: string) => new Date(dateStr);