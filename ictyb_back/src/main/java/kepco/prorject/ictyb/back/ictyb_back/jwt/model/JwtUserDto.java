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
}
