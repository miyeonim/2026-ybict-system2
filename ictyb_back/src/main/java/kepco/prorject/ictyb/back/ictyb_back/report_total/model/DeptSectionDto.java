package kepco.prorject.ictyb.back.ictyb_back.report_total.model;
import java.util.List;

import lombok.Data;

/**
 * 프론트엔드의 DeptSection 인터페이스와 매핑되는 DTO
 */
public record DeptSectionDto(
    String title,           // 부서명 (예: 영업시스템운영부)
    String icon,            // 아이콘 (예: 🔧)
    List<DeptPartDto> data  // 해당 부서에 속한 파트 리스트
) {}