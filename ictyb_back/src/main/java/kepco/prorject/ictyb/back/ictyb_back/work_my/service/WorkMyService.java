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
     * @param files 조치사항 첨부파일 (109단계에서만 사용, 선택, 0개 이상)
     */
    void approve(String instId, String sabun, String sabunName, String nextSabun, String nextName, String workResult,
                 List<MultipartFile> files) throws IOException;

    /**
     * 현재 단계를 이전 단계 처리자에게 반송한다 (사유 필수).
     */
    void returnToPrevious(String instId, String sabun, String sabunName, String reason);

    /**
     * 업무지시서 등록 폼의 코드성 드롭다운 옵션을 조회한다.
     */
    WorkMyDto.CreateOptions getCreateOptions();

    /**
     * 최초 결재자(한전 파트장) 후보 목록을 조회한다.
     */
    List<WorkMyDto.Candidate> getInitialApproverCandidates();

    /**
     * 업무지시서를 신규 등록한다 (지시서 작성, ACT_ID=104) 하고,
     * 지정된 최초 결재자(한전 파트장)에게 결재를 넘긴다 (ACT_ID=106 대기).
     * @param files 첨부파일 (선택, 0개 이상)
     * @return 생성된 업무지시서 처리번호(INST_ID)
     */
    String createWorkOrder(String creatorSabun, String creatorName, String creatorDepCd, String creatorDepNm,
                            WorkMyDto.CreateRequest request, List<MultipartFile> files) throws IOException;

    /**
     * 업무지시서 상세를 조회한다 (등록 정보 + 첨부파일 + 작업결과/조치사항).
     * 결재자를 포함해 상세보기 화면을 여는 누구나 조회할 수 있다 (열람 자체는 관련자 제한 없음).
     */
    WorkMyDto.Detail getDetail(String instId, String sabun);

    /**
     * 업무지시서 첨부파일을 다운로드한다.
     */
    WorkMyDto.DownloadFile downloadAttach(String instId, String seq) throws java.net.MalformedURLException;

    /**
     * 작업결과(조치사항) 첨부파일을 다운로드한다.
     */
    WorkMyDto.DownloadFile downloadWorkResultAttach(String instId, String seq) throws java.net.MalformedURLException;
}
