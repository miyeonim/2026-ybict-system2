package kepco.prorject.ictyb.back.ictyb_back.report_daily.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.SalesDailyReportPartVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.SalesDailyReportPartPk;

import java.util.List;

@Repository
public interface SalesDailyReportPartRepository extends JpaRepository<SalesDailyReportPartVo, SalesDailyReportPartPk> {

    List<SalesDailyReportPartVo> findByReportIdOrderByPartId(Long reportId);

    List<SalesDailyReportPartVo> findByReportIdIn(List<Long> reportIds);
}
