package kepco.prorject.ictyb.back.ictyb_back.work_my.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.IctybWorkOpinionCmntVo;

@Repository
public interface IctybWorkOpinionCmntRepository extends JpaRepository<IctybWorkOpinionCmntVo, String> {

    List<IctybWorkOpinionCmntVo> findByOpnId(String opnId);

    List<IctybWorkOpinionCmntVo> findByOpnIdIn(List<String> opnIds);
}
