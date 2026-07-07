package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.BaseVo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true) 
@NoArgsConstructor // 엔티티 필수: 기본 생성자
@AllArgsConstructor // 데이터 생성 편리함을 위한 전체 생성자
@Entity
@Table(name = "ictyb_mw_lgin_thkn_info")
public class MwLginTknInfoVo extends BaseVo{
    @Id
    @Column(name = "RFSTKN_KEY")
    private String rfstknKey;
    
    @Column(name = "FRST_REGR_EMPNO")
    private String frstRegrEmpno;
    
    @Column(name = "LST_CHGR_EMPNO")
    private String lstChgrEmpno;
    
    @Column(name = "USER_EMPNO")
    private String userEmpno;
    
    @Column(name = "EXP_YMD")
    private LocalDate expYmd;
    
    // FRST_REG_DT, LST_CHG_DT는 DB의 DEFAULT 설정으로 자동 처리되므로 생략 가능합니다.
}