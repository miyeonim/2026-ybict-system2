package kepco.prorject.ictyb.back.ictyb_back.work_opinion.service;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.IctybWorkNegotiationVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.IctybWorkOpinionAttachVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.IctybWorkOpinionCmntVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.IctybWorkOpinionVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.IctybWorkOpinionAttachPk;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.IctybWorkNegotiationRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.IctybWorkOpinionAttachRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.IctybWorkOpinionCmntRepository;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkOpinionServiceImpl implements WorkOpinionService {

    private final IctybWorkOpinionRepository opinionRepo;
    private final IctybWorkOpinionCmntRepository cmntRepo;
    private final IctybWorkOpinionAttachRepository attachRepo;
    private final IctybWorkNegotiationRepository negotiationRepo;

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

        return discussions.stream()
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
