package kepco.prorject.ictyb.back.ictyb_back.work_my.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.ItWorkReportAttachVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.ItWorkReportAttachPk;

@Repository
public interface ItWorkReportAttachRepository extends JpaRepository<ItWorkReportAttachVo, ItWorkReportAttachPk> {

    List<ItWorkReportAttachVo> findByInstId(String instId);
}
