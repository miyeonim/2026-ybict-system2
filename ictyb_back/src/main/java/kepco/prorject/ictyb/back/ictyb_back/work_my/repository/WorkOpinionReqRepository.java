package kepco.prorject.ictyb.back.ictyb_back.work_my.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.WorkOpinionReqVo;

/**
 * its_work_opinion_req 레포지토리 (의견요청내역 - MY 피드백)
 */
@Repository
public interface WorkOpinionReqRepository extends JpaRepository<WorkOpinionReqVo, String> {

    /** 피드백 탭: 작업지시서 기준 의견요청 조회 */
    List<WorkOpinionReqVo> findByInstrNo(String instrNo);

    /** 관련자 판단 후 instrNo 목록으로 일괄 조회 */
    List<WorkOpinionReqVo> findByInstrNoIn(List<String> instrNos);
}
