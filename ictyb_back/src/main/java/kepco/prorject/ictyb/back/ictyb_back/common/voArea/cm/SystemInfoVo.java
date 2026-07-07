package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import jakarta.persistence.*;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.SystemInfoPk;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import java.math.BigDecimal;

@Entity
@Table(name = "its_system_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
public class SystemInfoVo {

    @EmbeddedId
    private SystemInfoPk id; // 복합키 (대분류, 중분류, 순번)

    @Column(name = "SYSTEM_CD", length = 6)
    private String systemCd; // 시스템코드

    @Column(name = "CMS_NAME", length = 100)
    private String cmsName; // 단위시스템명

    @Column(name = "SYSTEM_CONTENTS", length = 2000)
    private String systemContents; // 개요

    @Column(name = "MAINTAIN_YN", length = 1)
    private String maintainYn; // 유지구분

    @Column(name = "DAILY_YN", length = 1)
    private String dailyYn; // 일상구분

    @Column(name = "BRANCH_DAILY_YN", length = 1)
    private String branchDailyYn; // 사업소일상구분

    @Column(name = "FP", precision = 6, scale = 2)
    private BigDecimal fp; // 기능점수

    @Column(name = "START_DATE", length = 14)
    private String startDate; // 위탁개시일

    @Column(name = "CLOSE_DATE", length = 14)
    private String closeDate; // 위탁해지일

    @Column(name = "INPUT_DATE", length = 14)
    private String inputDate; // 입력일

    @Column(name = "UPDATE_DATE", length = 14)
    private String updateDate; // 수정일

    @Column(name = "DOMAIN_NAME", length = 20)
    private String domainName; // 도메인명

    @Column(name = "SUBDOMAIN_NAME", length = 35)
    private String subdomainName; // 서브도메인명

    @Column(name = "SUB_NAME", length = 150)
    private String subName; // 하위도메인명

    @Column(name = "SAP_YN", length = 1)
    private String sapYn; // ERP여부

    @Column(name = "ITMS_BIZ_CD", length = 4)
    private String itmsBizCd; // 형상관리연계코드

    @Column(name = "NEW_ITMS_YN", length = 1)
    private String newItmsYn; // 형상관리관리여부

    @Column(name = "DEPLOY_START_DT", length = 14)
    private String deployStartDt; // 배포시작일

    @Column(name = "EP_SYS_YN", length = 1)
    private String epSysYn; // EP시스템여부

    @Column(name = "EP_GRP_NM", length = 50)
    private String epGrpNm; // EP그룹명

    @Column(name = "DRS_IMPT_YN", length = 1)
    private String drsImptYn; // 재해복구시스템영향여부
}