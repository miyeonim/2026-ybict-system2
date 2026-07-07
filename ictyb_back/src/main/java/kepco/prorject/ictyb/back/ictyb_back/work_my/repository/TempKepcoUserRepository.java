package kepco.prorject.ictyb.back.ictyb_back.work_my.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.TempKepcoUserVo;

/**
 * temp_kepco_user 레포지토리 (한전 인사정보 - 임시, 결재 다음 단계 후보 조회 + 로그인 인증용)
 */
@Repository
public interface TempKepcoUserRepository extends JpaRepository<TempKepcoUserVo, String> {

    /** 사번 기준 로그인 인증용 조회 */
    Optional<TempKepcoUserVo> findBySabun(String sabun);

    /** 전체 한전 직원 목록 (결재 후보 조회용). 실제 테이블에 직책(파트장/직원) 구분 필드가 없어
     *  현재는 역할 구분 없이 전원을 후보로 취급한다. */
    List<TempKepcoUserVo> findAll();
}
