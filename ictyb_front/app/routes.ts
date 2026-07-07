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
      route("/report_daily", "routes/report_daily/ReportDailyMain.tsx"),          //영업점검일지 목록
      route("/report_daily/register", "routes/report_daily/ReportDailyRegister.tsx"), //영업점검일지 등록
      route("/report_daily/:id", "routes/report_daily/ReportDailyDetail.tsx"),    //영업점검일지 상세
      route("/qna/list", "routes/qna/list/QnaList.tsx"),                          //Q&A
    ]),

] satisfies RouteConfig;