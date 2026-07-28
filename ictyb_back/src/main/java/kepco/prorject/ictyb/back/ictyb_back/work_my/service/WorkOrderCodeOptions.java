package kepco.prorject.ictyb.back.ictyb_back.work_my.service;

import java.util.List;

import kepco.prorject.ictyb.back.ictyb_back.work_my.model.WorkMyDto;

/**
 * 업무지시서 등록 폼의 코드성 드롭다운 옵션.
 * SERVICE_TYPE(서비스유형)/WORK_TYPE(작업유형)/WORK_GUBUN(작업구분)은 its_code 테이블(GUBUN별로 구분)에서
 * 조회한다 - 실제 옵션 목록 조회/라벨 변환은 CodeRepository를 쓰는 WorkMyServiceImpl에 있다.
 * WORK_LEVEL은 DB에 한글 그대로('상'/'중'/'하') 저장되는 필드라 코드표 없이 실값 그대로 쓴다.
 */
public final class WorkOrderCodeOptions {

    private WorkOrderCodeOptions() {
    }

    /** its_code.GUBUN 값 - 서비스유형 */
    public static final String SERVICE_TYPE_GUBUN = "01";
    /** its_code.GUBUN 값 - 작업구분 */
    public static final String WORK_GUBUN_GUBUN = "02";
    /** its_code.GUBUN 값 - 작업유형 */
    public static final String WORK_TYPE_GUBUN = "03";
    /**
     * 자료추출 건 판별용 CODE_NAME. 서비스유형(GUBUN=01) 또는 작업유형(GUBUN=03) 중
     * 어느 쪽이든 이 이름의 코드가 선택되면 자료추출로 취급한다 (108/111 결재 생략).
     */
    public static final String DATA_EXTRACTION_CODE_NAME = "자료추출";

    public static final List<WorkMyDto.CodeOption> WORK_LEVEL = List.of(
            WorkMyDto.CodeOption.of("상", "상"),
            WorkMyDto.CodeOption.of("중", "중"),
            WorkMyDto.CodeOption.of("하", "하")
    );

    /** DRS(재해복구시스템) 영향 여부. DB 컬럼(DRS_IMPT_YN)이 Y/N이라 코드값도 실값 그대로다. */
    public static final List<WorkMyDto.CodeOption> DRS_IMPT_YN = List.of(
            WorkMyDto.CodeOption.of("Y", "영향 있음"),
            WorkMyDto.CodeOption.of("N", "영향 없음")
    );

    /** 코드값을 사람이 읽을 수 있는 라벨로 변환한다. 매칭되는 코드가 없으면 원본 코드를 그대로 반환한다. */
    public static String labelOf(List<WorkMyDto.CodeOption> options, String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return options.stream()
                .filter(o -> o.getCode().equals(code))
                .map(WorkMyDto.CodeOption::getLabel)
                .findFirst()
                .orElse(code);
    }
}
