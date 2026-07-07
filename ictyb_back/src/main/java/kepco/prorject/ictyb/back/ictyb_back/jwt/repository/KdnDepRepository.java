package kepco.prorject.ictyb.back.ictyb_back.jwt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.KdnDepVo;

import java.util.Optional;

public interface KdnDepRepository extends JpaRepository<KdnDepVo, String> {
    Optional<KdnDepVo> findByDepId(String depId);
}