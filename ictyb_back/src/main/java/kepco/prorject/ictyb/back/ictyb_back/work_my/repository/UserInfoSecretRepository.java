package kepco.prorject.ictyb.back.ictyb_back.work_my.repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.UserInfoSecretVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.UserInfoSecretPk;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInfoSecretRepository extends JpaRepository<UserInfoSecretVo, UserInfoSecretPk> {
}
