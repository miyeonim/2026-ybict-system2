package kepco.prorject.ictyb.back.ictyb_back.jwt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.TempKepcoDepVo;

import java.util.Optional;

public interface TempKepcoDepRepository extends JpaRepository<TempKepcoDepVo, String> {
    Optional<TempKepcoDepVo> findByOfCd(String ofCd);
}
