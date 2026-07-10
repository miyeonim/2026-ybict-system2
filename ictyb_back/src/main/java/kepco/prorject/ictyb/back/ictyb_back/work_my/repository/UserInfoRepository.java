package kepco.prorject.ictyb.back.ictyb_back.work_my.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.UserInfoVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.UserInfoPk;

/**
 * ybict_user_info 레포지토리 (결재 다음 단계 후보 조회용 - MY)
 */
@Repository
public interface UserInfoRepository extends JpaRepository<UserInfoVo, UserInfoPk> {

    /** 사번으로 소속 파트를 확인한다 (작업자 기준 파트 해석용) */
    List<UserInfoVo> findByEmpnoAndUseYn(String empno, String useYn);

    /**
     * 부서 부장(BUJAN_YN='Y') 후보 조회 (지시서 배부 단계).
     * ybict_part_info를 거쳐 부서(DEP_ID) 기준으로 조회한다.
     */
    @Query(value = """
        SELECT ui.* FROM ybict_user_info ui
        JOIN ybict_part_info pi ON pi.PART_ID = ui.PART_ID AND pi.USE_YN = 'Y'
        WHERE pi.DEP_ID = :depId AND ui.USE_YN = 'Y' AND ui.BUJAN_YN = 'Y'
        """, nativeQuery = true)
    List<UserInfoVo> findDeptHeadsByDepId(@Param("depId") String depId);

    /**
     * 여러 부서의 부장(BUJAN_YN='Y') 후보를 한 번에 조회한다 (지시서 접수(107) 단계 -
     * 대상 부서가 아직 정해지지 않은 시점에 영업/배전/기술 전체 부장 중에서 고르기 위함).
     */
    @Query(value = """
        SELECT ui.* FROM ybict_user_info ui
        JOIN ybict_part_info pi ON pi.PART_ID = ui.PART_ID AND pi.USE_YN = 'Y'
        WHERE pi.DEP_ID IN (:depIds) AND ui.USE_YN = 'Y' AND ui.BUJAN_YN = 'Y'
        """, nativeQuery = true)
    List<UserInfoVo> findDeptHeadsByDepIds(@Param("depIds") List<String> depIds);

    /** 사번으로 소속 부서(DEP_ID)를 조회한다 (107 승인 시 선택된 부장의 부서를 WORKER_DEP_CD로 확정하기 위함) */
    @Query(value = """
        SELECT pi.DEP_ID FROM ybict_user_info ui
        JOIN ybict_part_info pi ON pi.PART_ID = ui.PART_ID AND pi.USE_YN = 'Y'
        WHERE ui.EMPNO = :empno AND ui.USE_YN = 'Y'
        """, nativeQuery = true)
    List<String> findDepIdByEmpno(@Param("empno") String empno);

    /** 파트장(PARTLEADER_YN='Y') 후보 조회 (작업결과 승인 단계) */
    @Query(value = """
        SELECT ui.* FROM ybict_user_info ui
        WHERE ui.PART_ID = :partId AND ui.USE_YN = 'Y' AND ui.PARTLEADER_YN = 'Y'
        """, nativeQuery = true)
    List<UserInfoVo> findPartLeadersByPartId(@Param("partId") String partId);

    /** 파트 일반 직원(부장/파트장이 아닌) 후보 조회 (작업결과 보고 단계) */
    @Query(value = """
        SELECT ui.* FROM ybict_user_info ui
        WHERE ui.PART_ID = :partId AND ui.USE_YN = 'Y'
          AND (ui.BUJAN_YN IS NULL OR ui.BUJAN_YN <> 'Y')
          AND (ui.PARTLEADER_YN IS NULL OR ui.PARTLEADER_YN <> 'Y')
        """, nativeQuery = true)
    List<UserInfoVo> findRegularMembersByPartId(@Param("partId") String partId);

    /**
     * 파트가 아직 정해지지 않은 경우(작업자 미지정)의 대체 조회: 부서(DEP_ID) 전체의 일반 직원 후보.
     */
    @Query(value = """
        SELECT ui.* FROM ybict_user_info ui
        JOIN ybict_part_info pi ON pi.PART_ID = ui.PART_ID AND pi.USE_YN = 'Y'
        WHERE pi.DEP_ID = :depId AND ui.USE_YN = 'Y'
          AND (ui.BUJAN_YN IS NULL OR ui.BUJAN_YN <> 'Y')
          AND (ui.PARTLEADER_YN IS NULL OR ui.PARTLEADER_YN <> 'Y')
        """, nativeQuery = true)
    List<UserInfoVo> findRegularMembersByDepId(@Param("depId") String depId);

    /**
     * 파트가 아직 정해지지 않은 경우(작업자 미지정)의 대체 조회: 부서(DEP_ID) 전체의 파트장 후보.
     */
    @Query(value = """
        SELECT ui.* FROM ybict_user_info ui
        JOIN ybict_part_info pi ON pi.PART_ID = ui.PART_ID AND pi.USE_YN = 'Y'
        WHERE pi.DEP_ID = :depId AND ui.USE_YN = 'Y' AND ui.PARTLEADER_YN = 'Y'
        """, nativeQuery = true)
    List<UserInfoVo> findPartLeadersByDepId(@Param("depId") String depId);
}
