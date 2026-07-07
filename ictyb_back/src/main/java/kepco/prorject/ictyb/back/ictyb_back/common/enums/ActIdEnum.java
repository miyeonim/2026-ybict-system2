package kepco.prorject.ictyb.back.ictyb_back.common.enums;

import lombok.Getter;
import lombok.ToString;
import java.util.Arrays; 
import java.util.List;   

@Getter
@ToString
public enum ActIdEnum {

    ACTID_107("107", "지시서 접수"),
    ACTID_108("108", "지시서 반려"),

    ACTID_109("109", "작업완료 보고(검사 완료)"),
    ACTID_110("110", "작업완료 퀄드"),
    ACTID_111("111", "작업완료 승인"),

    ACTID_206("206", "지시서 서류보고"),

    ACTID_114("114", "초지관리 승인"),
    ACTID_215("215", "초지관리 승인(2차)"),
    ACTID_216("216", "초지관리 승인(3차)"),

    ACTID_901("901", "적용"),

    ACTID_202("202", "보고서 서류보고"),
    ACTID_800("800", "완료")
    ;


    private String code;
    private String name;

    ActIdEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }


    // code list
    public static List<String> getCodeList() {
        String[] codeList = new String[ActIdEnum.values().length];

        for (int i = 0; i < ActIdEnum.values().length; i++) {
            codeList[i] = ActIdEnum.values()[i].getCode();
        }

        return Arrays.asList(codeList);
    }


    // code로 enum 찾기
    public static ActIdEnum findByCode(String code) {

        return Arrays.stream(ActIdEnum.values())
                .filter(v -> v.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }
}