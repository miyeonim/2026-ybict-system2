package kepco.prorject.ictyb.back.ictyb_back.work_my.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.WorkResultAttachVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.WorkResultAttachPk;

@Repository
public interface WorkResultAttachRepository extends JpaRepository<WorkResultAttachVo, WorkResultAttachPk> {

    List<WorkResultAttachVo> findByInstId(String instId);
}
