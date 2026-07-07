package kepco.prorject.ictyb.back.ictyb_back.report_daily.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.SalesDailyReportPersonVo;

import java.util.List;

@Repository
public interface SalesDailyReportPersonRepository extends JpaRepository<SalesDailyReportPersonVo, Long> {

    List<SalesDailyReportPersonVo> findByReportIdAndPartIdOrderByPersonSeq(Long reportId, String partId);

    List<SalesDailyReportPersonVo> findByReportIdIn(List<Long> reportIds);
}
