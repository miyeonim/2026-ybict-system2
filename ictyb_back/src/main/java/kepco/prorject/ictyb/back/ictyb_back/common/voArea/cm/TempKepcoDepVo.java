package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * temp_kepco_dep 테이블 엔티티 (한전 조직도 - 임시)
 * 한전측이 전달한 실제 조직도 테이블의 컬럼 구조를 그대로 반영한다.
 */
@Entity
@Table(name = "temp_kepco_dep")
@Getter
@Setter
@NoArgsConstructor
public class TempKepcoDepVo {

    @Id
    @Column(name = "OF_CD", length = 16, nullable = false, columnDefinition = "VARCHAR(16) COMMENT '조직코드(전체, OF_CD1~4 연결값)'")
    private String ofCd;

    @Column(name = "OF_CODE", length = 4, columnDefinition = "VARCHAR(4) COMMENT '조직코드(말단, OF_CD4와 동일)'")
    private String ofCode;

    @Column(name = "OF_CD1", length = 4, columnDefinition = "VARCHAR(4) COMMENT '본부코드'")
    private String ofCd1;

    @Column(name = "OF_CD2", length = 4, columnDefinition = "VARCHAR(4) COMMENT '지사코드'")
    private String ofCd2;

    @Column(name = "OF_CD3", length = 4, columnDefinition = "VARCHAR(4) COMMENT '부코드'")
    private String ofCd3;

    @Column(name = "OF_CD4", length = 4, columnDefinition = "VARCHAR(4) COMMENT '팀코드'")
    private String ofCd4;

    @Column(name = "OF_HAN1", length = 30, columnDefinition = "VARCHAR(30) COMMENT '본부명'")
    private String ofHan1;

    @Column(name = "OF_HAN2", length = 30, columnDefinition = "VARCHAR(30) COMMENT '지사명'")
    private String ofHan2;

    @Column(name = "OF_HAN3", length = 30, columnDefinition = "VARCHAR(30) COMMENT '부명'")
    private String ofHan3;

    @Column(name = "OF_HAN4", length = 30, columnDefinition = "VARCHAR(30) COMMENT '팀명'")
    private String ofHan4;

    @Column(name = "SER_GU", length = 2, columnDefinition = "VARCHAR(2) COMMENT '서비스구분'")
    private String serGu;

    @Column(name = "SER_1", length = 4, columnDefinition = "VARCHAR(4) COMMENT '서비스코드1'")
    private String ser1;

    @Column(name = "SER_2", length = 4, columnDefinition = "VARCHAR(4) COMMENT '서비스코드2'")
    private String ser2;

    @Column(name = "SER_3", length = 4, columnDefinition = "VARCHAR(4) COMMENT '서비스코드3'")
    private String ser3;

    @Column(name = "SER_4", length = 4, columnDefinition = "VARCHAR(4) COMMENT '서비스코드4'")
    private String ser4;

    @Column(name = "F_BONSA", length = 1, columnDefinition = "VARCHAR(1) COMMENT '본사구분값'")
    private String fBonsa;

    @Column(name = "F_JIKHAL", length = 1, columnDefinition = "VARCHAR(1) COMMENT '직할여부'")
    private String fJikhal;

    @Column(name = "UPDATEDATE", length = 10, columnDefinition = "VARCHAR(10) COMMENT '갱신일자'")
    private String updateDate;

    /** 화면 표시용 부서명: 가장 하위 단계(팀 > 부 > 지사 > 본부) 순으로 채워진 첫 값을 반환한다. */
    @Transient
    public String getDisplayTitle() {
        if (ofHan4 != null && !ofHan4.isBlank()) return ofHan4;
        if (ofHan3 != null && !ofHan3.isBlank()) return ofHan3;
        if (ofHan2 != null && !ofHan2.isBlank()) return ofHan2;
        return ofHan1;
    }
}
