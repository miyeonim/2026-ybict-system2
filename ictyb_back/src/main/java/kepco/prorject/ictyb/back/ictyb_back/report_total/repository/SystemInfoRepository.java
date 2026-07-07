package kepco.prorject.ictyb.back.ictyb_back.report_total.repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.SystemInfoVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.SystemInfoPk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 정보시스템정보 레포지토리
 */
@Repository("reportTotalSystemInfoRepository")
public interface SystemInfoRepository extends JpaRepository<SystemInfoVo, SystemInfoPk> {
    
    // 필요 시 복합키를 활용한 조회 메서드를 정의할 수 있사옵니다.
    // 예: 시스템 코드로 조회
    // SystemInfoVo findBySystemCd(String systemCd);
}