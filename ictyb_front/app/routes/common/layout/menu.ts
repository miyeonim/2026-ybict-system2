export const menuItems = [
  // 처장(user.deptHead === true)에게만 보이는 탭 - 백엔드에서 KEPCO(JIKGUB_HAN='1직급')/KDN(POSITION_CODE='P005') 기준으로 판별.
  { label: "종합현황 (처장)", path: "/report_total", deptHeadOnly: true },
  { label: "종합세부현황", path: "/report_detail" },
  { label: "작업현황", path: "/works_part" },
  { label: "작업지시서 (ALL)", path: "/works_all" },
  { label: "작업지시서 (MY)", path: "/works_my" },
  // 한전 사람(kepcoYn === "Y")에게만 보이는 탭 - KDN 사람은 요청서를 받는 쪽이라 필요 없다.
  { label: "작업 요청서 (MY)", path: "/works_request_my", kepcoOnly: true },
  { label: "영업 점검일지", path: "/report_daily" },
  { label: "Q&A (참조)", path: "/qna/list" },
];