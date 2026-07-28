package kepco.prorject.ictyb.back.ictyb_back.jwt.repository;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.AccessLogVo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessLogRepository extends JpaRepository<AccessLogVo, String> {
}
