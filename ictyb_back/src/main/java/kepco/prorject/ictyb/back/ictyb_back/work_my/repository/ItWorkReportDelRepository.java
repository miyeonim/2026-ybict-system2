package kepco.prorject.ictyb.back.ictyb_back.work_my.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.ItWorkReportDelVo;

/**
 * its_it_work_report_del 레포지토리 (작업지시서 삭제 스냅샷)
 */
@Repository
public interface ItWorkReportDelRepository extends JpaRepository<ItWorkReportDelVo, String> {
}
