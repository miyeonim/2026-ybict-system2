package kepco.prorject.ictyb.back.ictyb_back.common.voArea;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@MappedSuperclass // INFO. 공통된 정보를 Entity에서 필요로 할때 상속으로 필드를 사용 가능하게 함
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BaseVo {

    @CreatedDate
    @Column(name = "FRST_REG_DT", updatable = false) // INFO. 해당 필드는 UPDATE 문에 사용되지 않음
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime frstRegDt;

    @LastModifiedDate
    @Column(name = "LST_CHG_DT")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime lstChgDt;
}
