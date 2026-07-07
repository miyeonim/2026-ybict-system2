package kepco.prorject.ictyb.back.ictyb_back.work_my.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.IctybWorkOpinionVo;

@Repository
public interface IctybWorkOpinionRepository extends JpaRepository<IctybWorkOpinionVo, String> {

    List<IctybWorkOpinionVo> findByInstrNo(String instrNo);

    List<IctybWorkOpinionVo> findByInstrNoIn(List<String> instrNos);
}
