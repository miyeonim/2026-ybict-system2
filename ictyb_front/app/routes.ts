import { type RouteConfig, index, layout, route } from "@react-router/dev/routes";

export default [
  route("/common/jwt", "routes/common/jwt/jwt.tsx"),              //로그인

  layout("routes/common/layout/mainlayout.tsx", [
      index("routes/home.tsx"),
      //화면 순서에 맟줘서 추가
      route("/common/main", "routes/common/main/main.tsx"),                       //temp 
      route("/report_total", "routes/report_total/ReportTotalMain.tsx"),          //종합현황    메인
      route("/report_detail", "routes/report_detail/ReportDetailMain.tsx"),       //종합세부현황 메인
      route("/works_part", "routes/works_part/WorksPartMain.tsx"),                //작업현황.   메인
      route("/works_all", "routes/works_all/WorksAllMain.tsx"),                   //작업지시서(ALL) 메인
      route("/works_my", "routes/works_my/WorksMyMain.tsx"),                      //작업지시서(MY) 메인
      route("/works_request_my", "routes/works_request_my/WorksRequestMyMain.tsx"), //작업 요청서(MY) 메인 (한전 전용)
      route("/report_daily", "routes/report_daily/ReportDailyMain.tsx"),          //영업점검일지 목록
      route("/report_daily/view/:date", "routes/report_daily/ReportDailyView.tsx"), //영업점검일지 작성/결재 (날짜 기준)
      route("/qna/list", "routes/qna/list/QnaList.tsx"),                          //Q&A
    ]),

] satisfies RouteConfig;