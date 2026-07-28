package kepco.prorject.ictyb.back.ictyb_back.work_my.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.RealWorkReportAttachVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.RealWorkReportAttachPk;

/**
 * its_real_work_report_attach 레포지토리 (요청서_실무부서_첨부)
 */
@Repository
public interface RealWorkReportAttachRepository extends JpaRepository<RealWorkReportAttachVo, RealWorkReportAttachPk> {

    List<RealWorkReportAttachVo> findByInstId(String instId);
}
