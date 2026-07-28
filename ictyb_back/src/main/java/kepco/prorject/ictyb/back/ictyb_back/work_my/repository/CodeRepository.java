package kepco.prorject.ictyb.back.ictyb_back.work_my.repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.CodeVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.CodePk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 코드 정보(its_code) 레포지토리. GUBUN 값으로 서비스유형(01)/작업구분(02)/작업유형(03) 등
 * 업무지시서 등록 폼의 코드성 드롭다운 값을 조회한다.
 */
@Repository
public interface CodeRepository extends JpaRepository<CodeVo, CodePk> {

    List<CodeVo> findByIdGubun(String gubun);

    Optional<CodeVo> findByIdGubunAndIdCode(String gubun, String code);
}
