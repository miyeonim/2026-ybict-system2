package kepco.prorject.ictyb.back.ictyb_back.report_daily.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.UserInfoVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.UserInfoPk;

/**
 * ictyb_user_info 레포지토리 (영업 점검일지 파트별 결재 인가 판정용).
 * PART_START_DT/PART_END_DT로 "현재 유효한" 소속 한 행만 골라낸다 - 같은 사번이
 * 파트를 옮긴 이력 때문에 여러 행을 가질 수 있어서다.
 */
@Repository("reportDailyUserInfoRepository")
public interface UserInfoRepository extends JpaRepository<UserInfoVo, UserInfoPk> {

    /** 사번이 지금 이 파트 소속인지 확인 (파트장 여부는 PARTLEADER_YN으로 별도 확인) */
    @Query(value = """
        SELECT ui.* FROM ictyb_user_info ui
        WHERE ui.PART_ID = :partId AND ui.EMPNO = :empno AND ui.USE_YN = 'Y'
          AND ui.PART_START_DT <= CURDATE()
          AND (ui.PART_END_DT IS NULL OR ui.PART_END_DT >= CURDATE())
        LIMIT 1
        """, nativeQuery = true)
    Optional<UserInfoVo> findCurrentMembership(@Param("partId") String partId, @Param("empno") String empno);

    /** 사번의 현재 유효한 소속 파트/역할 조회 (JWT 로그인 정보 채우기용) */
    @Query(value = """
        SELECT ui.* FROM ictyb_user_info ui
        WHERE ui.EMPNO = :empno AND ui.USE_YN = 'Y'
          AND ui.PART_START_DT <= CURDATE()
          AND (ui.PART_END_DT IS NULL OR ui.PART_END_DT >= CURDATE())
        LIMIT 1
        """, nativeQuery = true)
    Optional<UserInfoVo> findCurrentByEmpno(@Param("empno") String empno);

    /** 파트의 현재 유효한 파트장 조회 (승인 권한 판정용) */
    @Query(value = """
        SELECT ui.* FROM ictyb_user_info ui
        WHERE ui.PART_ID = :partId AND ui.USE_YN = 'Y' AND ui.PARTLEADER_YN = 'Y'
          AND ui.PART_START_DT <= CURDATE()
          AND (ui.PART_END_DT IS NULL OR ui.PART_END_DT >= CURDATE())
        LIMIT 1
        """, nativeQuery = true)
    Optional<UserInfoVo> findCurrentPartLeader(@Param("partId") String partId);

    /** 부서(DEP_ID)의 현재 유효한 부장 조회 (최종 결재 권한 판정용) */
    @Query(value = """
        SELECT ui.* FROM ictyb_user_info ui
        JOIN ictyb_part_info pi ON pi.PART_ID = ui.PART_ID AND pi.USE_YN = 'Y'
        WHERE pi.DEP_ID = :depId AND ui.USE_YN = 'Y' AND ui.BUJAN_YN = 'Y'
          AND ui.PART_START_DT <= CURDATE()
          AND (ui.PART_END_DT IS NULL OR ui.PART_END_DT >= CURDATE())
        LIMIT 1
        """, nativeQuery = true)
    Optional<UserInfoVo> findCurrentDeptHead(@Param("depId") String depId);

    List<UserInfoVo> findByEmpnoAndUseYn(String empno, String useYn);
}
