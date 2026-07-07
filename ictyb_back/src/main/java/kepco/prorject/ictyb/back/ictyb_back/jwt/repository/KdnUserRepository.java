package kepco.prorject.ictyb.back.ictyb_back.jwt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.KdnUserVo;

import java.util.Optional;

public interface KdnUserRepository extends JpaRepository<KdnUserVo, String> {
    Optional<KdnUserVo> findByLoginId(String loginId);
}