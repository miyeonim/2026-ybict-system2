package kepco.prorject.ictyb.back.ictyb_back.qna.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.qa.NoticeAttachVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.qa.NoticeVo;
import kepco.prorject.ictyb.back.ictyb_back.qna.model.QnaDto;
import kepco.prorject.ictyb.back.ictyb_back.qna.repository.NoticeAttachRepository;
import kepco.prorject.ictyb.back.ictyb_back.qna.repository.NoticeRepository;

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
public class QnaServiceImp implements QnaService{
    private final NoticeRepository       qnaRepository;
    private final NoticeAttachRepository attachRepository;

    /** 파일 업로드 루트 경로 (application.properties: file.upload-dir) */
    @Value("${file.upload-dir:./uploads/qna}")
    private String uploadDir;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    // ── 목록 조회 ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<QnaDto.ListItem> getQnaList() {
        List<NoticeVo> entities = qnaRepository.findActiveQnaList();

        return entities.stream().map(e -> {
            int attachCount = attachRepository.countByNoticeNo(String.valueOf(e.getNoticeNo()));
            return QnaDto.ListItem.builder()
                    .noticeNo(e.getNoticeNo())
                    .noticeTitle(e.getNoticeTitle())
                    .regUserName(e.getRegUserName())
                    .regDt(e.getRegDt())
                    .viewCnt(e.getViewCnt() != null ? e.getViewCnt() : 0)
                    .attachCount(attachCount)
                    .build();
        }).collect(Collectors.toList());
    }

    // ── 상세 조회 ─────────────────────────────────────────────────

    @Transactional
    public QnaDto.Detail getQnaDetail(Long noticeNo) {
        NoticeVo entity = qnaRepository.findById(noticeNo)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 Q&A입니다."));

        // 조회수 증가
        entity.setViewCnt((entity.getViewCnt() == null ? 0 : entity.getViewCnt()) + 1);

        // 첨부파일 목록
        List<NoticeAttachVo> attachEntities =
                attachRepository.findByNoticeNoOrderBySeqAsc(String.valueOf(noticeNo));

        List<QnaDto.Attach> attachList = attachEntities.stream()
                .map(a -> QnaDto.Attach.builder()
                        .noticeNo(a.getNoticeNo())
                        .seq(a.getSeq())
                        .realFileName(a.getRealFileName())
                        .fileName(a.getFileName())
                        .fileLocation(a.getFileLocation())
                        .regDt(a.getRegDt())
                        .attachType(a.getAttachType())
                        .fileSize(a.getFileSize())
                        .build())
                .collect(Collectors.toList());

        return QnaDto.Detail.builder()
                .noticeNo(entity.getNoticeNo())
                .noticeTitle(entity.getNoticeTitle())
                .noticeDepCd(entity.getNoticeDepCd())
                .noticeContents(entity.getNoticeContents())
                .priority(entity.getPriority() != null ? entity.getPriority() : 0)
                .regUserSabun(entity.getRegUserSabun())
                .regUserDepCd(entity.getRegUserDepCd())
                .regUserName(entity.getRegUserName())
                .regDt(entity.getRegDt())
                .endDt(entity.getEndDt())
                .delYn(entity.getDelYn())
                .viewCnt(entity.getViewCnt())
                .noticeType(entity.getNoticeType())
                .attachList(attachList)
                .build();
    }

    // ── 등록 ─────────────────────────────────────────────────────

    @Transactional
    public void registerQna(QnaDto.RegisterRequest req, List<MultipartFile> files) throws IOException {
        String now = LocalDateTime.now().format(DT_FMT);

        // endDt 포맷 변환 (yyyy-MM-dd → yyyyMMdd000000)
        String endDt = req.getEndDt() != null && !req.getEndDt().isEmpty()
                ? req.getEndDt().replace("-", "") + "000000"
                : null;

        // Q&A 저장
        NoticeVo entity = NoticeVo.builder()
                .noticeTitle(req.getNoticeTitle())
                .noticeContents(req.getNoticeContents())
                .noticeDepCd(req.getNoticeDepCd())
                .priority(req.getPriority())
                .regUserSabun(req.getRegUserSabun())
                .regUserDepCd(req.getRegUserDepCd())
                .regUserName(req.getRegUserName())
                .regDt(now)
                .endDt(endDt)
                .delYn("N")
                .viewCnt(0)
                .noticeType("Q")
                .build();

        NoticeVo saved = qnaRepository.save(entity);
        String noticeNoStr = String.valueOf(saved.getNoticeNo());

        // 첨부파일 저장
        if (files != null && !files.isEmpty()) {
            // 변경 - 년/월/일/문서번호
            String year  = now.substring(0, 4);  // 2026
            String month = now.substring(4, 6);  // 06
            String day   = now.substring(6, 8);  // 18
            Path uploadPath = Paths.get(uploadDir, year, month, day, noticeNoStr);
            Files.createDirectories(uploadPath);

            int seq = 1;
            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;

                String realFileName = file.getOriginalFilename();
                String ext          = realFileName != null && realFileName.contains(".")
                        ? realFileName.substring(realFileName.lastIndexOf("."))
                        : "";
                String savedFileName = UUID.randomUUID() + ext;
                Path   targetPath    = uploadPath.resolve(savedFileName);

                file.transferTo(targetPath.toFile());

                NoticeAttachVo attach = NoticeAttachVo.builder()
                        .noticeNo(noticeNoStr)
                        .seq(String.format("%03d", seq++))
                        .realFileName(realFileName)
                        .fileName(savedFileName)
                        .fileLocation(targetPath.toString())
                        .regDt(now)
                        .attachType(ext.isEmpty() ? "E" : ext.substring(1, 2).toUpperCase())  // 첫 글자만
                        .attachFullType(ext.isEmpty() ? "ETC" : ext.substring(1).toUpperCase())         // 풀 확장자
                        .fileSize(String.valueOf(file.getSize()))
                        .build();

                attachRepository.save(attach);
            }
        }
    }

    // ── 파일 다운로드 ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Resource downloadFile(String noticeNo, String seq) throws MalformedURLException {
        NoticeAttachVo attach = attachRepository
                //.findById(new com.example.qna.entity.QnaAttachId(noticeNo, seq))
                .findById(new kepco.prorject.ictyb.back.ictyb_back.common.voArea.qa.pk.NoticeAttachPk(noticeNo, seq))
                .orElseThrow(() -> new IllegalArgumentException("첨부파일을 찾을 수 없습니다."));

        Path filePath = Paths.get(attach.getFileLocation());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("파일을 읽을 수 없습니다.");
        }
        return resource;
    }
}
