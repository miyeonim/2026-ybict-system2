package kepco.prorject.ictyb.back.ictyb_back.work_my.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.IctybWorkOpinionAttachVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.IctybWorkOpinionAttachPk;

@Repository
public interface IctybWorkOpinionAttachRepository extends JpaRepository<IctybWorkOpinionAttachVo, IctybWorkOpinionAttachPk> {

    List<IctybWorkOpinionAttachVo> findByCmntId(String cmntId);

    List<IctybWorkOpinionAttachVo> findByCmntIdIn(List<String> cmntIds);
}
