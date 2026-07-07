package kepco.prorject.ictyb.back.ictyb_back.report_total.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.UserInfoVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.UserInfoPk;


@Repository("reportTotalUserInfoRepository")
public interface UserInfoRepository extends JpaRepository<UserInfoVo, UserInfoPk> {

}
