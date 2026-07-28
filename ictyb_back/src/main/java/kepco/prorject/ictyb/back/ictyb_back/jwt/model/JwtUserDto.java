package kepco.prorject.ictyb.back.ictyb_back.jwt.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class JwtUserDto {
    private String depId;     // 부서ID
    private String parDepId;  // 처명 
    private String depTitle;  // 부서명
    private String kepcoMap;  // 본사여부
    private String userEmpno; // 사번 (loginId)
    private String empNm;     // 이름
    private String kepcoYn;   // 한전사람 여부
    private boolean deptHead; // 처장 여부 (DeptHeadUtil 기준)
    private String partId;    // 소속 파트ID (ictyb_user_info 기준, KDN만 - KEPCO는 null)
    private boolean partLeader; // 파트장 여부 (ictyb_user_info.PARTLEADER_YN='Y', KDN만)
    private boolean bujan;    // 부서장(부장) 여부 (ictyb_user_info.BUJAN_YN='Y', KDN만 - 처장(deptHead)과는 다른 개념)
}
