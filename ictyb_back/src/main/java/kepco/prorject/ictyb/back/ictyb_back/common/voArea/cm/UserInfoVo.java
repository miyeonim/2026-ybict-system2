package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import jakarta.persistence.*;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.UserInfoPk;
import lombok.Data;

@Data
@Entity
@Table(name = "ybict_user_info")
@IdClass(UserInfoPk.class)
public class UserInfoVo {

    @Id
    @Column(name = "PART_ID", nullable = false, columnDefinition = "VARCHAR(20) COMMENT '파트ID'")
    private String partId;

    @Column(name = "PART_NM", columnDefinition = "VARCHAR(100) COMMENT '파트명'")
    private String partNm;

    @Id
    @Column(name = "EMPNO", nullable = false, columnDefinition = "VARCHAR(20) COMMENT '사번'")
    private String empno;

    @Column(name = "USER_NM", columnDefinition = "VARCHAR(50) COMMENT '사용자명'")
    private String userNm;

    @Column(name = "BUJAN_YN", columnDefinition = "CHAR(1) COMMENT '부서장여부'")
    private String bujanYn;

    @Column(name = "PARTLEADER_YN", columnDefinition = "CHAR(1) COMMENT '파트장여부'")
    private String partleaderYn;

    @Column(name = "USE_YN", columnDefinition = "CHAR(1) DEFAULT 'Y' COMMENT '사용여부'")
    private String useYn;

    @Column(name = "PART_START_DT", columnDefinition = "DATE COMMENT '파트시작일'")
    private java.time.LocalDate partStartDt;

    @Column(name = "PART_END_DT", columnDefinition = "DATE COMMENT '파트종료일'")
    private java.time.LocalDate partEndDt;
}