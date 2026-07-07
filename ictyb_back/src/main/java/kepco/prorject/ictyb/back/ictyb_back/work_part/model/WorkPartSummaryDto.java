package kepco.prorject.ictyb.back.ictyb_back.work_part.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class WorkPartSummaryDto {

    private long done;
    private long notDone;

    // "전체" 조회일 때만 값 존재, 그 외에는 null
    private Long receivedTotal;

    private List<BarRow> barRows;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class BarRow {
        private String name;

        @JsonProperty("완료")
        private long done;

        @JsonProperty("미완료")
        private long notDone;
    }
}