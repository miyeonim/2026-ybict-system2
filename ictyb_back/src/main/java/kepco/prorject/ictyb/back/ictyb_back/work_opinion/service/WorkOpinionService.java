package kepco.prorject.ictyb.back.ictyb_back.work_opinion.service;

import kepco.prorject.ictyb.back.ictyb_back.work_opinion.model.WorkOpinionDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

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
}
