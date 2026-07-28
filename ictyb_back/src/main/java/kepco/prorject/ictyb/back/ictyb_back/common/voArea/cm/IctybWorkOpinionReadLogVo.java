package kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm;

import lombok.*;

import jakarta.persistence.*;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.IctybWorkOpinionReadLogPk;

import java.time.LocalDateTime;

/**
 * ictyb_work_opinion_read_log 테이블 엔티티 (사용자별 협의 읽음 기록)
 */
@Entity
@Table(name = "ictyb_work_opinion_read_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(IctybWorkOpinionReadLogPk.class)
public class IctybWorkOpinionReadLogVo {

    @Id
    @Column(name = "OPN_ID", length = 20) // 협의ID
    private String opnId;

    @Id
    @Column(name = "SABUN", length = 10) // 읽은 사용자 사번
    private String sabun;

    @Column(name = "READ_DT") // 읽은 일시
    private LocalDateTime readDt;
}
