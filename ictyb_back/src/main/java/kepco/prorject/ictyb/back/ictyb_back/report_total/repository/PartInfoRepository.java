package kepco.prorject.ictyb.back.ictyb_back.report_total.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.PartInfoVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.PartInfoPk;


@Repository("reportTotalPartInfoRepository")
public interface PartInfoRepository extends JpaRepository<PartInfoVo, PartInfoPk> {

}
