package kepco.prorject.ictyb.back.ictyb_back.work_my.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.ItReceiverVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.ItReceiverPk;

/**
 * its_it_receiver 레포지토리 (IT부서 접수자 - 관련자 판단용)
 */
@Repository
public interface ItReceiverRepository extends JpaRepository<ItReceiverVo, ItReceiverPk> {

    /** 관련자 판단(접수자) 기준 조회 */
    List<ItReceiverVo> findBySabun(String sabun);

    List<ItReceiverVo> findByInstId(String instId);
}
