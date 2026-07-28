package kepco.prorject.ictyb.back.ictyb_back.work_my.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.IctybWorkOpinionReadLogVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.IctybWorkOpinionReadLogPk;

@Repository
public interface IctybWorkOpinionReadLogRepository
        extends JpaRepository<IctybWorkOpinionReadLogVo, IctybWorkOpinionReadLogPk> {

    List<IctybWorkOpinionReadLogVo> findByOpnIdInAndSabun(List<String> opnIds, String sabun);
}
