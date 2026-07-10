package kepco.prorject.ictyb.back.ictyb_back.jwt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.KepcoDepVo;

import java.util.Optional;

public interface KepcoDepRepository extends JpaRepository<KepcoDepVo, String> {
    Optional<KepcoDepVo> findByOfCd(String ofCd);
}
