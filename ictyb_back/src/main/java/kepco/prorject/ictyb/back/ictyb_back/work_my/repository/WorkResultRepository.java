package kepco.prorject.ictyb.back.ictyb_back.work_my.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.WorkResultVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.WorkResultPk;

@Repository
public interface WorkResultRepository extends JpaRepository<WorkResultVo, WorkResultPk> {

    List<WorkResultVo> findByInstId(String instId);
}
