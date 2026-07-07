package kepco.prorject.ictyb.back.ictyb_back.work_my.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.ReceiveVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.ReceivePk;

/**
 * its_receive 레포지토리 (IT부서수신자 - 관련자 판단용)
 */
@Repository
public interface ReceiveRepository extends JpaRepository<ReceiveVo, ReceivePk> {

    /** 관련자 판단(수신자) 기준 조회 */
    List<ReceiveVo> findBySabun(String sabun);

    List<ReceiveVo> findByInstId(String instId);
}
