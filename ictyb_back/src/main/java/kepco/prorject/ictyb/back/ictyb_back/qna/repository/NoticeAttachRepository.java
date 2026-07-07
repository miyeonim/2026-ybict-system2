package kepco.prorject.ictyb.back.ictyb_back.qna.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.qa.NoticeAttachVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.qa.pk.NoticeAttachPk;

import java.util.List;

@Repository
public interface NoticeAttachRepository extends JpaRepository<NoticeAttachVo, NoticeAttachPk> {
    /**
     * 문서번호로 첨부파일 목록 조회
     */
    List<NoticeAttachVo> findByNoticeNoOrderBySeqAsc(String noticeNo);

    /**
     * 문서번호별 첨부파일 수 조회
     */
    int countByNoticeNo(String noticeNo);
}
