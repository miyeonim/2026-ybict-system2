package kepco.prorject.ictyb.back.ictyb_back.report_daily.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.PartInfoVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.PartInfoPk;

import java.util.List;

@Repository("reportDailyPartInfoRepository")
public interface PartInfoRepository extends JpaRepository<PartInfoVo, PartInfoPk> {

    List<PartInfoVo> findByDepIdAndUseYnAndPartOrderGreaterThanOrderByPartOrder(String depId, String useYn, Integer partOrder);
}
