package kepco.prorject.ictyb.back.ictyb_back.work_my.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.RealWorkReportVo;

/**
 * its_real_work_report 레포지토리 (요청서_실무부서)
 */
@Repository
public interface RealWorkReportRepository extends JpaRepository<RealWorkReportVo, String> {
}
