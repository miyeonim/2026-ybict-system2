package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "its_kdn_user")
@Getter
@Setter
@NoArgsConstructor
public class KdnUserVo {

    @Id
    @Column(name = "LOGIN_ID", length = 6, nullable = false, columnDefinition = "VARCHAR(6) COMMENT '로그인ID'")
    private String loginId;

    @Column(name = "DEP_ID", length = 10, nullable = false, columnDefinition = "VARCHAR(10) COMMENT '부서ID'")
    private String depId;

    @Column(name = "USER_ID", length = 10, columnDefinition = "VARCHAR(10) COMMENT '사용자ID'")
    private String userId;

    @Column(name = "USER_NM", length = 10, columnDefinition = "VARCHAR(10) COMMENT '이름'")
    private String userNm;

    @Column(name = "USER_PWD", length = 100, columnDefinition = "VARCHAR(100) COMMENT '비번'")
    private String userPwd;

    @Column(name = "EMAIL", length = 30, columnDefinition = "VARCHAR(30) COMMENT '이메일'")
    private String email;

    @Column(name = "CELL_PHONE", length = 15, columnDefinition = "VARCHAR(15) COMMENT '전화번호'")
    private String cellPhone;

    @Column(name = "PWD_FAIL_COUNT", precision = 10, scale = 0, columnDefinition = "DECIMAL(10,0) COMMENT '로그인실패카운트'")
    private Integer pwdFailCount;

    @Column(name = "DEL_YN", length = 1, columnDefinition = "VARCHAR(1) COMMENT '삭제여부'")
    private String delYn;

    @Column(name = "POSITION_CODE", length = 4, columnDefinition = "VARCHAR(4) COMMENT '직책코드'")
    private String positionCode;

    @Column(name = "POSITION_NM", length = 12, columnDefinition = "VARCHAR(12) COMMENT '직책'")
    private String positionNm;

    @Column(name = "CP_AUTH_YN", length = 1, columnDefinition = "VARCHAR(1) COMMENT '형상연계여부'")
    private String cpAuthYn;

    @Column(name = "CP_AUTH_DT", length = 14, columnDefinition = "VARCHAR(14) COMMENT '형상연계일'")
    private String cpAuthDt;
}