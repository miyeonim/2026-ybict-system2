package kepco.prorject.ictyb.back.ictyb_back.qna.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import kepco.prorject.ictyb.back.ictyb_back.qna.model.QnaDto;

public interface QnaService {
    /**
     * Q&A 목록 조회
     */
    List<QnaDto.ListItem> getQnaList();

    /**
     * Q&A 상세 조회
     */
    QnaDto.Detail getQnaDetail(Long noticeNo);

    /**
     * Q&A 등록
     */
    void registerQna(QnaDto.RegisterRequest req, List<MultipartFile> files) throws IOException;

    /**
     * 첨부파일 다운로드
     */
    Resource downloadFile(String noticeNo, String seq) throws MalformedURLException;
}
