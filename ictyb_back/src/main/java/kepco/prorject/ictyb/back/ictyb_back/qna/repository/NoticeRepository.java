package kepco.prorject.ictyb.back.ictyb_back.qna.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.qa.NoticeVo;
import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<NoticeVo, Long> {

    /**
     * Q&A 목록 조회 (삭제되지 않은 것, 최신순)
     */
    List<NoticeVo> findByNoticeTypeAndDelYnOrderByNoticeNoDesc(String noticeType, String delYn);

    /**
     * NOTICE_TYPE = 'Q', DEL_YN = 'N' 목록 조회 (최신 등록일 순)
     */
    @Query("SELECT q FROM NoticeVo q " +
           "WHERE q.noticeType = 'Q' AND (q.delYn IS NULL OR q.delYn = 'N') " +
           "ORDER BY q.noticeNo DESC")
    List<NoticeVo> findActiveQnaList();
}

