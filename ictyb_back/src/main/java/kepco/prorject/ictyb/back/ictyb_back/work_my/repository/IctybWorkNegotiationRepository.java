package kepco.prorject.ictyb.back.ictyb_back.work_my.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.IctybWorkNegotiationVo;

/**
 * ictyb_work_negotiation 레포지토리 (업무지시서 협의/피드백 표시 상태)
 */
@Repository
public interface IctybWorkNegotiationRepository extends JpaRepository<IctybWorkNegotiationVo, String> {

    /** 피드백(MY) 대상 판단: 협의 표시된 건 중 관련 instId 목록에 해당하는 것만 조회 */
    List<IctybWorkNegotiationVo> findByInstIdInAndNegotiationYn(List<String> instIds, String negotiationYn);

    /** 본인이 직접 협의를 등록한 건 조회 (결재선 밖이어도 본인 MY 피드백 탭에 표시하기 위함) */
    List<IctybWorkNegotiationVo> findByRegSabunAndNegotiationYn(String regSabun, String negotiationYn);
}
