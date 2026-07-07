package kepco.prorject.ictyb.back.ictyb_back.work_my.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.NSignVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.NSignPk;

/**
 * its_n_sign 레포지토리 (현재결재자정보 - MY)
 */
@Repository
public interface NSignRepository extends JpaRepository<NSignVo, NSignPk> {

    /** 결재대기 탭: 현재 결재자 기준 조회 */
    List<NSignVo> findBySabun(String sabun);

    /** 관련자 판단용 조회 */
    List<NSignVo> findByInstId(String instId);
}
