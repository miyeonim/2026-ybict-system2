package kepco.prorject.ictyb.back.ictyb_back.work_my.service;

import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.IctybWorkNegotiationVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.ItWorkReportAttachVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.ItWorkReportVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.NSignVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.UserInfoVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.WorkHistoryVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.WorkOpinionReqVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.WorkOpinionVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.WorkResultAttachVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.WorkResultVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.KdnDepVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.NSignPk;
import kepco.prorject.ictyb.back.ictyb_back.jwt.repository.KdnDepRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.model.WorkMyDto;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.IctybWorkNegotiationRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.ItReceiverRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.ItWorkReportAttachRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.ItWorkReportRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.NSignRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.ReceiveRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.KepcoUserRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.UserInfoRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.WorkHistoryRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.WorkOpinionReqRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.WorkOpinionRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.WorkResultAttachRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.WorkResultRepository;
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkMyServiceImpl implements WorkMyService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ItWorkReportRepository itWorkReportRepository;
    private final ReceiveRepository receiveRepository;
    private final ItReceiverRepository itReceiverRepository;
    private final NSignRepository nSignRepository;
    private final WorkHistoryRepository workHistoryRepository;
    private final WorkOpinionRepository workOpinionRepository;
    private final WorkOpinionReqRepository workOpinionReqRepository;
    private final IctybWorkNegotiationRepository ictybWorkNegotiationRepository;
    private final KepcoUserRepository kepcoUserRepository;
    private final UserInfoRepository userInfoRepository;
    private final KdnDepRepository kdnDepRepository;
    private final ItWorkReportAttachRepository itWorkReportAttachRepository;
    private final WorkResultRepository workResultRepository;
    private final WorkResultAttachRepository workResultAttachRepository;

    @Value("${file.work-my-upload-dir:./uploads/work_my}")
    private String uploadDir;

    /** ictyb_kepco_user 로그인 지원을 위해 its_kdn_dep에 함께 등록해둔 한전 부서코드 (실제 KDN 작업부서 아님). */
    private static final String KEPCO_DUMMY_DEP_ID = "KP_YBDS";

    @Override
    public List<WorkMyDto.ListItem> getWorksMyList(String sabun) {
        // 1) 관련자 판단: 작업자/등록자/결재자(고정필드)/수신자/접수자/처리이력 중 하나라도 해당하면 MY 목록에 포함된다.
        Set<String> relatedInstIds = new LinkedHashSet<>();
        itWorkReportRepository.findByWorkerSabun(sabun).forEach(v -> relatedInstIds.add(v.getInstId()));
        itWorkReportRepository.findByRegUserSabun(sabun).forEach(v -> relatedInstIds.add(v.getInstId()));
        itWorkReportRepository.findByApproverSabun(sabun).forEach(v -> relatedInstIds.add(v.getInstId()));
        receiveRepository.findBySabun(sabun).forEach(v -> relatedInstIds.add(v.getInstId()));
        itReceiverRepository.findBySabun(sabun).forEach(v -> relatedInstIds.add(v.getInstId()));
        List<WorkHistoryVo> myProcessedHistory = workHistoryRepository.findByRegSabun(sabun);
        myProcessedHistory.forEach(v -> relatedInstIds.add(v.getInstId()));

        // 본인이 직접 등록한 업무협의(피드백)는 결재선 밖이어도 본인 MY 목록/피드백 탭에 표시한다.
        List<IctybWorkNegotiationVo> myStartedNegotiations =
                ictybWorkNegotiationRepository.findByRegSabunAndNegotiationYn(sabun, "Y");
        myStartedNegotiations.forEach(v -> relatedInstIds.add(v.getInstId()));

        // 결재대기: its_n_sign(현재결재자) 기준. 관련자 목록에도 함께 포함시킨다.
        Set<String> currentApproverInstIds = nSignRepository.findBySabun(sabun).stream()
                .map(NSignVo::getInstId)
                .collect(Collectors.toSet());
        relatedInstIds.addAll(currentApproverInstIds);

        if (relatedInstIds.isEmpty()) {
            return List.of();
        }

        List<String> instIds = new ArrayList<>(relatedInstIds);

        // 피드백: 의견요청(its_work_opinion_req) 중 답변(its_work_opinion, OPN_YN='Y')이 없는 건은 '협의' 상태로 본다.
        Set<String> answeredOpnIds = workOpinionRepository.findByInstrNoIn(instIds).stream()
                .filter(o -> "Y".equals(o.getOpnYn()))
                .map(WorkOpinionVo::getOpnId)
                .collect(Collectors.toSet());
        Set<String> pendingFeedbackInstIds = workOpinionReqRepository.findByInstrNoIn(instIds).stream()
                .filter(req -> !answeredOpnIds.contains(req.getOpnId()))
                .map(WorkOpinionReqVo::getInstrNo)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // 피드백(업무협의): 실제 결재선을 거쳐간 사람(처리이력 등록자 + 현재 결재대기자) 또는
        // 본인이 직접 협의를 등록한 경우에만 표시한다.
        Set<String> approvalChainInstIds = new LinkedHashSet<>(currentApproverInstIds);
        myProcessedHistory.forEach(v -> approvalChainInstIds.add(v.getInstId()));
        myStartedNegotiations.forEach(v -> approvalChainInstIds.add(v.getInstId()));
        ictybWorkNegotiationRepository.findByInstIdInAndNegotiationYn(instIds, "Y").stream()
                .map(IctybWorkNegotiationVo::getInstId)
                .filter(approvalChainInstIds::contains)
                .forEach(pendingFeedbackInstIds::add);

        // 결재이력(승인/반려 처리자 + 시각): 건별로 그룹핑해 목록 각 항목에 함께 내려준다.
        Map<String, List<WorkMyDto.ApprovalHistoryItem>> historyByInstId =
                workHistoryRepository.findByInstIdInOrderByInstIdAscSeqAsc(instIds).stream()
                        .collect(Collectors.groupingBy(
                                WorkHistoryVo::getInstId,
                                LinkedHashMap::new,
                                Collectors.mapping(this::toApprovalHistoryItem, Collectors.toList())));

        // 아직 결재하지 않은 현재결재자도 결재이력 맨 끝에 붙여, 누구한테 결재가 가 있어 멈춰있는지 보이게 한다.
        nSignRepository.findByInstIdIn(instIds).forEach(sign ->
                historyByInstId.computeIfAbsent(sign.getInstId(), k -> new ArrayList<>())
                        .add(toPendingApprovalHistoryItem(sign)));

        List<Object[]> rows = itWorkReportRepository.getWorksMyListByInstIds(instIds);

        return rows.stream().map(row -> {
            String workOrderNo = (String) row[0];
            String approvalStatus = currentApproverInstIds.contains(workOrderNo)
                    ? "결재 대기"
                    : (String) row[4];
            // 최종 완료된 건은 협의가 남아있어도 피드백이 아닌 완료로 본다 (완료 시 피드백 탭에서 자동으로 빠짐).
            String rawStatus = (String) row[5];
            String status = "완료".equals(rawStatus)
                    ? rawStatus
                    : (pendingFeedbackInstIds.contains(workOrderNo) ? "협의" : rawStatus);

            return WorkMyDto.ListItem.builder()
                    .workOrderNo(workOrderNo)
                    .title((String) row[1])
                    .department((String) row[2])
                    .part((String) row[3])
                    .approvalStatus(approvalStatus)
                    .status(status)
                    .dueDt((String) row[6])
                    .approvalHistory(historyByInstId.getOrDefault(workOrderNo, List.of()))
                    .build();
        }).collect(Collectors.toList());
    }

    private WorkMyDto.ApprovalHistoryItem toApprovalHistoryItem(WorkHistoryVo h) {
        return WorkMyDto.ApprovalHistoryItem.builder()
                .sabun(h.getRegSabun())
                .name(h.getRegName())
                .actIdNm(h.getActIdNm())
                .signLabel("R".equals(h.getActSign()) ? "반려" : "승인")
                .regDt(h.getRegDt())
                .reason("R".equals(h.getActSign()) ? h.getRegCntnt() : null)
                .build();
    }

    /** 아직 결재하지 않은, 현재 결재가 대기 중인 사람을 결재이력 맨 끝에 표시하기 위한 항목 (처리 시각 없음). */
    private WorkMyDto.ApprovalHistoryItem toPendingApprovalHistoryItem(NSignVo sign) {
        return WorkMyDto.ApprovalHistoryItem.builder()
                .sabun(sign.getSabun())
                .name(sign.getName())
                .actIdNm(ApprovalFlow.ACT_ID_NM.get(sign.getActId()))
                .signLabel("결재대기")
                .regDt(null)
                .build();
    }

    @Override
    public WorkMyDto.NextCandidatesResponse getNextCandidates(String instId, String sabun) {
        NSignVo current = requireCurrentApprover(instId, sabun);
        ItWorkReportVo report = requireReport(instId);
        String nextActId = ApprovalFlow.next(current.getActId(), report.getWorkType());
        List<WorkMyDto.Candidate> candidates = (nextActId == null || "800".equals(nextActId))
                ? List.of()
                : resolveCandidates(nextActId, report, current);
        return WorkMyDto.NextCandidatesResponse.builder()
                .currentActId(current.getActId())
                .candidates(candidates)
                .build();
    }

    @Override
    @Transactional
    public void approve(String instId, String sabun, String sabunName, String nextSabun, String nextName,
                         String workResult, List<MultipartFile> files) throws IOException {
        NSignVo current = requireCurrentApprover(instId, sabun);
        String currentActId = current.getActId();
        ItWorkReportVo report = requireReport(instId);
        String nextActId = ApprovalFlow.next(currentActId, report.getWorkType());
        if (nextActId == null) {
            throw new IllegalStateException("이미 완료된 건이거나 알 수 없는 진행단계입니다.");
        }
        if ("109".equals(currentActId) && (workResult == null || workResult.isBlank())) {
            throw new IllegalStateException("조치사항을 입력해야 합니다.");
        }

        String now = LocalDateTime.now().format(DT_FMT);

        appendHistory(instId, currentActId, ApprovalFlow.ACT_ID_NM.get(currentActId), "S", sabun, sabunName, now, null);
        nSignRepository.deleteById(new NSignPk(instId, sabun));

        if ("109".equals(currentActId)) {
            workResultRepository.save(WorkResultVo.builder()
                    .instId(instId)
                    .reqId(instId)
                    .result(workResult)
                    .workerId(sabun)
                    .workerName(sabunName)
                    .workerDepNm(resolveDepNm(report.getWorkerDepCd()))
                    .regDt(now)
                    .build());
            saveWorkResultAttachments(instId, now, files);
        }

        if ("800".equals(nextActId)) {
            report.setActId("800");
        } else {
            if (nextSabun == null || nextSabun.isBlank()) {
                throw new IllegalStateException("다음 단계 담당자를 지정해야 합니다.");
            }
            report.setActId(currentActId);
            nSignRepository.save(NSignVo.builder()
                    .instId(instId)
                    .sabun(nextSabun)
                    .actId(nextActId)
                    .name(nextName)
                    .build());
            // 다음 단계가 지시서 접수(107)로 넘어갈 때, 등록 시엔 미정이던 대상 부서(WORKER_DEP_CD)를
            // 방금 선택된 부장의 소속 부서로 확정한다.
            if ("107".equals(nextActId)) {
                String depId = userInfoRepository.findDepIdByEmpno(nextSabun).stream().findFirst()
                        .orElseThrow(() -> new IllegalStateException("선택한 결재자의 소속 부서를 찾을 수 없습니다."));
                report.setWorkerDepCd(depId);
            }
            // 다음 단계가 결과 보고(109)로 넘어갈 때, 지정된 담당자를 작업자로 반영한다.
            // (일반 건은 지시서 배부(108)에서, 자료추출 건은 지시서 접수(107)에서 바로 넘어옴)
            if ("109".equals(nextActId)) {
                report.setWorkerSabun(nextSabun);
                report.setWorkerName(nextName);
            }
        }
        itWorkReportRepository.save(report);
    }

    @Override
    @Transactional
    public void returnToPrevious(String instId, String sabun, String sabunName, String reason) {
        NSignVo current = requireCurrentApprover(instId, sabun);
        String currentActId = current.getActId();
        ItWorkReportVo report = requireReport(instId);
        String targetActId = ApprovalFlow.prev(currentActId, report.getWorkType());
        if (targetActId == null) {
            throw new IllegalStateException("반송할 이전 단계가 없습니다.");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalStateException("반송 사유를 입력해야 합니다.");
        }

        List<WorkHistoryVo> history = workHistoryRepository.findByInstIdOrderBySeqAsc(instId);
        WorkHistoryVo targetHistory = history.stream()
                .filter(h -> targetActId.equals(h.getActId()))
                .reduce((first, second) -> second) // 가장 최근(마지막) 처리자
                .orElseThrow(() -> new IllegalStateException("이전 단계 처리자를 찾을 수 없습니다."));

        String now = LocalDateTime.now().format(DT_FMT);

        appendHistory(instId, currentActId, ApprovalFlow.ACT_ID_NM.get(currentActId), "R", sabun, sabunName, now, reason);
        nSignRepository.deleteById(new NSignPk(instId, sabun));
        nSignRepository.save(NSignVo.builder()
                .instId(instId)
                .sabun(targetHistory.getRegSabun())
                .actId(targetActId)
                .name(targetHistory.getRegName())
                .build());

        report.setActId(ApprovalFlow.revertReportActId(targetActId, report.getWorkType()));
        // 109(결과 보고) 담당자가 반려하면 그 배정 자체를 되돌리는 것이므로 WORKER_SABUN/NAME도 함께 지운다.
        // 지우지 않으면, 이후 상위 결재자(부장/파트장)가 재승인할 때 resolveCandidates()의
        // resolvePartId()가 "이미 배정된 작업자가 있다"고 오판해 이전 담당자의 파트로만
        // 다음 담당자 후보를 좁혀버리는 버그가 있었다(2026-07-08, 자료추출 건에서 재현).
        if ("109".equals(currentActId)) {
            report.setWorkerSabun(null);
            report.setWorkerName(null);
        }
        itWorkReportRepository.save(report);
    }

    @Override
    public WorkMyDto.CreateOptions getCreateOptions() {
        return WorkMyDto.CreateOptions.builder()
                .serviceTypeOptions(WorkOrderCodeOptions.SERVICE_TYPE)
                .workTypeOptions(WorkOrderCodeOptions.WORK_TYPE)
                .workGubunOptions(WorkOrderCodeOptions.WORK_GUBUN)
                .workLevelOptions(WorkOrderCodeOptions.WORK_LEVEL)
                .build();
    }

    /**
     * 대상 부서 후보 3곳(영업/배전/기술)의 DEP_ID 목록.
     * 영배사업처(처 레벨)와 한전 더미 부서는 제외한다 - 등록 시 targetDepCd 선택란이 있던 시절
     * getCreateOptions()의 departmentOptions 필터와 동일한 기준이며, 이제는 지시서 접수(107)
     * 단계에서 부장 후보를 좁히는 데 쓰인다.
     */
    private List<String> businessDepIds() {
        return kdnDepRepository.findAll().stream()
                .filter(d -> !KEPCO_DUMMY_DEP_ID.equals(d.getDepId()))
                .filter(d -> d.getParDepId() != null)
                .map(KdnDepVo::getDepId)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkMyDto.Candidate> getInitialApproverCandidates() {
        return kepcoStaffCandidates();
    }

    @Override
    @Transactional
    public String createWorkOrder(String creatorSabun, String creatorName, String creatorDepCd, String creatorDepNm,
                                   WorkMyDto.CreateRequest request, List<MultipartFile> files) throws IOException {
        if (request.getChangeTitle() == null || request.getChangeTitle().isBlank()) {
            throw new IllegalStateException("제목을 입력해야 합니다.");
        }
        if (request.getInitialApproverSabun() == null || request.getInitialApproverSabun().isBlank()) {
            throw new IllegalStateException("최초 결재자를 지정해야 합니다.");
        }

        String now = LocalDateTime.now().format(DT_FMT);
        String instId = now; // yyyyMMddHHmmss, INST_ID/REQ_ID 컬럼 폭(14)에 맞춘 임시 채번

        String expectedFinishedDt = toExpectedFinishedDt(request.getExpectedFinishedDt());

        ItWorkReportVo report = ItWorkReportVo.builder()
                .instId(instId)
                .reqId(instId)
                .changeTitle(request.getChangeTitle())
                .changeReason(request.getChangeReason())
                .serviceType(request.getServiceType())
                .workType(request.getWorkType())
                .workGubun(request.getWorkGubun())
                .workLevel(request.getWorkLevel())
                .workPeriod(request.getWorkPeriod())
                .expectedFinishedDt(expectedFinishedDt)
                .actId("104")
                // 등록자 자신의 소속(한전일 수도 있음)과, 이 일을 실제로 처리할 KDN 부서(WORKER_DEP_CD)는 별개다.
                // WORKER_DEP_CD는 등록 시점엔 아직 정해지지 않고, 106(한전 파트장 승인) 통과 후
                // 107 단계에서 대상 부서 부장이 선택되는 순간 확정된다.
                .regUserSabun(creatorSabun)
                .regUserName(creatorName)
                .regUserDepCd(creatorDepCd)
                .regUserDepNm(creatorDepNm)
                .regDt(now)
                .workStartDt(now)
                .build();
        itWorkReportRepository.save(report);

        appendHistory(instId, "104", ApprovalFlow.ACT_ID_NM.get("104"), "S", creatorSabun, creatorName, now, null);

        nSignRepository.save(NSignVo.builder()
                .instId(instId)
                .sabun(request.getInitialApproverSabun())
                .actId("106")
                .name(request.getInitialApproverName())
                .build());

        saveAttachments(instId, now, files);

        return instId;
    }

    @Override
    public WorkMyDto.Detail getDetail(String instId, String sabun) {
        ItWorkReportVo report = requireReport(instId);

        List<WorkMyDto.AttachmentItem> attachments = itWorkReportAttachRepository.findByInstId(instId).stream()
                .map(a -> WorkMyDto.AttachmentItem.builder()
                        .seq(a.getSeq())
                        .realFileName(a.getRealFileName())
                        .fileSize(a.getFileSize())
                        .regDt(a.getRegDt())
                        .build())
                .collect(Collectors.toList());

        Optional<NSignVo> currentSign = nSignRepository.findByInstId(instId).stream().findFirst();
        String currentActId = currentSign.map(NSignVo::getActId).orElse(null);
        boolean myTurn = currentSign.map(NSignVo::getSabun).map(s -> s.equals(sabun)).orElse(false);

        List<WorkMyDto.ApprovalHistoryItem> approvalHistory = new ArrayList<>(
                workHistoryRepository.findByInstIdOrderBySeqAsc(instId)
                        .stream()
                        .map(this::toApprovalHistoryItem)
                        .collect(Collectors.toList()));
        // 아직 결재하지 않은 현재결재자도 결재이력 맨 끝에 붙여, 누구한테 결재가 가 있어 멈춰있는지 보이게 한다.
        currentSign.ifPresent(sign -> approvalHistory.add(toPendingApprovalHistoryItem(sign)));

        List<WorkMyDto.AttachmentItem> workResultAttachments = workResultAttachRepository.findByInstId(instId).stream()
                .map(a -> WorkMyDto.AttachmentItem.builder()
                        .seq(a.getSeq())
                        .realFileName(a.getRealFileName())
                        .fileSize(a.getFileSize())
                        .regDt(a.getRegDt())
                        .build())
                .collect(Collectors.toList());

        WorkMyDto.WorkResultItem workResultItem = workResultRepository.findByInstId(instId).stream().findFirst()
                .map(r -> WorkMyDto.WorkResultItem.builder()
                        .result(r.getResult())
                        .workerName(r.getWorkerName())
                        .regDt(r.getRegDt())
                        .attachments(workResultAttachments)
                        .build())
                .orElse(null);

        return WorkMyDto.Detail.builder()
                .workOrderNo(instId)
                .changeTitle(report.getChangeTitle())
                .changeReason(report.getChangeReason())
                .serviceTypeLabel(WorkOrderCodeOptions.labelOf(WorkOrderCodeOptions.SERVICE_TYPE, report.getServiceType()))
                .workTypeLabel(WorkOrderCodeOptions.labelOf(WorkOrderCodeOptions.WORK_TYPE, report.getWorkType()))
                .workGubunLabel(WorkOrderCodeOptions.labelOf(WorkOrderCodeOptions.WORK_GUBUN, report.getWorkGubun()))
                .workLevel(report.getWorkLevel())
                .workPeriod(report.getWorkPeriod())
                .expectedFinishedDt(toDisplayDate(report.getExpectedFinishedDt()))
                .targetDepNm(resolveDepNm(report.getWorkerDepCd()))
                .attachments(attachments)
                .currentActId(currentActId)
                .myTurn(myTurn)
                .workResult(workResultItem)
                .approvalHistory(approvalHistory)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkMyDto.DownloadFile downloadAttach(String instId, String seq) throws MalformedURLException {
        ItWorkReportAttachVo attach = itWorkReportAttachRepository.findByInstId(instId).stream()
                .filter(a -> seq.equals(a.getSeq()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("첨부파일을 찾을 수 없습니다."));

        Path filePath = Paths.get(attach.getFileLocation());
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new IllegalStateException("파일을 읽을 수 없습니다.");
        }
        return WorkMyDto.DownloadFile.builder()
                .resource(resource)
                .realFileName(attach.getRealFileName())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkMyDto.DownloadFile downloadWorkResultAttach(String instId, String seq) throws MalformedURLException {
        WorkResultAttachVo attach = workResultAttachRepository.findByInstId(instId).stream()
                .filter(a -> seq.equals(a.getSeq()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("첨부파일을 찾을 수 없습니다."));

        Path filePath = Paths.get(attach.getFileLocation());
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new IllegalStateException("파일을 읽을 수 없습니다.");
        }
        return WorkMyDto.DownloadFile.builder()
                .resource(resource)
                .realFileName(attach.getRealFileName())
                .build();
    }

    private void saveWorkResultAttachments(String instId, String now, List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) return;

        String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path uploadPath = Paths.get(uploadDir, dateDir, instId, "work_result");
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

            workResultAttachRepository.save(WorkResultAttachVo.builder()
                    .instId(instId)
                    .seq(String.format("%03d", seq++))
                    .reqId(instId)
                    .realFileName(realFileName)
                    .fileName(savedFileName)
                    .fileLocation(targetPath.toString())
                    .fileSize(String.valueOf(file.getSize()))
                    .regDt(now)
                    .build());
        }
    }

    /** yyyyMMddHHmmss(23:59:59 고정) 형식을 화면 표시용 yyyy-MM-dd로 변환한다. */
    private String toDisplayDate(String expectedFinishedDt) {
        if (expectedFinishedDt == null || expectedFinishedDt.length() < 8) {
            return null;
        }
        return expectedFinishedDt.substring(0, 4) + "-" + expectedFinishedDt.substring(4, 6) + "-"
                + expectedFinishedDt.substring(6, 8);
    }

    private String resolveDepNm(String depCd) {
        if (depCd == null || depCd.isBlank()) {
            return null;
        }
        return kdnDepRepository.findByDepId(depCd).map(KdnDepVo::getDepTitle).orElse(depCd);
    }

    /** 사번의 소속 부서명 (107 결재자 후보 목록에서 부서별로 구분해 보여주기 위함) */
    private String deptNmOf(String empno) {
        return userInfoRepository.findDepIdByEmpno(empno).stream().findFirst()
                .map(this::resolveDepNm)
                .orElse("소속 부서 미상");
    }

    private void saveAttachments(String instId, String now, List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) return;

        String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path uploadPath = Paths.get(uploadDir, dateDir, instId);
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

            itWorkReportAttachRepository.save(ItWorkReportAttachVo.builder()
                    .instId(instId)
                    .seq(String.format("%03d", seq++))
                    .realFileName(realFileName)
                    .fileName(savedFileName)
                    .fileLocation(targetPath.toString())
                    .fileSize(String.valueOf(file.getSize()))
                    .regDt(now)
                    .build());
        }
    }

    /** yyyy-MM-dd 형식 입력을 EXPECTED_FINISHED_DT 컬럼 규격(yyyyMMddHHmmss, 해당일 23:59:59)으로 변환한다. */
    private String toExpectedFinishedDt(String dateInput) {
        if (dateInput == null || dateInput.isBlank()) {
            return null;
        }
        String digitsOnly = dateInput.replaceAll("[^0-9]", "");
        if (digitsOnly.length() != 8) {
            throw new IllegalStateException("완료예정일 형식이 올바르지 않습니다.");
        }
        return digitsOnly + "235959";
    }

    private NSignVo requireCurrentApprover(String instId, String sabun) {
        return nSignRepository.findById(new NSignPk(instId, sabun))
                .orElseThrow(() -> new IllegalStateException("현재 결재 대기 중인 단계가 아닙니다."));
    }

    private ItWorkReportVo requireReport(String instId) {
        return itWorkReportRepository.findByInstIdIn(List.of(instId)).stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("작업지시서를 찾을 수 없습니다."));
    }

    private void appendHistory(String instId, String actId, String actIdNm, String actSign,
                                String regSabun, String regName, String regDt, String regCntnt) {
        List<WorkHistoryVo> existing = workHistoryRepository.findByInstIdOrderBySeqAsc(instId);
        WorkHistoryVo history = new WorkHistoryVo();
        history.setInstId(instId);
        history.setSeq(String.valueOf(existing.size() + 1));
        history.setActId(actId);
        history.setActIdNm(actIdNm);
        history.setActSign(actSign);
        history.setRegSabun(regSabun);
        history.setRegName(regName);
        history.setRegDt(regDt);
        history.setRegCntnt(regCntnt);
        workHistoryRepository.save(history);
    }

    private List<WorkMyDto.Candidate> resolveCandidates(String nextActId, ItWorkReportVo report, NSignVo current) {
        // 이 일을 처리할 KDN 부서는 REG_USER_DEP_CD(등록자 자신의 소속 - 한전일 수도 있음)가 아니라
        // WORKER_DEP_CD(작업부서, 등록 시 지정)를 기준으로 판단한다.
        String depId = report.getWorkerDepCd();
        String partId = resolvePartId(report, current);

        return switch (nextActId) {
            case "106" -> kepcoStaffCandidates();
            // 107 시점엔 아직 대상 부서(WORKER_DEP_CD)가 정해지지 않았으므로 영업/배전/기술
            // 3개 부서 부장 전체를 후보로 보여준다. 여기서 선택된 부장의 소속 부서가 곧
            // 이 지시서의 대상 부서로 확정된다(approve()에서 WORKER_DEP_CD를 세팅).
            // 후보가 여러 부서에 걸쳐 있으므로 roleNm에 소속 부서명을 같이 표시해 구분한다.
            case "107" -> userInfoRepository.findDeptHeadsByDepIds(businessDepIds()).stream()
                    .map(u -> toCandidate(u.getEmpno(), u.getUserNm(), "KDN 부장 (" + deptNmOf(u.getEmpno()) + ")"))
                    .collect(Collectors.toList());
            // 108 시점엔 아직 작업자(partId)가 정해지지 않아 부서 전체 파트장 중에서 고른다.
            // 후보가 여러 파트에 걸쳐 있으므로 roleNm에 담당 파트명을 같이 표시해 구분한다.
            case "108" -> (partId != null
                    ? userInfoRepository.findPartLeadersByPartId(partId)
                    : userInfoRepository.findPartLeadersByDepId(depId)).stream()
                    .map(u -> toCandidate(u.getEmpno(), u.getUserNm(), "KDN 파트장 (" + u.getPartNm() + ")"))
                    .collect(Collectors.toList());
            case "109" -> (partId != null
                    ? userInfoRepository.findRegularMembersByPartId(partId)
                    : userInfoRepository.findRegularMembersByDepId(depId)).stream()
                    .map(u -> toCandidate(u.getEmpno(), u.getUserNm(), "KDN 대리"))
                    .collect(Collectors.toList());
            case "110" -> (partId != null
                    ? userInfoRepository.findPartLeadersByPartId(partId)
                    : userInfoRepository.findPartLeadersByDepId(depId)).stream()
                    .map(u -> toCandidate(u.getEmpno(), u.getUserNm(), "KDN 파트장"))
                    .collect(Collectors.toList());
            case "111" -> userInfoRepository.findDeptHeadsByDepId(depId).stream()
                    .map(u -> toCandidate(u.getEmpno(), u.getUserNm(), "KDN 부장"))
                    .collect(Collectors.toList());
            case "114" -> kepcoStaffCandidates();
            default -> List.of();
        };
    }

    private WorkMyDto.Candidate toCandidate(String sabun, String name, String roleNm) {
        return WorkMyDto.Candidate.builder().sabun(sabun).name(name).roleNm(roleNm).build();
    }

    /**
     * 한전 담당자(결재자) 후보 목록. 실제 한전 인사정보 테이블엔 직책(파트장/직원) 구분 필드가 없어
     * 역할 구분 없이 전원을 후보로 보여주지만, 처장(JIKGUB_HAN이 채워진 인원, 예: 이명종)은
     * 부서 위 상위 직급이라 결재자 후보에서 제외한다.
     */
    private List<WorkMyDto.Candidate> kepcoStaffCandidates() {
        return kepcoUserRepository.findAll().stream()
                .filter(u -> u.getJikgubHan() == null || u.getJikgubHan().isBlank())
                .map(u -> toCandidate(u.getSabun(), u.getName(), "한전 담당자"))
                .collect(Collectors.toList());
    }

    /**
     * 결과 보고(109) 담당자를 좁혀 찾기 위한 소속 파트 해석.
     * 우선 작업지시서에 이미 지정된 작업자(WORKER_SABUN) 기준으로 찾고,
     * 아직 없다면(=지시서 배부(108) 파트장이 막 다음 담당자를 고르는 시점) 그 파트장 본인의 파트로 좁힌다
     * — 그렇지 않으면 부서 전체 대리가 다 후보로 뜬다(실제 버그로 보고됨, 2026-07-07).
     * 그 외 단계에서는 부서 전체(depId) 기준 조회로 폴백한다.
     */
    private String resolvePartId(ItWorkReportVo report, NSignVo current) {
        String workerSabun = report.getWorkerSabun();
        if (workerSabun != null && !workerSabun.isBlank()) {
            return partIdOf(workerSabun);
        }
        if ("108".equals(current.getActId())) {
            return partIdOf(current.getSabun());
        }
        return null;
    }

    private String partIdOf(String empno) {
        return userInfoRepository.findByEmpnoAndUseYn(empno, "Y").stream()
                .findFirst()
                .map(UserInfoVo::getPartId)
                .orElse(null);
    }
}
