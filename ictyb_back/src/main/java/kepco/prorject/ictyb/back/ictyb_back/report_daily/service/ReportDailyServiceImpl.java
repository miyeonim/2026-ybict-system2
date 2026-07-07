package kepco.prorject.ictyb.back.ictyb_back.report_daily.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.PartInfoVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.SalesDailyReportAttachVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.SalesDailyReportPartVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.SalesDailyReportPersonVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.SalesDailyReportVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.PartInfoPk;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.SalesDailyReportAttachPk;
import kepco.prorject.ictyb.back.ictyb_back.report_daily.model.ReportDailyDto;
import kepco.prorject.ictyb.back.ictyb_back.report_daily.repository.ItWorkReportRepository;
import kepco.prorject.ictyb.back.ictyb_back.report_daily.repository.PartInfoRepository;
import kepco.prorject.ictyb.back.ictyb_back.report_daily.repository.SalesDailyReportAttachRepository;
import kepco.prorject.ictyb.back.ictyb_back.report_daily.repository.SalesDailyReportPartRepository;
import kepco.prorject.ictyb.back.ictyb_back.report_daily.repository.SalesDailyReportPersonRepository;
import kepco.prorject.ictyb.back.ictyb_back.report_daily.repository.SalesDailyReportRepository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportDailyServiceImpl implements ReportDailyService {

    /** 영업시스템운영부 (ybict_part_info.DEP_ID) - 영업 점검일지는 이 부서 산하 파트로 고정 */
    private static final String SALES_DEP_ID = "9611175";

    private final SalesDailyReportRepository reportRepository;
    private final SalesDailyReportPartRepository partRepository;
    private final SalesDailyReportPersonRepository personRepository;
    private final SalesDailyReportAttachRepository attachRepository;
    private final PartInfoRepository partInfoRepository;
    private final ItWorkReportRepository itWorkReportRepository;

    @Value("${file.report-daily-upload-dir:./uploads/report_daily}")
    private String uploadDir;

    // ── 파트 목록 ─────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ReportDailyDto.PartOption> getPartOptions() {
        return partInfoRepository
                .findByDepIdAndUseYnAndPartOrderGreaterThanOrderByPartOrder(SALES_DEP_ID, "Y", 0)
                .stream()
                .map(p -> ReportDailyDto.PartOption.builder()
                        .partId(p.getPartId())
                        .partNm(p.getPartNm())
                        .partOrder(p.getPartOrder())
                        .build())
                .collect(Collectors.toList());
    }

    // ── 인원별 작업지시 현황 (DB 집계) ───────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ReportDailyDto.PersonStat> getPersonStats(String partId) {
        return itWorkReportRepository.getPersonStatsByPart(partId).stream()
                .map(row -> ReportDailyDto.PersonStat.builder()
                        .personSabun((String) row[0])
                        .personNm((String) row[1])
                        .inProgressCnt(((Number) row[2]).intValue())
                        .delayedCnt(((Number) row[3]).intValue())
                        .build())
                .collect(Collectors.toList());
    }

    // ── 목록 조회 ─────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ReportDailyDto.ListItem> getReportList() {
        List<SalesDailyReportVo> reports = reportRepository.findAllByOrderByReportDateDescReportIdDesc();

        return reports.stream().map(report -> {
            Long reportId = report.getReportId();
            List<SalesDailyReportPartVo> parts = partRepository.findByReportIdOrderByPartId(reportId);

            int totalInProgress = 0;
            int totalDelayed = 0;
            int totalDistributed = 0;
            int attachmentCount = 0;

            for (SalesDailyReportPartVo part : parts) {
                List<SalesDailyReportPersonVo> people =
                        personRepository.findByReportIdAndPartIdOrderByPersonSeq(reportId, part.getPartId());
                for (SalesDailyReportPersonVo person : people) {
                    totalInProgress += person.getInProgressCnt() != null ? person.getInProgressCnt() : 0;
                    totalDelayed += person.getDelayedCnt() != null ? person.getDelayedCnt() : 0;
                    totalDistributed += person.getDistributedCnt() != null ? person.getDistributedCnt() : 0;
                }
                attachmentCount += attachRepository.countByReportIdAndPartId(reportId, part.getPartId());
            }

            return ReportDailyDto.ListItem.builder()
                    .reportId(reportId)
                    .reportDate(report.getReportDate())
                    .authorName(report.getAuthorName())
                    .partNames(parts.stream()
                            .map(p -> p.getPartNm() != null ? p.getPartNm() : p.getPartId())
                            .collect(Collectors.toList()))
                    .totalInProgress(totalInProgress)
                    .totalDelayed(totalDelayed)
                    .totalDistributed(totalDistributed)
                    .attachmentCount(attachmentCount)
                    .build();
        }).collect(Collectors.toList());
    }

    // ── 상세 조회 ─────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ReportDailyDto.Detail getReportDetail(Long reportId) {
        SalesDailyReportVo report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 점검일지입니다."));

        List<SalesDailyReportPartVo> parts = partRepository.findByReportIdOrderByPartId(reportId);

        List<ReportDailyDto.PartDetail> partDetails = parts.stream().map(part -> {
            List<ReportDailyDto.Person> people =
                    personRepository.findByReportIdAndPartIdOrderByPersonSeq(reportId, part.getPartId())
                            .stream()
                            .map(p -> ReportDailyDto.Person.builder()
                                    .personNm(p.getPersonNm())
                                    .inProgressCnt(p.getInProgressCnt() != null ? p.getInProgressCnt() : 0)
                                    .delayedCnt(p.getDelayedCnt() != null ? p.getDelayedCnt() : 0)
                                    .distributedCnt(p.getDistributedCnt() != null ? p.getDistributedCnt() : 0)
                                    .build())
                            .collect(Collectors.toList());

            List<ReportDailyDto.Attach> attachments =
                    attachRepository.findByReportIdAndPartIdOrderBySeq(reportId, part.getPartId())
                            .stream()
                            .map(a -> ReportDailyDto.Attach.builder()
                                    .seq(a.getSeq())
                                    .realFileName(a.getRealFileName())
                                    .fileSize(a.getFileSize())
                                    .regDt(a.getRegDt())
                                    .build())
                            .collect(Collectors.toList());

            return ReportDailyDto.PartDetail.builder()
                    .partId(part.getPartId())
                    .partNm(part.getPartNm())
                    .people(people)
                    .efficiencyContent(part.getEfficiencyContent())
                    .mainInstructionContent(part.getMainInstructionContent())
                    .wasErrorContent(part.getWasErrorContent())
                    .meetingSchedule(part.getMeetingSchedule())
                    .specialNotes(part.getSpecialNotes())
                    .attachments(attachments)
                    .build();
        }).collect(Collectors.toList());

        return ReportDailyDto.Detail.builder()
                .reportId(report.getReportId())
                .reportDate(report.getReportDate())
                .authorSabun(report.getAuthorSabun())
                .authorName(report.getAuthorName())
                .parts(partDetails)
                .build();
    }

    // ── 등록 ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public void registerReport(ReportDailyDto.RegisterRequest req, String authorSabun, String authorName,
                                MultiValueMap<String, MultipartFile> fileMap) throws IOException {

        SalesDailyReportVo saved = reportRepository.save(SalesDailyReportVo.builder()
                .reportDate(req.getReportDate())
                .authorSabun(authorSabun)
                .authorName(authorName)
                .build());
        Long reportId = saved.getReportId();

        if (req.getParts() == null) return;

        LocalDateTime now = LocalDateTime.now();
        String dateDir = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        for (ReportDailyDto.PartRequest partReq : req.getParts()) {
            String partId = partReq.getPartId();
            String partNm = partInfoRepository.findById(new PartInfoPk(SALES_DEP_ID, partId))
                    .map(PartInfoVo::getPartNm)
                    .orElse(null);

            partRepository.save(SalesDailyReportPartVo.builder()
                    .reportId(reportId)
                    .partId(partId)
                    .partNm(partNm)
                    .efficiencyContent(partReq.getEfficiencyContent())
                    .mainInstructionContent(partReq.getMainInstructionContent())
                    .wasErrorContent(partReq.getWasErrorContent())
                    .meetingSchedule(partReq.getMeetingSchedule())
                    .specialNotes(partReq.getSpecialNotes())
                    .build());

            if (partReq.getPeople() != null) {
                for (ReportDailyDto.PersonRequest p : partReq.getPeople()) {
                    personRepository.save(SalesDailyReportPersonVo.builder()
                            .reportId(reportId)
                            .partId(partId)
                            .personNm(p.getPersonNm())
                            .inProgressCnt(p.getInProgressCnt())
                            .delayedCnt(p.getDelayedCnt())
                            .distributedCnt(p.getDistributedCnt())
                            .build());
                }
            }

            saveAttachments(reportId, partId, dateDir, now, fileMap);
        }
    }

    private void saveAttachments(Long reportId, String partId, String dateDir, LocalDateTime now,
                                  MultiValueMap<String, MultipartFile> fileMap) throws IOException {
        if (fileMap == null) return;
        List<MultipartFile> files = fileMap.get("files_" + partId);
        if (files == null || files.isEmpty()) return;

        Path uploadPath = Paths.get(uploadDir, dateDir, String.valueOf(reportId), partId);
        Files.createDirectories(uploadPath);

        int seq = 1;
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            String realFileName = file.getOriginalFilename();
            String ext = realFileName != null && realFileName.contains(".")
                    ? realFileName.substring(realFileName.lastIndexOf("."))
                    : "";
            String savedFileName = UUID.randomUUID() + ext;
            Path targetPath = uploadPath.resolve(savedFileName);
            file.transferTo(targetPath.toFile());

            attachRepository.save(SalesDailyReportAttachVo.builder()
                    .reportId(reportId)
                    .partId(partId)
                    .seq(String.format("%03d", seq++))
                    .realFileName(realFileName)
                    .fileName(savedFileName)
                    .fileLocation(targetPath.toString())
                    .fileSize(file.getSize())
                    .attachFullType(ext.isEmpty() ? "ETC" : ext.substring(1).toUpperCase())
                    .regDt(now)
                    .build());
        }
    }

    // ── 첨부파일 다운로드 ─────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ReportDailyDto.DownloadFile downloadAttach(Long reportId, String partId, String seq) throws MalformedURLException {
        SalesDailyReportAttachVo attach = attachRepository
                .findById(new SalesDailyReportAttachPk(reportId, partId, seq))
                .orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다."));

        Path filePath = Paths.get(attach.getFileLocation());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("파일을 읽을 수 없습니다.");
        }
        return ReportDailyDto.DownloadFile.builder()
                .resource(resource)
                .realFileName(attach.getRealFileName())
                .build();
    }
}
