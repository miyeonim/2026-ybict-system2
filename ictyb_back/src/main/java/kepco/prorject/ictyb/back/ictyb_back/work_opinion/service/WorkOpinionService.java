package kepco.prorject.ictyb.back.ictyb_back.work_opinion.service;

import kepco.prorject.ictyb.back.ictyb_back.work_opinion.model.WorkOpinionDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface WorkOpinionService {

    /** 지시번호(instrNo)에 연결된 협의 스레드 목록 + 각 스레드의 댓글 목록 조회 */
    List<WorkOpinionDto.DiscussionItem> getDiscussions(String instrNo);

    /**
     * 새 협의 스레드 생성 (첨부파일 포함, 0개 이상).
     * 첨부파일이 있으면 그 파일들을 담을 최초 댓글을 함께 생성한다.
     */
    WorkOpinionDto.DiscussionItem createDiscussion(WorkOpinionDto.CreateDiscussionReq req, List<MultipartFile> files)
            throws IOException;

    /** 댓글 등록 (첨부파일 포함, 0개 이상) */
    WorkOpinionDto.CommentItem addComment(WorkOpinionDto.CreateCommentReq req, List<MultipartFile> files) throws IOException;

    /** 댓글 첨부파일 다운로드 */
    WorkOpinionDto.DownloadFile downloadAttach(String cmntId, Long seqNo) throws MalformedURLException;

    /** 지시번호에 연결된 모든 협의 스레드를 해당 사용자 기준으로 지금 시각에 읽음 처리한다. */
    void markRead(String instrNo, String sabun);

    /**
     * 주어진 지시번호 목록 중, 해당 사용자가 아직 읽지 않은 활동(신규 협의 또는 댓글)이 있는
     * 지시번호만 추려서 반환한다. 작업지시서(MY) "협의" 탭의 new! 배지 판단에 사용한다.
     */
    Set<String> getUnreadInstrNos(List<String> instrNos, String sabun);

    /**
     * 주어진 지시번호별로 "최종 협의 활동 시각"(그 지시번호에 달린 모든 협의 스레드/댓글 중 가장 최근 등록일시)을
     * 계산한다. 협의가 하나도 없는 지시번호는 결과 맵에 포함되지 않는다.
     * 작업지시서(MY) 목록의 협의 최신순 정렬에 사용한다.
     */
    Map<String, LocalDateTime> getLastActivityByInstrNo(List<String> instrNos);
}
