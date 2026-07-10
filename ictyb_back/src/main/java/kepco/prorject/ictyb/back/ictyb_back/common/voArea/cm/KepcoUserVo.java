package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import jakarta.persistence.*;
import lombok.Data;

/**
 * ictyb_kepco_user 테이블 엔티티 (한전 인사정보)
 * 한전측이 전달한 실제 인사정보 테이블의 컬럼 구조를 그대로 반영한다.
 * 한전 사용자는 SSO로 인증되어 별도 비밀번호 컬럼이 없다(추후 한전 망 이관 시 SSO 연동 예정).
 */
@Data
@Entity
@Table(name = "ictyb_kepco_user")
public class KepcoUserVo {

    @Id
    @Column(name = "SABUN", length = 10, nullable = false, columnDefinition = "VARCHAR(10) COMMENT '사번'")
    private String sabun;

    @Column(name = "NAME", length = 20, columnDefinition = "VARCHAR(20) COMMENT '이름'")
    private String name;

    @Column(name = "JIKGUB_HAN", length = 20, columnDefinition = "VARCHAR(20) COMMENT '직급명'")
    private String jikgubHan;

    @Column(name = "JIKKUN_HAN", length = 20, columnDefinition = "VARCHAR(20) COMMENT '직군명'")
    private String jikkunHan;

    @Column(name = "SOSOK_HAN", length = 30, columnDefinition = "VARCHAR(30) COMMENT '소속명'")
    private String sosokHan;

    @Column(name = "SOSOK_CD", length = 16, columnDefinition = "VARCHAR(16) COMMENT '소속코드(전체)'")
    private String sosokCd;

    @Column(name = "SOSOK_CD1", length = 4, columnDefinition = "VARCHAR(4) COMMENT '소속코드-본부'")
    private String sosokCd1;

    @Column(name = "SOSOK_CD2", length = 4, columnDefinition = "VARCHAR(4) COMMENT '소속코드-지사'")
    private String sosokCd2;

    @Column(name = "SOSOK_CD3", length = 4, columnDefinition = "VARCHAR(4) COMMENT '소속코드-부'")
    private String sosokCd3;

    @Column(name = "SOSOK_CD4", length = 4, columnDefinition = "VARCHAR(4) COMMENT '소속코드-팀'")
    private String sosokCd4;

    @Column(name = "TEL", length = 20, columnDefinition = "VARCHAR(20) COMMENT '전화번호'")
    private String tel;

    @Column(name = "JIKGUB", length = 4, columnDefinition = "VARCHAR(4) COMMENT '직급코드'")
    private String jikgub;

    @Column(name = "JIKYEE", length = 30, columnDefinition = "VARCHAR(30) COMMENT '직예명'")
    private String jikyee;

    @Column(name = "HP", length = 20, columnDefinition = "VARCHAR(20) COMMENT '휴대폰번호'")
    private String hp;

    @Column(name = "E_MAIL", length = 50, columnDefinition = "VARCHAR(50) COMMENT '이메일'")
    private String email;

    @Column(name = "UPDATEDATE", length = 10, columnDefinition = "VARCHAR(10) COMMENT '갱신일자'")
    private String updateDate;
}
