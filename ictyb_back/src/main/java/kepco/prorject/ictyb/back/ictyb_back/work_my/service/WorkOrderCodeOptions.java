package kepco.prorject.ictyb.back.ictyb_back.work_my.service;

import java.util.List;

import kepco.prorject.ictyb.back.ictyb_back.work_my.model.WorkMyDto;

/**
 * 업무지시서 등록 폼의 코드성 드롭다운 옵션 (임시).
 * SERVICE_TYPE/WORK_TYPE/WORK_GUBUN은 실제 ITMS 코드표를 아직 확보하지 못해,
 * 실사용 화면에서 확인된 라벨만을 근거로 임시 코드('01' 등)를 붙여둔 것이다.
 * 실제 코드표가 확인되면 이 클래스의 값만 교체하면 된다.
 * WORK_LEVEL은 DB에 한글 그대로('상'/'중'/'하') 저장되는 필드라 임시코드가 아닌 실값이다.
 */
public final class WorkOrderCodeOptions {

    private WorkOrderCodeOptions() {
    }

    public static final List<WorkMyDto.CodeOption> SERVICE_TYPE = List.of(
            WorkMyDto.CodeOption.of("01", "업무지원")
    );

    public static final List<WorkMyDto.CodeOption> WORK_TYPE = List.of(
            WorkMyDto.CodeOption.of("01", "업무지원"),
            WorkMyDto.CodeOption.of("02", "자료추출")
    );

    public static final List<WorkMyDto.CodeOption> WORK_GUBUN = List.of(
            WorkMyDto.CodeOption.of("01", "SW일상운영")
    );

    public static final List<WorkMyDto.CodeOption> WORK_LEVEL = List.of(
            WorkMyDto.CodeOption.of("상", "상"),
            WorkMyDto.CodeOption.of("중", "중"),
            WorkMyDto.CodeOption.of("하", "하")
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
