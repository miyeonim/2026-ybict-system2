package kepco.prorject.ictyb.back.ictyb_back.work_my.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.WorkOpinionVo;

/**
 * its_work_opinion 레포지토리 (의견/답변 - MY 피드백)
 */
@Repository
public interface WorkOpinionRepository extends JpaRepository<WorkOpinionVo, String> {

    /** 피드백 탭: 답변 여부 확인용 (opnId로 의견요청과 매칭) */
    List<WorkOpinionVo> findByInstrNo(String instrNo);

    /** 관련자 판단 후 instrNo 목록으로 일괄 조회 */
    List<WorkOpinionVo> findByInstrNoIn(List<String> instrNos);
}
