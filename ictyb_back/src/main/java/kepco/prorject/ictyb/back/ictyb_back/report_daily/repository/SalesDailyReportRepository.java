package kepco.prorject.ictyb.back.ictyb_back.report_daily.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.SalesDailyReportVo;

import java.util.List;

@Repository
public interface SalesDailyReportRepository extends JpaRepository<SalesDailyReportVo, Long> {

    List<SalesDailyReportVo> findAllByOrderByReportDateDescReportIdDesc();
}
