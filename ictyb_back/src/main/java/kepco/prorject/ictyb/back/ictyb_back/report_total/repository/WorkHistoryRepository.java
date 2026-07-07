package kepco.prorject.ictyb.back.ictyb_back.report_total.repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.WorkHistoryVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.WorkHistoryPk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 정보시스템정보 레포지토리
 */
@Repository("reportTotalWorkHistoryRepository")
public interface WorkHistoryRepository extends JpaRepository<WorkHistoryVo, WorkHistoryPk> {
    
    // 필요 시 복합키를 활용한 조회 메서드를 정의할 수 있사옵니다.
    // 예: 시스템 코드로 조회
    // SystemInfoVo findBySystemCd(String systemCd);
}
