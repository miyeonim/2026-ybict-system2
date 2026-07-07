package kepco.prorject.ictyb.back.ictyb_back.work_my.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.WorkHistoryVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.WorkHistoryPk;

/**
 * its_work_history 레포지토리 (결재이력 - MY)
 */
@Repository
public interface WorkHistoryRepository extends JpaRepository<WorkHistoryVo, WorkHistoryPk> {

    /** 처리 내역 탭: 결재자 기준 조회 (본인이 실제로 처리한 행위 기록) */
    List<WorkHistoryVo> findByRegSabun(String regSabun);

    /**
     * 작업지시서별 결재이력 조회 (상세화면 표시용).
     * 결재선 변경/회수로 현재 상태와 어긋날 수 있어 관련자 판단에는 사용하지 않는다.
     * SEQ는 VARCHAR라 문자 정렬 시 '10'이 '2'보다 앞에 오므로 숫자로 캐스팅해 정렬한다.
     */
    @Query(value = "SELECT * FROM its_work_history WHERE INST_ID = :instId ORDER BY CAST(SEQ AS UNSIGNED) ASC",
            nativeQuery = true)
    List<WorkHistoryVo> findByInstIdOrderBySeqAsc(@Param("instId") String instId);

    /** 목록 화면의 결재이력 컬럼용: 여러 건을 한 번에 조회(N+1 방지). SEQ는 숫자로 캐스팅해 정렬한다. */
    @Query(value = "SELECT * FROM its_work_history WHERE INST_ID IN (:instIds) "
            + "ORDER BY INST_ID ASC, CAST(SEQ AS UNSIGNED) ASC",
            nativeQuery = true)
    List<WorkHistoryVo> findByInstIdInOrderByInstIdAscSeqAsc(@Param("instIds") List<String> instIds);
}
