package kepco.prorject.ictyb.back.ictyb_back.report_total.repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.SystemInfoVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.SystemInfoPk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 정보시스템정보 레포지토리
 */
@Repository("reportTotalSystemInfoRepository")
public interface SystemInfoRepository extends JpaRepository<SystemInfoVo, SystemInfoPk> {

    // 업무지시서 등록/수정 시 선택한 단위시스템(SYSTEM_CD)으로 업무분야·DRS영향여부를 조회할 때 사용
    Optional<SystemInfoVo> findBySystemCd(String systemCd);
}