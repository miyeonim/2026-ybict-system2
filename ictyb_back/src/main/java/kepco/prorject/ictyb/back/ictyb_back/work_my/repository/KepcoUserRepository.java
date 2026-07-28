package kepco.prorject.ictyb.back.ictyb_back.work_my.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.KepcoUserVo;

/**
 * its_kepco_user 레포지토리 (한전 인사정보 - 결재 다음 단계 후보 조회 + 로그인 인증용)
 */
@Repository
public interface KepcoUserRepository extends JpaRepository<KepcoUserVo, String> {

    /** 사번(10자리, its_kepco_user 원본 그대로) 기준 로그인 인증용 조회 */
    Optional<KepcoUserVo> findBySabun(String sabun);

    /**
     * 8자리로 잘린 사번(KepcoSabunUtil.toLegacySabun 결과, 예: REG_USER_SABUN에 저장된 값)으로
     * 원본 한전 인사정보를 역조회할 때 사용한다. 10자리 값이 그대로 들어와도(자르기 전 값과의
     * 하위호환) SABUN 전체가 자기 자신으로 끝나므로 동일하게 매칭된다.
     */
    Optional<KepcoUserVo> findFirstBySabunEndingWith(String sabunSuffix);

    /** 전체 한전 직원 목록 (결재 후보 조회용). 실제 테이블에 직책(파트장/직원) 구분 필드가 없어
     *  현재는 역할 구분 없이 전원을 후보로 취급한다. */
    List<KepcoUserVo> findAll();
}
