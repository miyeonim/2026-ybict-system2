package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "its_kdn_dep", comment = "KDN부서") // 하이버네이트 설정에 따라 사용 가능, 안 되면 제거하소서
@Getter
@Setter
@NoArgsConstructor
public class KdnDepVo {

    @Id
    @Column(name = "DEP_ID", length = 10, nullable = false, columnDefinition = "VARCHAR(10) COMMENT '부서코드'")
    private String depId;

    @Column(name = "PAR_DEP_ID", length = 10, columnDefinition = "VARCHAR(10) COMMENT '처명'")
    private String parDepId;

    @Column(name = "DEP_TITLE", length = 20, columnDefinition = "VARCHAR(20) COMMENT '부서명'")
    private String depTitle;

    @Column(name = "KEPCO_MAP", length = 10, columnDefinition = "VARCHAR(10) COMMENT '본사여부'")
    private String kepcoMap;
}