package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import lombok.*;

import jakarta.persistence.*;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.UserInfoSecretPk;

/**
 * its_user_info_secret 테이블 엔티티 (개인정보포함여부)
 * 원본 레거시 스키마는 INST_ID를 DECIMAL(10,0)로 정의했으나, 부모 테이블(its_real_work_report 등)
 * 어디에도 INST_ID가 숫자형으로 정의된 곳이 없어 레거시 설계 오류로 판단, VARCHAR(14)로 통일함
 */
@Entity
@Table(name = "its_user_info_secret")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(UserInfoSecretPk.class)
public class UserInfoSecretVo {

    @Id
    @Column(name = "INST_ID", length = 14) // 지시번호
    private String instId;

    @Id
    @Column(name = "DOC_TYPE", length = 1) // 문서타입
    private String docType;

    @Id
    @Column(name = "REQ_ID", length = 14) // 요청번호
    private String reqId;

    @Column(name = "IS_SECRET", length = 1) // 개인정보포함여부
    private String isSecret;

    @Column(name = "USER_SECRET_CONTENT", length = 4000) // 개인정보내용
    private String userSecretContent;

    @Column(name = "ATTACH_EXPIRE_DATE", length = 8) // 파기일
    private String attachExpireDate;

    @Column(name = "OPPB_CL_YN", length = 1) // 공개구분여부
    private String oppbClYn;
}
