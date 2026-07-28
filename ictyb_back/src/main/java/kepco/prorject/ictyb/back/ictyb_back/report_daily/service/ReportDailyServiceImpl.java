package kepco.prorject.ictyb.back.ictyb_back.report_daily.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.PartInfoVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.SalesDailyReportAttachVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.SalesDailyReportPartVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.SalesDailyReportPersonVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.SalesDailyReportVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.UserInfoVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.PartInfoPk;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.SalesDailyReportAttachPk;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.SalesDailyReportPartPk;
import kepco.prorject.ictyb.back.ictyb_back.report_daily.model.ReportDailyDto;
import kepco.prorject.ictyb.back.ictyb_back.report_daily.model.ReportPartStatus;
import kepco.prorject.ictyb.back.ictyb_back.report_daily.repository.ItWorkReportRepository;
import kepco.prorject.ictyb.back.ictyb_back.report_daily.repository.PartInfoRepository;
import kepco.prorject.ictyb.back.ictyb_back.report_daily.repository.SalesDailyReportAttachRepository;
import kepco.prorject.ictyb.back.ictyb_back.report_daily.repository.SalesDailyReportPartRepository;
import kepco.prorject.ictyb.back.ictyb_back.report_daily.repository.SalesDailyReportPersonRepository;
import kepco.prorject.ictyb.back.ictyb_back.report_daily.repository.SalesDailyReportRepository;
import kepco.prorject.ictyb.back.ictyb_back.report_daily.repository.UserInfoRepository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportDailyServiceImpl implements ReportDailyService {

    /** 영업시스템운영부 (ictyb_part_info.DEP_ID) - 영업 점검일지는 이 부서 산하 파트로 고정 */
    private static final String SALES_DEP_ID = "9611175";

    private final SalesDailyReportRepository reportRepository;
    private final SalesDailyReportPartRepository partRepository;
    private final SalesDailyReportPersonRepository personRepository;
    private final SalesDailyReportAttachRepository attachRepository;
    private final PartInfoRepository partInfoRepository;
    private final ItWorkReportRepository itWorkReportRepository;
    private final UserInfoRepository userInfoRepository;

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
                    .overallStatus(computeOverallStatus(parts))
                    .build();
        }).collect(Collectors.toList());
    }

    private String computeOverallStatus(List<SalesDailyReportPartVo> parts) {
        if (parts.isEmpty()) return "작성중";
        if (parts.stream().anyMatch(p -> p.getStatus() == ReportPartStatus.REJECTED)) return "반려";
        if (parts.stream().allMatch(p -> p.getStatus() == ReportPartStatus.FINAL_APPROVED)) return "승인완료";
        if (parts.stream().anyMatch(p -> p.getStatus() == ReportPartStatus.PART_APPROVED
                || p.getStatus() == ReportPartStatus.FINAL_APPROVED)) return "부장결재대기";
        if (parts.stream().anyMatch(p -> p.getStatus() == ReportPartStatus.SUBMITTED)) return "파트장결재대기";
        return "작성중";
    }

    // ── 상세 조회 (reportId 기준, 과거 호환용) ───────────────────────

    @Override
    @Transactional(readOnly = true)
    public ReportDailyDto.Detail getReportDetail(Long reportId) {
        SalesDailyReportVo report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 점검일지입니다."));

        List<SalesDailyReportPartVo> parts = partRepository.findByReportIdOrderByPartId(reportId);
        List<ReportDailyDto.PartDetail> partDetails = parts.stream()
                .map(part -> buildPartDetail(part, null, null, null))
                .collect(Collectors.toList());

        return ReportDailyDto.Detail.builder()
                .reportId(report.getReportId())
                .reportDate(report.getReportDate())
                .authorSabun(report.getAuthorSabun())
                .authorName(report.getAuthorName())
                .parts(partDetails)
                .build();
    }

    // ── 날짜 기준 조회 (신규 화면용, 호출자 권한 포함) ─────────────────

    @Override
    @Transactional(readOnly = true)
    public ReportDailyDto.Detail getReportByDate(LocalDate reportDate, String callerSabun) {
        List<ReportDailyDto.PartOption> partOptions = getPartOptions();
        UserInfoVo deptHead = userInfoRepository.findCurrentDeptHead(SALES_DEP_ID).orElse(null);

        SalesDailyReportVo report = reportRepository.findByReportDate(reportDate).orElse(null);
        Long reportId = report != null ? report.getReportId() : null;

        List<SalesDailyReportPartVo> existingParts = reportId != null
                ? partRepository.findByReportIdOrderByPartId(reportId)
                : List.of();

        List<ReportDailyDto.PartDetail> partDetails = partOptions.stream().map(option -> {
            SalesDailyReportPartVo part = existingParts.stream()
                    .filter(p -> p.getPartId().equals(option.getPartId()))
                    .findFirst()
                    .orElseGet(() -> SalesDailyReportPartVo.builder()
                            .partId(option.getPartId())
                            .partNm(option.getPartNm())
                            .status(ReportPartStatus.DRAFT)
                            .build());

            UserInfoVo callerMembership = userInfoRepository
                    .findCurrentMembership(option.getPartId(), callerSabun).orElse(null);
            UserInfoVo partLeader = userInfoRepository
                    .findCurrentPartLeader(option.getPartId()).orElse(null);

            return buildPartDetail(part, callerMembership, partLeader, deptHead, callerSabun);
        }).collect(Collectors.toList());

        return ReportDailyDto.Detail.builder()
                .reportId(reportId)
                .reportDate(reportDate)
                .authorSabun(report != null ? report.getAuthorSabun() : null)
                .authorName(report != null ? report.getAuthorName() : null)
                .parts(partDetails)
                .build();
    }

    /** 과거 호환용 (reportId 기준 상세) - 호출자 권한 정보 없이 상태/작성자/승인자 정보만 채운다. */
    private ReportDailyDto.PartDetail buildPartDetail(SalesDailyReportPartVo part, UserInfoVo callerMembership,
                                                        UserInfoVo partLeader, UserInfoVo deptHead) {
        return buildPartDetail(part, callerMembership, partLeader, deptHead, null);
    }

    private ReportDailyDto.PartDetail buildPartDetail(SalesDailyReportPartVo part, UserInfoVo callerMembership,
                                                        UserInfoVo partLeader, UserInfoVo deptHead,
                                                        String callerSabun) {
        Long reportId = part.getReportId();
        String partId = part.getPartId();

        // 아직 한 번도 제출되지 않은 파트(reportId 없음)는 저장된 인원 스냅샷이 없으므로,
        // 작업지시서 DB 집계(getPersonStats)로 초기 인원 목록을 채워준다 - 기존 등록 화면이
        // fetchPersonStats로 미리 채워주던 것과 동일한 동작.
        List<ReportDailyDto.Person> people = reportId == null
                ? getPersonStats(partId).stream()
                        .map(s -> ReportDailyDto.Person.builder()
                                .personNm(s.getPersonNm())
                                .inProgressCnt(s.getInProgressCnt())
                                .delayedCnt(s.getDelayedCnt())
                                .distributedCnt(0)
                                .build())
                        .collect(Collectors.toList())
                : personRepository.findByReportIdAndPartIdOrderByPersonSeq(reportId, partId)
                        .stream()
                        .map(p -> ReportDailyDto.Person.builder()
                                .personNm(p.getPersonNm())
                                .inProgressCnt(p.getInProgressCnt() != null ? p.getInProgressCnt() : 0)
                                .delayedCnt(p.getDelayedCnt() != null ? p.getDelayedCnt() : 0)
                                .distributedCnt(p.getDistributedCnt() != null ? p.getDistributedCnt() : 0)
                                .build())
                        .collect(Collectors.toList());

        List<ReportDailyDto.Attach> attachments = reportId == null ? List.of()
                : attachRepository.findByReportIdAndPartIdOrderBySeq(reportId, partId)
                        .stream()
                        .map(a -> ReportDailyDto.Attach.builder()
                                .seq(a.getSeq())
                                .realFileName(a.getRealFileName())
                                .fileSize(a.getFileSize())
                                .regDt(a.getRegDt())
                                .build())
                        .collect(Collectors.toList());

        ReportPartStatus status = part.getStatus() != null ? part.getStatus() : ReportPartStatus.DRAFT;

        ReportDailyDto.PartDetail.PartDetailBuilder builder = ReportDailyDto.PartDetail.builder()
                .partId(partId)
                .partNm(part.getPartNm())
                .people(people)
                .efficiencyContent(part.getEfficiencyContent())
                .mainInstructionContent(part.getMainInstructionContent())
                .wasErrorContent(part.getWasErrorContent())
                .meetingSchedule(part.getMeetingSchedule())
                .specialNotes(part.getSpecialNotes())
                .attachments(attachments)
                .status(status.name())
                .authorSabun(part.getAuthorSabun())
                .authorName(part.getAuthorName())
                .submittedDt(part.getSubmittedDt())
                .partLeaderName(part.getPartLeaderName())
                .partLeaderApprovedDt(part.getPartLeaderApprovedDt())
                .headName(part.getHeadName())
                .headApprovedDt(part.getHeadApprovedDt())
                .rejectedByName(part.getRejectedByName())
                .rejectedByRole(part.getRejectedByRole())
                .rejectReason(part.getRejectReason())
                .rejectedDt(part.getRejectedDt());

        boolean isHead = callerSabun != null && deptHead != null && deptHead.getEmpno().equals(callerSabun);
        boolean isLeader = callerSabun != null && partLeader != null && partLeader.getEmpno().equals(callerSabun);
        boolean isMember = callerSabun != null && callerMembership != null
                && !"Y".equals(callerMembership.getPartleaderYn());

        String myRole;
        boolean canEdit = false;
        boolean canApprove = false;
        boolean canReject = false;

        if (isHead) {
            myRole = "HEAD";
            canApprove = status == ReportPartStatus.PART_APPROVED;
            canReject = status == ReportPartStatus.PART_APPROVED;
        } else if (isLeader) {
            myRole = "LEADER";
            canApprove = status == ReportPartStatus.SUBMITTED;
            canReject = status == ReportPartStatus.SUBMITTED;
        } else if (isMember) {
            myRole = "AUTHOR";
            canEdit = status == ReportPartStatus.DRAFT || status == ReportPartStatus.REJECTED;
        } else {
            myRole = "VIEWER";
        }

        return builder.myRole(myRole).canEdit(canEdit).canApprove(canApprove).canReject(canReject).build();
    }

    // ── 파트 제출 ─────────────────────────────────────────────────

    @Override
    @Transactional
    public void submitPart(LocalDate reportDate, String partId, ReportDailyDto.SubmitPartRequest req,
                            String sabun, String name, MultiValueMap<String, MultipartFile> fileMap) throws IOException {

        UserInfoVo membership = userInfoRepository.findCurrentMembership(partId, sabun)
                .orElseThrow(() -> new SecurityException("해당 파트 소속이 아니라 작성할 수 없습니다."));
        if ("Y".equals(membership.getPartleaderYn())) {
            throw new SecurityException("파트장은 파트 내용을 직접 작성할 수 없습니다. 결재만 가능합니다.");
        }

        SalesDailyReportVo report = getOrCreateReport(reportDate, sabun, name);
        Long reportId = report.getReportId();

        SalesDailyReportPartVo existing = partRepository.findById(new SalesDailyReportPartPk(reportId, partId))
                .orElse(null);
        if (existing != null && existing.getStatus() != null
                && existing.getStatus() != ReportPartStatus.DRAFT
                && existing.getStatus() != ReportPartStatus.REJECTED) {
            throw new IllegalStateException("이미 결재가 진행 중인 파트는 다시 제출할 수 없습니다.");
        }

        String partNm = partInfoRepository.findById(new PartInfoPk(SALES_DEP_ID, partId))
                .map(PartInfoVo::getPartNm)
                .orElse(null);

        LocalDateTime now = LocalDateTime.now();

        partRepository.save(SalesDailyReportPartVo.builder()
                .reportId(reportId)
                .partId(partId)
                .partNm(partNm)
                .efficiencyContent(req.getEfficiencyContent())
                .mainInstructionContent(req.getMainInstructionContent())
                .wasErrorContent(req.getWasErrorContent())
                .meetingSchedule(req.getMeetingSchedule())
                .specialNotes(req.getSpecialNotes())
                .authorSabun(sabun)
                .authorName(name)
                .status(ReportPartStatus.SUBMITTED)
                .submittedDt(now)
                .build());

        personRepository.deleteByReportIdAndPartId(reportId, partId);
        if (req.getPeople() != null) {
            for (ReportDailyDto.PersonRequest p : req.getPeople()) {
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

        String dateDir = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        saveAttachments(reportId, partId, dateDir, now, fileMap);

        report.setAuthorSabun(sabun);
        report.setAuthorName(name);
        reportRepository.save(report);
    }

    private SalesDailyReportVo getOrCreateReport(LocalDate reportDate, String sabun, String name) {
        return reportRepository.findByReportDate(reportDate).orElseGet(() -> {
            try {
                return reportRepository.save(SalesDailyReportVo.builder()
                        .reportDate(reportDate)
                        .authorSabun(sabun)
                        .authorName(name)
                        .build());
            } catch (DataIntegrityViolationException e) {
                return reportRepository.findByReportDate(reportDate)
                        .orElseThrow(() -> e);
            }
        });
    }

    private void saveAttachments(Long reportId, String partId, String dateDir, LocalDateTime now,
                                  MultiValueMap<String, MultipartFile> fileMap) throws IOException {
        if (fileMap == null) return;
        List<MultipartFile> files = fileMap.get("files");
        if (files == null || files.isEmpty()) return;

        Path uploadPath = Paths.get(uploadDir, dateDir, String.valueOf(reportId), partId);
        Files.createDirectories(uploadPath);

        int seq = attachRepository.findByReportIdAndPartIdOrderBySeq(reportId, partId).size() + 1;
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

    // ── 결재 승인 / 반려 ─────────────────────────────────────────────

    @Override
    @Transactional
    public void approvePart(Long reportId, String partId, String sabun, String name) {
        SalesDailyReportPartVo part = partRepository.findById(new SalesDailyReportPartPk(reportId, partId))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 파트입니다."));

        ReportPartStatus status = part.getStatus();
        LocalDateTime now = LocalDateTime.now();

        if (status == ReportPartStatus.SUBMITTED) {
            UserInfoVo leader = userInfoRepository.findCurrentPartLeader(partId).orElse(null);
            if (leader == null || !leader.getEmpno().equals(sabun)) {
                throw new SecurityException("이 파트의 파트장만 승인할 수 있습니다.");
            }
            part.setStatus(ReportPartStatus.PART_APPROVED);
            part.setPartLeaderSabun(sabun);
            part.setPartLeaderName(name);
            part.setPartLeaderApprovedDt(now);
        } else if (status == ReportPartStatus.PART_APPROVED) {
            UserInfoVo head = userInfoRepository.findCurrentDeptHead(SALES_DEP_ID).orElse(null);
            if (head == null || !head.getEmpno().equals(sabun)) {
                throw new SecurityException("부장만 최종 승인할 수 있습니다.");
            }
            part.setStatus(ReportPartStatus.FINAL_APPROVED);
            part.setHeadSabun(sabun);
            part.setHeadName(name);
            part.setHeadApprovedDt(now);
        } else {
            throw new IllegalStateException("현재 상태(" + status + ")에서는 승인할 수 없습니다.");
        }

        partRepository.save(part);
    }

    @Override
    @Transactional
    public void rejectPart(Long reportId, String partId, String sabun, String name, String reason) {
        SalesDailyReportPartVo part = partRepository.findById(new SalesDailyReportPartPk(reportId, partId))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 파트입니다."));

        ReportPartStatus status = part.getStatus();
        String rejectedByRole;

        if (status == ReportPartStatus.SUBMITTED) {
            UserInfoVo leader = userInfoRepository.findCurrentPartLeader(partId).orElse(null);
            if (leader == null || !leader.getEmpno().equals(sabun)) {
                throw new SecurityException("이 파트의 파트장만 반려할 수 있습니다.");
            }
            rejectedByRole = "PART_LEADER";
        } else if (status == ReportPartStatus.PART_APPROVED) {
            UserInfoVo head = userInfoRepository.findCurrentDeptHead(SALES_DEP_ID).orElse(null);
            if (head == null || !head.getEmpno().equals(sabun)) {
                throw new SecurityException("부장만 반려할 수 있습니다.");
            }
            rejectedByRole = "HEAD";
        } else {
            throw new IllegalStateException("현재 상태(" + status + ")에서는 반려할 수 없습니다.");
        }

        part.setStatus(ReportPartStatus.REJECTED);
        part.setRejectedBySabun(sabun);
        part.setRejectedByName(name);
        part.setRejectedByRole(rejectedByRole);
        part.setRejectReason(reason);
        part.setRejectedDt(LocalDateTime.now());

        partRepository.save(part);
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
