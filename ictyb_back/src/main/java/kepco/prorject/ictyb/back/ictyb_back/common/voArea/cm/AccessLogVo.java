package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "its_access_log")
@Getter
@Setter
@NoArgsConstructor
public class AccessLogVo {

    @Id
    @Column(name = "SABUN", length = 10, nullable = false, columnDefinition = "VARCHAR(10) COMMENT '사번'")
    private String sabun;

    @Column(name = "CONNECT_TIME", columnDefinition = "DATE COMMENT '접속시간'")
    private LocalDate connectTime;

    @Column(name = "LOGIN_DT", length = 8, columnDefinition = "VARCHAR(8) COMMENT '마지막접속일'")
    private String loginDt;

    @Column(name = "CNT", precision = 10, scale = 0, columnDefinition = "DECIMAL(10,0) COMMENT '로그인시도횟수'")
    private Integer cnt;
}
