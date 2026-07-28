package kepco.prorject.ictyb.back.ictyb_back.work_opinion.service;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.IctybWorkNegotiationVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.IctybWorkOpinionAttachVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.IctybWorkOpinionCmntVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.IctybWorkOpinionReadLogVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.IctybWorkOpinionVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.IctybWorkOpinionAttachPk;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.IctybWorkOpinionReadLogPk;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.IctybWorkNegotiationRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.IctybWorkOpinionAttachRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.IctybWorkOpinionCmntRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.IctybWorkOpinionReadLogRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.IctybWorkOpinionRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_opinion.model.WorkOpinionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkOpinionServiceImpl implements WorkOpinionService {

    private final IctybWorkOpinionRepository opinionRepo;
    private final IctybWorkOpinionCmntRepository cmntRepo;
    private final IctybWorkOpinionAttachRepository attachRepo;
    private final IctybWorkNegotiationRepository negotiationRepo;
    private final IctybWorkOpinionReadLogRepository readLogRepo;

    @Value("${file.work-opinion-upload-dir:./uploads/work_opinion}")
    private String uploadDir;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter REG_DT_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public List<WorkOpinionDto.DiscussionItem> getDiscussions(String instrNo) {
        List<IctybWorkOpinionVo> discussions = opinionRepo.findByInstrNo(instrNo);

        if (discussions.isEmpty()) return List.of();

        List<String> opnIds = discussions.stream()
                .map(IctybWorkOpinionVo::getOpnId)
                .collect(Collectors.toList());

        List<IctybWorkOpinionCmntVo> comments = cmntRepo.findByOpnIdIn(opnIds);
        List<String> cmntIds = comments.stream().map(IctybWorkOpinionCmntVo::getCmntId).collect(Collectors.toList());
        Map<String, List<WorkOpinionDto.AttachmentItem>> attachByCmntId = attachRepo.findByCmntIdIn(cmntIds).stream()
                .collect(Collectors.groupingBy(IctybWorkOpinionAttachVo::getCmntId,
                        Collectors.mapping(this::toAttachmentItem, Collectors.toList())));

        Map<String, List<WorkOpinionDto.CommentItem>> commentMap = comments
                .stream()
                .map(c -> WorkOpinionDto.CommentItem.builder()
                        .cmntId(c.getCmntId())
                        .opnId(c.getOpnId())
                        .cmntCtt(c.getCmntCtt())
                        .wrtrEmpno(c.getWrtrEmpno())
                        .wrtrNm(c.getWrtrNm())
                        .wrtrRoleNm(c.getWrtrRoleNm())
                        .regDt(c.getFrstRegDt() != null ? c.getFrstRegDt().format(FMT) : null)
                        .attachments(attachByCmntId.getOrDefault(c.getCmntId(), List.of()))
                        .build())
                .collect(Collectors.groupingBy(WorkOpinionDto.CommentItem::getOpnId));

        // 협의 등록일과, 그 안에 달린 모든 댓글(첨부파일은 댓글과 함께 등록되므로 별도 고려 불필요) 등록일 중
        // 가장 최근 시각을 "최종 활동 시각"으로 보고, 이 시각 기준 내림차순으로 정렬한다.
        Map<String, LocalDateTime> lastActivityByOpnId = computeLastActivityByOpnId(discussions, comments);

        // nullsLast(...).reversed()는 .reversed()가 null 배치까지 뒤집어 활동 시각이 없는 항목이
        // 오히려 맨 앞으로 오는 버그가 있다 - nullsLast(reverseOrder())로 내림차순+null-후순위를
        // 한 번에 표현해야 한다(2026-07-13, work_my 목록 정렬에서 동일 패턴으로 발견).
        return discussions.stream()
                .sorted(Comparator.comparing(
                        (IctybWorkOpinionVo d) -> lastActivityByOpnId.get(d.getOpnId()),
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(d -> WorkOpinionDto.DiscussionItem.builder()
                        .opnId(d.getOpnId())
                        .instrNo(d.getInstrNo())
                        .opnTitle(d.getOpnTitle())
                        .wrtrEmpno(d.getWrtrEmpno())
                        .wrtrNm(d.getWrtrNm())
                        .regDt(d.getFrstRegDt() != null ? d.getFrstRegDt().format(FMT) : null)
                        .comments(commentMap.getOrDefault(d.getOpnId(), List.of()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WorkOpinionDto.DiscussionItem createDiscussion(WorkOpinionDto.CreateDiscussionReq req,
                                                            List<MultipartFile> files) throws IOException {
        IctybWorkOpinionVo vo = new IctybWorkOpinionVo();
        vo.setInstrNo(req.getInstrNo());
        vo.setOpnTitle(req.getOpnTitle());
        vo.setWrtrEmpno(req.getWrtrEmpno());
        vo.setWrtrNm(req.getWrtrNm());
        vo.setFrstRegrEmpno(req.getWrtrEmpno());
        vo.setLstChgrEmpno(req.getWrtrEmpno());

        IctybWorkOpinionVo saved = opinionRepo.save(vo);

        // 협의(피드백) 시작 시, 이 건의 결재선을 거쳐간 사람들의 작업지시서(MY) 피드백 탭에 표시되도록 협의 플래그를 세운다.
        markNegotiation(req.getInstrNo(), req.getWrtrEmpno(), req.getWrtrNm());

        // 본인이 방금 등록한 협의는 본인에게 안읽음(new!)으로 뜨면 안 되므로, 작성 즉시 본인 기준으로는
        // 읽음 처리해둔다.
        markReadForAuthor(saved.getOpnId(), req.getWrtrEmpno());

        // 협의 스레드 자체에는 첨부 테이블이 없으므로, 첨부파일이 있으면 이를 담을 최초 댓글을 함께 생성한다.
        List<WorkOpinionDto.CommentItem> comments = List.of();
        if (files != null && files.stream().anyMatch(f -> !f.isEmpty())) {
            WorkOpinionDto.CommentItem initialComment = saveComment(
                    saved.getOpnId(), null, req.getWrtrEmpno(), req.getWrtrNm(), req.getWrtrRoleNm(), files);
            comments = List.of(initialComment);
        }

        return WorkOpinionDto.DiscussionItem.builder()
                .opnId(saved.getOpnId())
                .instrNo(saved.getInstrNo())
                .opnTitle(saved.getOpnTitle())
                .wrtrEmpno(saved.getWrtrEmpno())
                .wrtrNm(saved.getWrtrNm())
                .regDt(saved.getFrstRegDt() != null ? saved.getFrstRegDt().format(FMT) : null)
                .comments(comments)
                .build();
    }

    @Override
    @Transactional
    public WorkOpinionDto.CommentItem addComment(WorkOpinionDto.CreateCommentReq req, List<MultipartFile> files)
            throws IOException {
        return saveComment(req.getOpnId(), req.getCmntCtt(), req.getWrtrEmpno(), req.getWrtrNm(),
                req.getWrtrRoleNm(), files);
    }

    @Override
    @Transactional
    public void markRead(String instrNo, String sabun) {
        List<String> opnIds = opinionRepo.findByInstrNo(instrNo).stream()
                .map(IctybWorkOpinionVo::getOpnId)
                .collect(Collectors.toList());
        if (opnIds.isEmpty()) return;

        Map<String, IctybWorkOpinionReadLogVo> existing = readLogRepo.findByOpnIdInAndSabun(opnIds, sabun).stream()
                .collect(Collectors.toMap(IctybWorkOpinionReadLogVo::getOpnId, l -> l));

        LocalDateTime now = LocalDateTime.now();
        List<IctybWorkOpinionReadLogVo> toSave = opnIds.stream()
                .map(opnId -> {
                    IctybWorkOpinionReadLogVo log = existing.getOrDefault(opnId,
                            IctybWorkOpinionReadLogVo.builder().opnId(opnId).sabun(sabun).build());
                    log.setReadDt(now);
                    return log;
                })
                .collect(Collectors.toList());
        readLogRepo.saveAll(toSave);
    }

    /**
     * 협의 스레드(opnId)에 새 협의/댓글을 작성한 그 순간, 작성자 본인 기준으로는 방금 쓴 내용을
     * 이미 아는 것이므로 즉시 읽음 처리한다. 이렇게 하지 않으면 방금 본인이 작성한 활동 때문에
     * 그 스레드가 속한 지시번호가 본인 화면에도 안읽음(new!)으로 뜨는 문제가 있었다(2026-07-13).
     */
    private void markReadForAuthor(String opnId, String sabun) {
        IctybWorkOpinionReadLogVo log = readLogRepo.findByOpnIdInAndSabun(List.of(opnId), sabun).stream()
                .findFirst()
                .orElse(IctybWorkOpinionReadLogVo.builder().opnId(opnId).sabun(sabun).build());
        log.setReadDt(LocalDateTime.now());
        readLogRepo.save(log);
    }

    @Override
    public Set<String> getUnreadInstrNos(List<String> instrNos, String sabun) {
        if (instrNos == null || instrNos.isEmpty()) return Set.of();

        List<IctybWorkOpinionVo> discussions = opinionRepo.findByInstrNoIn(instrNos);
        if (discussions.isEmpty()) return Set.of();

        List<String> opnIds = discussions.stream().map(IctybWorkOpinionVo::getOpnId).collect(Collectors.toList());
        List<IctybWorkOpinionCmntVo> comments = cmntRepo.findByOpnIdIn(opnIds);
        Map<String, LocalDateTime> lastActivityByOpnId = computeLastActivityByOpnId(discussions, comments);

        Map<String, LocalDateTime> readDtByOpnId = readLogRepo.findByOpnIdInAndSabun(opnIds, sabun).stream()
                .collect(Collectors.toMap(IctybWorkOpinionReadLogVo::getOpnId, IctybWorkOpinionReadLogVo::getReadDt));

        Set<String> unreadInstrNos = new HashSet<>();
        for (IctybWorkOpinionVo d : discussions) {
            LocalDateTime lastActivity = lastActivityByOpnId.get(d.getOpnId());
            LocalDateTime readDt = readDtByOpnId.get(d.getOpnId());
            if (lastActivity != null && (readDt == null || lastActivity.isAfter(readDt))) {
                unreadInstrNos.add(d.getInstrNo());
            }
        }
        return unreadInstrNos;
    }

    @Override
    public Map<String, LocalDateTime> getLastActivityByInstrNo(List<String> instrNos) {
        if (instrNos == null || instrNos.isEmpty()) return Map.of();

        List<IctybWorkOpinionVo> discussions = opinionRepo.findByInstrNoIn(instrNos);
        if (discussions.isEmpty()) return Map.of();

        List<String> opnIds = discussions.stream().map(IctybWorkOpinionVo::getOpnId).collect(Collectors.toList());
        List<IctybWorkOpinionCmntVo> comments = cmntRepo.findByOpnIdIn(opnIds);
        Map<String, LocalDateTime> lastActivityByOpnId = computeLastActivityByOpnId(discussions, comments);

        Map<String, LocalDateTime> lastActivityByInstrNo = new HashMap<>();
        for (IctybWorkOpinionVo d : discussions) {
            LocalDateTime activity = lastActivityByOpnId.get(d.getOpnId());
            if (activity == null) continue;
            lastActivityByInstrNo.merge(d.getInstrNo(), activity, (a, b) -> a.isAfter(b) ? a : b);
        }
        return lastActivityByInstrNo;
    }

    /**
     * 협의 등록일과, 그 안에 달린 모든 댓글 등록일 중 가장 최근 시각을 "최종 활동 시각"으로 계산한다.
     * 정렬(getDiscussions)과 읽음 여부 판정(getUnreadInstrNos) 양쪽에서 공유해 쓴다.
     */
    private Map<String, LocalDateTime> computeLastActivityByOpnId(List<IctybWorkOpinionVo> discussions,
                                                                     List<IctybWorkOpinionCmntVo> comments) {
        Map<String, LocalDateTime> lastActivityByOpnId = new HashMap<>();
        for (IctybWorkOpinionVo d : discussions) {
            lastActivityByOpnId.put(d.getOpnId(), d.getFrstRegDt());
        }
        for (IctybWorkOpinionCmntVo c : comments) {
            LocalDateTime current = lastActivityByOpnId.get(c.getOpnId());
            LocalDateTime cmntDt = c.getFrstRegDt();
            if (cmntDt != null && (current == null || cmntDt.isAfter(current))) {
                lastActivityByOpnId.put(c.getOpnId(), cmntDt);
            }
        }
        return lastActivityByOpnId;
    }

    private void markNegotiation(String instId, String regSabun, String regName) {
        IctybWorkNegotiationVo negotiation = negotiationRepo.findById(instId)
                .orElse(IctybWorkNegotiationVo.builder().instId(instId).build());
        negotiation.setNegotiationYn("Y");
        negotiation.setRegSabun(regSabun);
        negotiation.setRegName(regName);
        negotiation.setRegDt(LocalDateTime.now().format(REG_DT_FMT));
        negotiationRepo.save(negotiation);
    }

    private WorkOpinionDto.CommentItem saveComment(String opnId, String cmntCtt, String wrtrEmpno, String wrtrNm,
                                                     String wrtrRoleNm, List<MultipartFile> files) throws IOException {
        IctybWorkOpinionCmntVo vo = new IctybWorkOpinionCmntVo();
        vo.setOpnId(opnId);
        vo.setCmntCtt(cmntCtt);
        vo.setWrtrEmpno(wrtrEmpno);
        vo.setWrtrNm(wrtrNm);
        vo.setWrtrRoleNm(wrtrRoleNm);
        vo.setFrstRegrEmpno(wrtrEmpno);
        vo.setLstChgrEmpno(wrtrEmpno);

        IctybWorkOpinionCmntVo saved = cmntRepo.save(vo);

        // 본인이 방금 단 댓글도 마찬가지로 본인 기준 즉시 읽음 처리한다 (markReadForAuthor 주석 참고).
        markReadForAuthor(opnId, wrtrEmpno);

        List<WorkOpinionDto.AttachmentItem> attachments = saveAttachments(saved.getCmntId(), files);

        return WorkOpinionDto.CommentItem.builder()
                .cmntId(saved.getCmntId())
                .opnId(saved.getOpnId())
                .cmntCtt(saved.getCmntCtt())
                .wrtrEmpno(saved.getWrtrEmpno())
                .wrtrNm(saved.getWrtrNm())
                .wrtrRoleNm(saved.getWrtrRoleNm())
                .regDt(saved.getFrstRegDt() != null ? saved.getFrstRegDt().format(FMT) : null)
                .attachments(attachments)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkOpinionDto.DownloadFile downloadAttach(String cmntId, Long seqNo) throws MalformedURLException {
        IctybWorkOpinionAttachVo attach = attachRepo.findById(new IctybWorkOpinionAttachPk(cmntId, seqNo))
                .orElseThrow(() -> new IllegalStateException("첨부파일을 찾을 수 없습니다."));

        Path filePath = Paths.get(attach.getFilePthCtt());
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new IllegalStateException("파일을 읽을 수 없습니다.");
        }
        return WorkOpinionDto.DownloadFile.builder()
                .resource(resource)
                .realFileName(attach.getRlFileNm())
                .build();
    }

    private List<WorkOpinionDto.AttachmentItem> saveAttachments(String cmntId, List<MultipartFile> files)
            throws IOException {
        if (files == null || files.isEmpty()) return List.of();

        LocalDateTime now = LocalDateTime.now();
        String dateDir = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path uploadPath = Paths.get(uploadDir, dateDir, cmntId);
        Files.createDirectories(uploadPath);

        List<WorkOpinionDto.AttachmentItem> result = new ArrayList<>();
        long seq = 1;
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            String realFileName = file.getOriginalFilename();
            String ext = realFileName != null && realFileName.contains(".")
                    ? realFileName.substring(realFileName.lastIndexOf("."))
                    : "";
            String savedFileName = UUID.randomUUID() + ext;
            Path targetPath = uploadPath.resolve(savedFileName);
            file.transferTo(targetPath.toFile());

            IctybWorkOpinionAttachVo attach = new IctybWorkOpinionAttachVo();
            attach.setCmntId(cmntId);
            attach.setSeqNo(seq++);
            attach.setRlFileNm(realFileName);
            attach.setFileNm(savedFileName);
            attach.setFilePthCtt(targetPath.toString());
            attach.setFileCpct(file.getSize());

            IctybWorkOpinionAttachVo saved = attachRepo.save(attach);
            result.add(toAttachmentItem(saved));
        }
        return result;
    }

    private WorkOpinionDto.AttachmentItem toAttachmentItem(IctybWorkOpinionAttachVo a) {
        return WorkOpinionDto.AttachmentItem.builder()
                .seqNo(a.getSeqNo())
                .realFileName(a.getRlFileNm())
                .fileSize(a.getFileCpct())
                .build();
    }
}
