package kepco.prorject.ictyb.back.ictyb_back.work_my.service;

import kepco.prorject.ictyb.back.ictyb_back.work_my.model.WorkMyDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface WorkMyService {

    /**
     * 업무지시서(MY) 목록을 조회한다 (로그인 사용자가 관련자로 판단되는 건만).
     */
    List<WorkMyDto.ListItem> getWorksMyList(String sabun);

    /**
     * 현재 결재 대기 중인 사용자를 위한 "다음 단계 담당자" 후보 목록을 조회한다.
     * 로그인 사용자가 해당 건의 현재 결재자가 아니면 예외를 던진다.
     */
    WorkMyDto.NextCandidatesResponse getNextCandidates(String instId, String sabun);

    /**
     * 현재 단계를 승인하고, 지정된 다음 단계 담당자에게 결재를 넘긴다.
     * 현재 단계가 109(작업결과 보고)인 경우 workResult(조치사항)가 필수이며, its_work_result에 저장된다.
     * 이때 isSecret/oppbClYn/userSecretContent/attachExpireDate(조치사항 자체의 개인정보 포함 여부)도
     * its_user_info_secret에 함께 저장된다 (DOC_TYPE=DOC_TYPE_WORK_RESULT).
     * @param files 조치사항 첨부파일 (109단계에서만 사용, 선택, 0개 이상)
     */
    void approve(String instId, String sabun, String sabunName, String nextSabun, String nextName, String workResult,
                 String isSecret, String oppbClYn, String userSecretContent, String attachExpireDate,
                 List<MultipartFile> files) throws IOException;

    /**
     * 반송 시 선택 가능한 이전 결재 단계 목록(및 각 단계로 반송할 때 재배정될 처리자)을 조회한다.
     * 로그인 사용자가 해당 건의 현재 결재자가 아니면 예외를 던진다.
     */
    WorkMyDto.ReturnTargetsResponse getReturnTargets(String instId, String sabun);

    /**
     * 현재 단계를 지정된 이전 단계 처리자에게 반송한다 (사유 필수).
     * @param targetActId 반송 대상 단계 코드. null/blank면 직전 단계로 반송한다.
     */
    void returnToPrevious(String instId, String sabun, String sabunName, String reason, String targetActId);

    /**
     * 업무지시서 등록 폼의 코드성 드롭다운 옵션을 조회한다.
     */
    WorkMyDto.CreateOptions getCreateOptions();

    /**
     * 최초 결재자(한전 파트장) 후보 목록을 조회한다.
     */
    List<WorkMyDto.Candidate> getInitialApproverCandidates();

    /**
     * 작업 요청서(100→102)의 최초 결재자 후보 목록을 조회한다.
     * 요청자와 같은 소속(SOSOK_CD)인 한전 담당자만 후보로 준다.
     */
    List<WorkMyDto.Candidate> getRequestInitialApproverCandidates(String requesterDepCd);

    /**
     * 등록 폼을 열 때 표시할 등록번호(INST_ID)를 미리 예약한다.
     * 실제 등록(createWorkOrder) 시 이 번호를 그대로 넘기면 그 번호가 그대로 사용된다.
     */
    String reserveWorkOrderNo();

    /**
     * 업무지시서를 신규 등록한다 (지시서 작성, ACT_ID=104) 하고,
     * 지정된 최초 결재자(한전 파트장)에게 결재를 넘긴다 (ACT_ID=106 대기).
     * @param files 첨부파일 (선택, 0개 이상)
     * @return 생성된 업무지시서 처리번호(INST_ID)
     */
    String createWorkOrder(String creatorSabun, String creatorName, String creatorDepCd, String creatorDepNm,
                            WorkMyDto.CreateRequest request, List<MultipartFile> files) throws IOException;

    /**
     * 작업 요청서를 신규 등록한다 (요청서 작성, ACT_ID=100은 등록과 동시에 완료 처리) 하고,
     * 지정된 요청서 승인(102, 한전 비IT부서 파트장) 결재자에게 결재를 넘긴다 (ACT_ID=102 대기).
     * its_it_work_report가 아니라 its_real_work_report(RealWorkReportVo)에 저장되며,
     * 102(요청서 승인)/103(요청서 접수) 승인·반송과 103 접수 후 지시서(104) 신규 생성 전환은
     * 아직 구현되어 있지 않다 - 이 메서드는 등록(생성)까지만 처리한다.
     * @param files 첨부파일 (선택, 0개 이상)
     * @return 생성된 요청서 처리번호(INST_ID)
     */
    String createWorkRequest(String creatorSabun, String creatorName, String creatorDepCd, String creatorDepNm,
                              WorkMyDto.CreateWorkRequestReq request, List<MultipartFile> files) throws IOException;

    /**
     * 작업지시서를 수정한다. 지시서 작성(104)/지시서 승인(106) 단계의 현재 결재자만 수정할 수 있다
     * (이 두 단계는 항상 한전 담당자가 처리하므로, 곧 한전 담당자만 수정할 수 있다는 뜻이다).
     * 로그인 사용자가 해당 건의 현재 결재자가 아니거나, 현재 단계가 104/106이 아니면 예외를 던진다.
     * @param newFiles 새로 추가할 첨부파일 (선택, 0개 이상)
     */
    void updateWorkOrder(String instId, String sabun, WorkMyDto.UpdateRequest request,
                          List<MultipartFile> newFiles) throws IOException;

    /**
     * 업무지시서 상세를 조회한다 (등록 정보 + 첨부파일 + 작업결과/조치사항).
     * 결재자를 포함해 상세보기 화면을 여는 누구나 조회할 수 있다 (열람 자체는 관련자 제한 없음).
     */
    WorkMyDto.Detail getDetail(String instId, String sabun);

    /**
     * 결과 보고(109, KDN 대리) 단계가 결재 대기 중일 때, 지시서 작성자(REG_USER_SABUN)가
     * 처리기간(WORK_CHANGE_PERIOD)을 늘린다. 처리예정일(EXPECTED_FINISHED_DT)은
     * 늘어난 일수만큼 자동으로 함께 밀린다.
     * 로그인 사용자가 작성자가 아니거나, 현재 단계가 109가 아니거나, 새 처리기간이 기존보다 크지 않으면 예외를 던진다.
     */
    WorkMyDto.Detail extendWorkPeriod(String instId, String sabun, String newWorkPeriod);

    /**
     * 업무지시서를 삭제한다. 등록자(REG_USER_SABUN) 본인만, 진행 단계와 무관하게 삭제할 수 있다.
     * its_it_work_report 원본 스냅샷을 its_it_work_report_del로 옮겨 담은 뒤 원본 행을 삭제한다.
     * 첨부파일(지시서 첨부 + 작업결과 첨부)은 실물 파일까지 함께 삭제하지만,
     * 결재이력(its_work_history)/결재대기(its_n_sign)/개인정보포함여부(its_user_info_secret)/
     * 작업결과(its_work_result)/업무협의 등 나머지 연관 레코드는 보존한다(추후 추적용, 고아 참조 허용).
     * 로그인 사용자가 등록자가 아니면 예외를 던진다.
     */
    void deleteWorkOrder(String instId, String sabun);

    /**
     * 업무지시서 첨부파일을 다운로드한다.
     */
    WorkMyDto.DownloadFile downloadAttach(String instId, String seq) throws java.net.MalformedURLException;

    /**
     * 작업결과(조치사항) 첨부파일을 다운로드한다.
     */
    WorkMyDto.DownloadFile downloadWorkResultAttach(String instId, String seq) throws java.net.MalformedURLException;

    // ── 작업 요청서(MY) - 등록은 createWorkRequest(위), 여기서부터는 100/102 관련자 조회 +
    // 102(요청서 승인) 승인·반송. 103(요청서 접수) 자체의 승인·반송과 지시서 자동 전환은 아직 없다. ──

    /**
     * 작업 요청서(MY) 목록을 조회한다 (등록자 본인 + 이력상 처리자 + 현재 결재대기자로 판단되는 건만).
     */
    List<WorkMyDto.RequestListItem> getWorkRequestsMyList(String sabun);

    /**
     * 요청서의 다음 단계(103, 한전 IT부서 직원) 담당자 후보를 조회한다.
     * 로그인 사용자가 해당 건의 현재 결재자(102)가 아니면 예외를 던진다.
     */
    WorkMyDto.NextCandidatesResponse getRequestNextCandidates(String instId, String sabun);

    /**
     * 요청서의 현재 단계(102)를 승인하고, 지정된 103 담당자(IT담당자)에게 결재를 넘긴다.
     * 이때 RealWorkReportVo.SEND_IT_SABUN/NAME(IT담당자)이 비로소 채워진다.
     */
    void approveWorkRequest(String instId, String sabun, String sabunName, String nextSabun, String nextName);

    /**
     * 요청서 반송 시 선택 가능한 이전 단계 목록을 조회한다.
     */
    WorkMyDto.ReturnTargetsResponse getRequestReturnTargets(String instId, String sabun);

    /**
     * 요청서의 현재 단계를 지정된 이전 단계 처리자에게 반송한다 (사유 필수).
     */
    void returnWorkRequestToPrevious(String instId, String sabun, String sabunName, String reason, String targetActId);

    /**
     * 작업 요청서 상세를 조회한다 (등록 정보 + 첨부파일 + 결재이력).
     */
    WorkMyDto.RequestDetail getRequestDetail(String instId, String sabun);

    /**
     * 작업 요청서 첨부파일을 다운로드한다.
     */
    WorkMyDto.DownloadFile downloadRequestAttach(String instId, String seq) throws java.net.MalformedURLException;
}
