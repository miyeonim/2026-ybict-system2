package kepco.prorject.ictyb.back.ictyb_back.report_daily.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.SalesDailyReportAttachVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.SalesDailyReportAttachPk;

import java.util.List;

@Repository
public interface SalesDailyReportAttachRepository extends JpaRepository<SalesDailyReportAttachVo, SalesDailyReportAttachPk> {

    List<SalesDailyReportAttachVo> findByReportIdAndPartIdOrderBySeq(Long reportId, String partId);

    List<SalesDailyReportAttachVo> findByReportIdIn(List<Long> reportIds);

    int countByReportIdAndPartId(Long reportId, String partId);
}
