package kepco.prorject.ictyb.back.ictyb_back.work_my.service;

import kepco.prorject.ictyb.back.ictyb_back.common.util.DeptHeadUtil;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.IctybWorkNegotiationVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.ItWorkReportAttachVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.ItWorkReportDelVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.ItWorkReportVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.KepcoUserVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.NSignVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.UserInfoVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.UserInfoSecretVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.WorkHistoryVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.WorkOpinionReqVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.WorkOpinionVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.WorkResultAttachVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.WorkResultVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.CodeVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.KdnDepVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.SystemInfoVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.RealWorkReportVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.RealWorkReportAttachVo;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.NSignPk;
import kepco.prorject.ictyb.back.ictyb_back.common.voArea.cm.pk.UserInfoSecretPk;
import kepco.prorject.ictyb.back.ictyb_back.common.util.KepcoSabunUtil;
import kepco.prorject.ictyb.back.ictyb_back.jwt.repository.KdnDepRepository;
import kepco.prorject.ictyb.back.ictyb_back.report_total.repository.SystemInfoRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.model.WorkMyDto;
import kepco.prorject.ictyb.back.ictyb_back.work_opinion.service.WorkOpinionService;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.CodeRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.IctybWorkNegotiationRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.ItReceiverRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.ItWorkReportAttachRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.ItWorkReportDelRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.ItWorkReportRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.NSignRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.RealWorkReportRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.RealWorkReportAttachRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.ReceiveRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.KepcoUserRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.UserInfoRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.UserInfoSecretRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.WorkHistoryRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.WorkOpinionReqRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.WorkOpinionRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.WorkResultAttachRepository;
import kepco.prorject.ictyb.back.ictyb_back.work_my.repository.WorkResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkMyServiceImpl implements WorkMyService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /** its_user_info_secret.DOC_TYPE 코드값 (2026-07-27 확정): 요청서=1, 지시서=2, 결과보고=3. */
    private static final String DOC_TYPE_REQUEST = "1";
    private static final String DOC_TYPE_WORK_ORDER = "2";
    private static final String DOC_TYPE_WORK_RESULT = "3";

    /**
     * 등록 다이얼로그를 여는 시점에 미리 채번해 화면에 보여준 뒤, 실제 등록 시 그대로 사용하기 위한
     * 예약 저장소. 등록을 완료하지 않고 다이얼로그를 닫아버린 예약은 TTL이 지나면 정리한다.
     */
    private static final Map<String, Instant> RESERVED_NOS = new ConcurrentHashMap<>();
    private static final Duration RESERVATION_TTL = Duration.ofMinutes(30);
    private final Object reserveLock = new Object();

    private final ItWorkReportRepository itWorkReportRepository;
    private final ReceiveRepository receiveRepository;
    private final ItReceiverRepository itReceiverRepository;
    private final NSignRepository nSignRepository;
    private final WorkHistoryRepository workHistoryRepository;
    private final WorkOpinionRepository workOpinionRepository;
    private final WorkOpinionReqRepository workOpinionReqRepository;
    private final IctybWorkNegotiationRepository ictybWorkNegotiationRepository;
    private final WorkOpinionService workOpinionService;
    private final KepcoUserRepository kepcoUserRepository;
    private final UserInfoRepository userInfoRepository;
    private final KdnDepRepository kdnDepRepository;
    private final ItWorkReportAttachRepository itWorkReportAttachRepository;
    private final ItWorkReportDelRepository itWorkReportDelRepository;
    private final WorkResultRepository workResultRepository;
    private final WorkResultAttachRepository workResultAttachRepository;
    private final UserInfoSecretRepository userInfoSecretRepository;
    private final SystemInfoRepository systemInfoRepository;
    private final CodeRepository codeRepository;
    private final RealWorkReportRepository realWorkReportRepository;
    private final RealWorkReportAttachRepository realWorkReportAttachRepository;

    @Value("${file.work-my-upload-dir:./uploads/work_my}")
    private String uploadDir;

    /** its_kepco_user 로그인 지원을 위해 its_kdn_dep에 함께 등록해둔 한전 부서코드 (실제 KDN 작업부서 아님). */
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
        Map<String, List<WorkHistoryVo>> rawHistoryByInstId =
                workHistoryRepository.findByInstIdInOrderByInstIdAscSeqAsc(instIds).stream()
                        .collect(Collectors.groupingBy(WorkHistoryVo::getInstId, LinkedHashMap::new, Collectors.toList()));
        Map<String, List<WorkMyDto.ApprovalHistoryItem>> historyByInstId = rawHistoryByInstId.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream().map(this::toApprovalHistoryItem).collect(Collectors.toList()),
                        (a, b) -> a,
                        LinkedHashMap::new));

        // 요청서에서 전환된 지시서(REQ_ID가 자기 자신이 아니라 원본 요청서를 가리킴)는 그 요청서의
        // 100/102/103 결재이력도 지시서 자체 이력 앞에 이어붙여, 요청서 단계부터 전체 흐름이 보이게 한다.
        Map<String, String> reqIdByInstId = itWorkReportRepository.findByInstIdIn(instIds).stream()
                .filter(r -> r.getReqId() != null && !r.getReqId().isBlank() && !r.getReqId().equals(r.getInstId()))
                .collect(Collectors.toMap(ItWorkReportVo::getInstId, ItWorkReportVo::getReqId));
        if (!reqIdByInstId.isEmpty()) {
            Map<String, List<WorkMyDto.ApprovalHistoryItem>> requestHistoryByReqId =
                    workHistoryRepository.findByInstIdInOrderByInstIdAscSeqAsc(new ArrayList<>(reqIdByInstId.values()))
                            .stream()
                            .collect(Collectors.groupingBy(WorkHistoryVo::getInstId, LinkedHashMap::new,
                                    Collectors.mapping(this::toApprovalHistoryItem, Collectors.toList())));
            reqIdByInstId.forEach((workOrderInstId, reqId) -> {
                List<WorkMyDto.ApprovalHistoryItem> requestHistory = requestHistoryByReqId.get(reqId);
                if (requestHistory != null && !requestHistory.isEmpty()) {
                    List<WorkMyDto.ApprovalHistoryItem> merged = new ArrayList<>(requestHistory);
                    merged.addAll(historyByInstId.getOrDefault(workOrderInstId, List.of()));
                    historyByInstId.put(workOrderInstId, merged);
                }
            });
        }

        // 반송 여부: 내가 현재결재자이고, 그 직전 마지막 처리 이력이 반려(반송)인 건.
        Set<String> returnedToMeInstIds = currentApproverInstIds.stream()
                .filter(instId -> {
                    List<WorkHistoryVo> hist = rawHistoryByInstId.get(instId);
                    return hist != null && !hist.isEmpty() && "R".equals(hist.get(hist.size() - 1).getActSign());
                })
                .collect(Collectors.toSet());

        // 아직 결재하지 않은 현재결재자도 결재이력 맨 끝에 붙여, 누구한테 결재가 가 있어 멈춰있는지 보이게 한다.
        nSignRepository.findByInstIdIn(instIds).forEach(sign ->
                historyByInstId.computeIfAbsent(sign.getInstId(), k -> new ArrayList<>())
                        .add(toPendingApprovalHistoryItem(sign)));

        List<Object[]> rows = itWorkReportRepository.getWorksMyListByInstIds(instIds);

        // 협의(피드백) 탭 new! 배지 판단: 그 탭에 뜨는 건들 중 본인이 아직 읽지 않은 활동(신규 협의/댓글)이
        // 있는 건만 추린다.
        Set<String> unreadInstrNos = workOpinionService.getUnreadInstrNos(
                new ArrayList<>(pendingFeedbackInstIds), sabun);
        // 협의 탭 정렬: 그 탭에 뜨는 건들을 "최종 협의 활동 시각"(신규 협의 등록/댓글) 기준 최신순으로 보여주기 위함.
        Map<String, LocalDateTime> lastDiscussionActivityByInstId = workOpinionService.getLastActivityByInstrNo(
                new ArrayList<>(pendingFeedbackInstIds));

        List<WorkMyDto.ListItem> items = rows.stream().map(row -> {
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
                    .returnedToMe(returnedToMeInstIds.contains(workOrderNo))
                    .hasUnreadDiscussion("협의".equals(status) && unreadInstrNos.contains(workOrderNo))
                    .build();
        }).collect(Collectors.toList());

        // 협의 상태인 건들끼리만 최종 협의 활동 시각 내림차순으로 재정렬하고, 그 외 건들은 서로의 기존
        // 순서(등록일 최신순, 완료 건은 맨 아래)를 그대로 유지한다. "둘 다 협의일 때만 비교, 아니면 동일 취급"하는 Comparator는
        // 추이성(transitivity)을 위반해 정렬 결과가 무작위로 깨지므로(2026-07-13 확인), 협의 건만 따로
        // 뽑아 올바른 Comparator로 정렬한 뒤 원래 있던 자리에 그대로 되꽂아 넣는 방식으로 처리한다.
        List<Integer> discussionIndices = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            if ("협의".equals(items.get(i).getStatus())) discussionIndices.add(i);
        }
        // 주의: nullsLast(...).reversed()로 쓰면 .reversed()가 null 배치까지 뒤집어 null(협의 스레드가
        // 아직 없는 건)이 오히려 맨 앞으로 온다 - nullsLast(reverseOrder())로 내림차순+null-후순위를
        // 한 번에 표현해야 한다(2026-07-13, DUE0005처럼 활동 시각이 없는 건이 최상단에 뜨는 버그로 발견).
        List<WorkMyDto.ListItem> sortedDiscussionItems = discussionIndices.stream()
                .map(items::get)
                .sorted(Comparator.comparing(
                        (WorkMyDto.ListItem item) -> lastDiscussionActivityByInstId.get(item.getWorkOrderNo()),
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
        for (int k = 0; k < discussionIndices.size(); k++) {
            items.set(discussionIndices.get(k), sortedDiscussionItems.get(k));
        }

        return items;
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
        WorkHistoryVo kepcoDirectReturn = findLastKepcoReturnToWorker(instId, current.getActId()).orElse(null);
        List<WorkMyDto.Candidate> candidates;
        if (kepcoDirectReturn != null) {
            // 한전이 실제 작업자에게 직접 반송한 건의 재작업 재제출이므로, KDN 내부 재검토(110/111)를
            // 건너뛰고 반송했던 그 한전 담당자에게만 결재 요청할 수 있도록 후보를 그 한 명으로 고정한다.
            candidates = List.of(toCandidate(kepcoDirectReturn.getRegSabun(), kepcoDirectReturn.getRegName(),
                    "한전 담당자 (반송자에게 즉시 재상신)"));
        } else {
            String nextActId = ApprovalFlow.next(current.getActId(), isDataExtraction(report));
            candidates = (nextActId == null || "800".equals(nextActId))
                    ? List.of()
                    : resolveCandidates(nextActId, report, current);
        }
        return WorkMyDto.NextCandidatesResponse.builder()
                .currentActId(current.getActId())
                .candidates(candidates)
                .build();
    }

    @Override
    @Transactional
    public void approve(String instId, String sabun, String sabunName, String nextSabun, String nextName,
                         String workResult, String isSecret, String oppbClYn, String userSecretContent,
                         String attachExpireDate, List<MultipartFile> files) throws IOException {
        NSignVo current = requireCurrentApprover(instId, sabun);
        String currentActId = current.getActId();
        ItWorkReportVo report = requireReport(instId);
        String nextActId = ApprovalFlow.next(currentActId, isDataExtraction(report));
        if (nextActId == null) {
            throw new IllegalStateException("이미 완료된 건이거나 알 수 없는 진행단계입니다.");
        }
        if ("109".equals(currentActId) && (workResult == null || workResult.isBlank())) {
            throw new IllegalStateException("조치사항을 입력해야 합니다.");
        }

        // 한전(114)이 실제 작업자(109)에게 직접 반송한 건을 재작업해 재제출하는 경우,
        // KDN 내부 재검토(110/111)를 생략하고 반송했던 그 한전 담당자에게 바로 결재 요청한다.
        WorkHistoryVo kepcoDirectReturn = findLastKepcoReturnToWorker(instId, currentActId).orElse(null);
        if (kepcoDirectReturn != null) {
            nextActId = "114";
            nextSabun = kepcoDirectReturn.getRegSabun();
            nextName = kepcoDirectReturn.getRegName();
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

            userInfoSecretRepository.save(UserInfoSecretVo.builder()
                    .instId(instId)
                    .docType(DOC_TYPE_WORK_RESULT)
                    .reqId(instId)
                    .isSecret(isSecret)
                    .oppbClYn(secretSubField(isSecret, oppbClYn))
                    .userSecretContent(secretSubField(isSecret, userSecretContent))
                    .attachExpireDate(toAttachExpireDate(isSecret, attachExpireDate))
                    .build());
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
    public WorkMyDto.ReturnTargetsResponse getReturnTargets(String instId, String sabun) {
        NSignVo current = requireCurrentApprover(instId, sabun);
        ItWorkReportVo report = requireReport(instId);
        List<String> priorSteps = ApprovalFlow.priorSteps(current.getActId(), isDataExtraction(report));

        List<WorkHistoryVo> history = workHistoryRepository.findByInstIdOrderBySeqAsc(instId);
        // actId별 "가장 최근(마지막) 처리자" 이력만 남긴다 (반려 후 재승인 등으로 같은 단계가 여러 번 나타날 수 있음).
        Map<String, WorkHistoryVo> lastByActId = new LinkedHashMap<>();
        history.forEach(h -> lastByActId.put(h.getActId(), h));

        List<WorkMyDto.ReturnTarget> targets = priorSteps.stream()
                .map(actId -> {
                    WorkHistoryVo h = lastByActId.get(actId);
                    return WorkMyDto.ReturnTarget.builder()
                            .actId(actId)
                            .actIdNm(ApprovalFlow.ACT_ID_NM.get(actId))
                            .sabun(h != null ? h.getRegSabun() : null)
                            .name(h != null ? h.getRegName() : null)
                            .build();
                })
                .collect(Collectors.toList());

        return WorkMyDto.ReturnTargetsResponse.builder()
                .currentActId(current.getActId())
                .targets(targets)
                .build();
    }

    @Override
    @Transactional
    public void returnToPrevious(String instId, String sabun, String sabunName, String reason, String targetActId) {
        NSignVo current = requireCurrentApprover(instId, sabun);
        String currentActId = current.getActId();
        ItWorkReportVo report = requireReport(instId);
        List<String> priorSteps = ApprovalFlow.priorSteps(currentActId, isDataExtraction(report));
        if (priorSteps.isEmpty()) {
            throw new IllegalStateException("반송할 이전 단계가 없습니다.");
        }
        String resolvedTargetActId = (targetActId == null || targetActId.isBlank())
                ? priorSteps.get(0) // 미지정 시 직전 단계로 반송 (기존 동작과 동일)
                : targetActId;
        if (!priorSteps.contains(resolvedTargetActId)) {
            throw new IllegalStateException("반송할 수 없는 단계입니다.");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalStateException("반송 사유를 입력해야 합니다.");
        }

        List<WorkHistoryVo> history = workHistoryRepository.findByInstIdOrderBySeqAsc(instId);
        WorkHistoryVo targetHistory = history.stream()
                .filter(h -> resolvedTargetActId.equals(h.getActId()))
                .reduce((first, second) -> second) // 가장 최근(마지막) 처리자
                .orElseThrow(() -> new IllegalStateException("이전 단계 처리자를 찾을 수 없습니다."));

        String now = LocalDateTime.now().format(DT_FMT);

        appendHistory(instId, currentActId, ApprovalFlow.ACT_ID_NM.get(currentActId), "R", sabun, sabunName, now, reason);
        nSignRepository.deleteById(new NSignPk(instId, sabun));
        nSignRepository.save(NSignVo.builder()
                .instId(instId)
                .sabun(targetHistory.getRegSabun())
                .actId(resolvedTargetActId)
                .name(targetHistory.getRegName())
                .build());

        report.setActId(ApprovalFlow.revertReportActId(resolvedTargetActId, isDataExtraction(report)));
        // 반송 대상 단계가 109(결과 보고) 배정 시점이거나 그 이전이면, 그 작업자 배정 자체를 되돌리는
        // 것이므로 WORKER_SABUN/NAME도 함께 지운다. 지우지 않으면, 이후 상위 결재자(부장/파트장)가
        // 재승인할 때 resolveCandidates()의 resolvePartId()가 "이미 배정된 작업자가 있다"고 오판해
        // 이전 담당자의 파트로만 다음 담당자 후보를 좁혀버리는 버그가 있었다
        // (2026-07-08, 자료추출 건에서 재현. 원래는 109 자신이 반려할 때만 처리했으나,
        // 임의 단계로 반송할 수 있게 되며 109 이전 단계로 보내는 모든 경우로 일반화함).
        if (ApprovalFlow.isAtOrBeforeWorkerAssignment(resolvedTargetActId, isDataExtraction(report))) {
            report.setWorkerSabun(null);
            report.setWorkerName(null);
            // 109(결과 보고) 자체를 다시 하게 되는 반송이므로, 이전에 제출됐던 조치사항(작업결과)도
            // 함께 지운다. 지우지 않으면 getDetail()이 여전히 옛 조치사항을 내려주고, 프론트는
            // "이미 제출됨"으로 보고 재작성 폼(WorkResultSection)을 열어주지 않아 작업자가 재작업 결과를
            // 다시 입력할 수 없는 버그가 있었다(2026-07-11).
            clearWorkResult(instId);
        }
        itWorkReportRepository.save(report);
    }

    /** 109(결과 보고) 재작업을 위해, 그 전에 제출됐던 조치사항(작업결과)과 첨부파일을 지운다. */
    private void clearWorkResult(String instId) {
        List<WorkResultAttachVo> attachments = workResultAttachRepository.findByInstId(instId);
        for (WorkResultAttachVo attach : attachments) {
            try {
                Files.deleteIfExists(Paths.get(attach.getFileLocation()));
            } catch (IOException e) {
                log.warn("작업결과 첨부파일 삭제 실패: instId={}, seq={}", instId, attach.getSeq(), e);
            }
        }
        workResultAttachRepository.deleteAll(attachments);
        workResultRepository.deleteAll(workResultRepository.findByInstId(instId));
    }

    @Override
    public WorkMyDto.CreateOptions getCreateOptions() {
        return WorkMyDto.CreateOptions.builder()
                .serviceTypeOptions(buildCodeOptions(WorkOrderCodeOptions.SERVICE_TYPE_GUBUN))
                .workTypeOptions(buildCodeOptions(WorkOrderCodeOptions.WORK_TYPE_GUBUN))
                .workGubunOptions(buildCodeOptions(WorkOrderCodeOptions.WORK_GUBUN_GUBUN))
                .workLevelOptions(WorkOrderCodeOptions.WORK_LEVEL)
                .drsImptOptions(WorkOrderCodeOptions.DRS_IMPT_YN)
                .unitSystemOptions(buildUnitSystemOptions())
                .build();
    }

    /** its_code(GUBUN=gubun) 행들을 VIEW_ORDER(없으면 CODE) 순으로 정렬한 코드/라벨 목록으로 변환한다. */
    private List<WorkMyDto.CodeOption> buildCodeOptions(String gubun) {
        return codeRepository.findByIdGubun(gubun).stream()
                .sorted(Comparator.comparing(CodeVo::getViewOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(c -> c.getId().getCode()))
                .map(c -> WorkMyDto.CodeOption.of(c.getId().getCode(), c.getCodeName()))
                .collect(Collectors.toList());
    }

    /** its_code(GUBUN=gubun, CODE=code)의 CODE_NAME을 조회한다. 매칭되는 코드가 없으면 원본 코드를 그대로 반환한다. */
    private String codeNameOf(String gubun, String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return codeRepository.findByIdGubunAndIdCode(gubun, code)
                .map(CodeVo::getCodeName)
                .orElse(code);
    }

    /**
     * 서비스유형 또는 작업유형 중 하나라도 "자료추출"이면 자료추출 건으로 판별한다
     * (2026-07-20, 사용자 확인: 서비스유형만 자료추출이어도 108/111을 생략해야 함).
     */
    private boolean isDataExtraction(ItWorkReportVo report) {
        return isDataExtractionCode(WorkOrderCodeOptions.SERVICE_TYPE_GUBUN, report.getServiceType())
                || isDataExtractionCode(WorkOrderCodeOptions.WORK_TYPE_GUBUN, report.getWorkType());
    }

    private boolean isDataExtractionCode(String gubun, String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        return codeRepository.findByIdGubunAndIdCode(gubun, code)
                .map(c -> WorkOrderCodeOptions.DATA_EXTRACTION_CODE_NAME.equals(c.getCodeName()))
                .orElse(false);
    }

    /**
     * its_system_info 전체 행을 단위시스템 선택지로 변환한다. 업무분야는 DOMAIN_NAME-SUBDOMAIN_NAME을
     * 미리 조립해두고, DRS영향여부는 its_system_info.DRS_IMPT_YN(Y=이 시스템은 DRS 영향 없음)을
     * its_it_work_report.DRS_IMPT_YN 의미(Y=영향 있음)로 뒤집어서 내려준다 - 그래야 등록/상세 화면 모두
     * WorkOrderCodeOptions.DRS_IMPT_YN 라벨("영향 있음"/"영향 없음")과 일관되게 표시된다.
     */
    private List<WorkMyDto.UnitSystemOption> buildUnitSystemOptions() {
        return systemInfoRepository.findAll().stream()
                .map(s -> WorkMyDto.UnitSystemOption.builder()
                        .code(s.getSystemCd())
                        .label(s.getCmsName())
                        .businessField(s.getDomainName() + "-" + s.getSubdomainName())
                        .drsImptYn("Y".equals(s.getDrsImptYn()) ? "N" : "Y")
                        .build())
                .collect(Collectors.toList());
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
        return salesDistributionSystemCandidates();
    }

    @Override
    public List<WorkMyDto.Candidate> getRequestInitialApproverCandidates(String requesterDepCd) {
        return kepcoStaffCandidatesBySosokCd(requesterDepCd);
    }

    @Override
    public String reserveWorkOrderNo() {
        synchronized (reserveLock) {
            cleanupExpiredReservations();
            String no = generateUniqueInstId();
            RESERVED_NOS.put(no, Instant.now());
            return no;
        }
    }

    /**
     * RESERVED_NOS/DB 어디에도 없는 yyyyMMddHHmmss 번호를 찾을 때까지 초 단위로 밀어가며 채번한다.
     * 지시서(its_it_work_report)와 요청서(its_real_work_report)가 이 채번 하나를 함께 쓰므로
     * 두 테이블 모두에서 중복이 없는지 확인한다 (서로 다른 테이블이라 PK 충돌은 없지만, 번호
     * 예약 시점에 두 다이얼로그가 같은 번호를 보여주는 걸 막기 위함).
     */
    private String generateUniqueInstId() {
        LocalDateTime candidate = LocalDateTime.now();
        String candidateNo;
        do {
            candidateNo = candidate.format(DT_FMT);
            candidate = candidate.plusSeconds(1);
        } while (RESERVED_NOS.containsKey(candidateNo) || itWorkReportRepository.existsByInstId(candidateNo)
                || realWorkReportRepository.existsById(candidateNo));
        return candidateNo;
    }

    private void cleanupExpiredReservations() {
        Instant threshold = Instant.now().minus(RESERVATION_TTL);
        RESERVED_NOS.entrySet().removeIf(e -> e.getValue().isBefore(threshold));
    }

    /**
     * 업무지시서 등록. request.sourceRequestNo가 채워져 있으면 작업 요청서(RealWorkReportVo) 기반
     * 작성으로 취급한다 - 이 경우 지시서 생성 자체가 그 요청서의 103(요청서 접수) 완료 처리를 겸한다
     * (2026-07-21, 사용자 확인: 103에 별도 승인/반려 버튼을 두지 않고, "지시서 작성"이라는 실제 행위로
     * 103을 완료한다). 새 지시서의 REQ_ID는 원래 설계대로 그 요청서의 INST_ID를 가리키게 된다.
     */
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

        String sourceRequestNo = request.getSourceRequestNo();
        boolean fromRequest = sourceRequestNo != null && !sourceRequestNo.isBlank();
        if (fromRequest) {
            NSignVo requestSign = requireCurrentApprover(sourceRequestNo, creatorSabun);
            if (!"103".equals(requestSign.getActId())) {
                throw new IllegalStateException("요청서 접수 담당자만 이 요청서를 기반으로 지시서를 작성할 수 있습니다.");
            }
            // fromRequest=true일 땐 위 requireCurrentApprover가 이미 "이 요청서의 103 담당자인지"를
            // 검증한다 - 그 103 담당자 자체가 salesDistributionSystemCandidates()(영업배전시스템실)에서
            // 지정된 사람이므로 아래 소속 검사를 또 할 필요가 없다.
        } else {
            // 요청서를 거치지 않고 지시서를 바로 등록하는 경로(fromRequest=false)는 위와 달리 아무 검증도
            // 없었다 - 104(지시서 작성)는 항상 한전(영업배전시스템실) 담당자가 하는 일이므로(2026-07-23,
            // 사용자 확인) 여기서 직접 소속을 확인한다.
            if (!isSalesDistributionSystem(creatorSabun)) {
                throw new IllegalStateException("영업배전시스템실 소속 한전 담당자만 작업지시서를 등록할 수 있습니다.");
            }
        }

        String now = LocalDateTime.now().format(DT_FMT);

        // 등록 다이얼로그를 열 때 미리 예약해 화면에 보여준 번호가 있으면(그리고 그 사이 충돌이 없었으면)
        // 그 번호를 그대로 사용해 화면에 보인 등록번호 = 실제 저장된 등록번호가 되도록 한다.
        String instId;
        synchronized (reserveLock) {
            String reservedNo = request.getWorkOrderNo();
            if (reservedNo != null && !reservedNo.isBlank()
                    && RESERVED_NOS.containsKey(reservedNo)
                    && !itWorkReportRepository.existsByInstId(reservedNo)) {
                instId = reservedNo;
            } else {
                instId = generateUniqueInstId();
            }
            RESERVED_NOS.remove(instId);
        }

        String expectedFinishedDt = toExpectedFinishedDt(request.getExpectedFinishedDt());

        ItWorkReportVo report = ItWorkReportVo.builder()
                .instId(instId)
                .reqId(fromRequest ? sourceRequestNo : instId)
                .changeTitle(request.getChangeTitle())
                .changeReason(request.getChangeReason())
                .systemCd(request.getSystemCd())
                .serviceType(request.getServiceType())
                .workType(request.getWorkType())
                .workGubun(request.getWorkGubun())
                .workLevel(request.getWorkLevel())
                .workPeriod(request.getWorkPeriod())
                .workChangePeriod(request.getWorkPeriod())
                .workPeriodManual(request.getWorkDuration())
                .expectedFinishedDt(expectedFinishedDt)
                .drsImptYn(request.getDrsImptYn())
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

        userInfoSecretRepository.save(UserInfoSecretVo.builder()
                .instId(instId)
                .docType(DOC_TYPE_WORK_ORDER)
                .reqId(instId)
                .isSecret(request.getIsSecret())
                .oppbClYn(secretSubField(request.getIsSecret(), request.getOppbClYn()))
                .userSecretContent(secretSubField(request.getIsSecret(), request.getUserSecretContent()))
                .attachExpireDate(toAttachExpireDate(request.getIsSecret(), request.getAttachExpireDate()))
                .build());

        saveAttachments(instId, now, files);

        if (fromRequest) {
            // 지시서 작성 자체가 곧 103(요청서 접수) 완료 처리다 - 별도 승인 버튼 없이, 방금 만든
            // 지시서가 REQ_ID로 이 요청서를 가리키게 된 시점에 103 이력을 남기고 NSign을 정리한다.
            appendHistory(sourceRequestNo, "103", ApprovalFlow.ACT_ID_NM.get("103"), "S", creatorSabun, creatorName, now, null);
            nSignRepository.deleteById(new NSignPk(sourceRequestNo, creatorSabun));
            RealWorkReportVo realWorkReport = requireRealWorkReport(sourceRequestNo);
            realWorkReport.setStatusCd("104");
            realWorkReport.setStatusNm(ApprovalFlow.ACT_ID_NM.get("104"));
            realWorkReportRepository.save(realWorkReport);
        }

        return instId;
    }

    /**
     * 작업 요청서 등록. its_it_work_report가 아니라 its_real_work_report에 저장하고, 요청서 작성(100)을
     * 즉시 완료 처리한 뒤 지정된 요청서 승인(102, 한전 비IT부서 파트장) 결재자에게 넘긴다.
     * 102 승인/반송은 approveWorkRequest/returnWorkRequestToPrevious가 처리한다. 103(요청서 접수)은
     * 별도 승인 버튼이 없고, 103 담당자로 지정된 사람이 createWorkOrder(sourceRequestNo=이 요청서 번호)를
     * 호출해 실제 지시서를 작성하는 행위 자체가 103 완료 처리를 겸한다.
     */
    @Override
    @Transactional
    public String createWorkRequest(String creatorSabun, String creatorName, String creatorDepCd, String creatorDepNm,
                                     WorkMyDto.CreateWorkRequestReq request, List<MultipartFile> files) throws IOException {
        if (request.getChangeTitle() == null || request.getChangeTitle().isBlank()) {
            throw new IllegalStateException("제목을 입력해야 합니다.");
        }
        if (request.getInitialApproverSabun() == null || request.getInitialApproverSabun().isBlank()) {
            throw new IllegalStateException("요청서 승인 결재자를 지정해야 합니다.");
        }
        // [2026-07-23, 사용자 확인] 작업 요청서는 영업배전시스템실이 "받아서 처리하는" 대상이므로,
        // 그 소속 사람 본인은 요청서를 등록할 수 없다(반대로 지시서 결재는 그 소속만 가능 -
        // salesDistributionSystemCandidates()/createWorkOrder() 참고).
        if (isSalesDistributionSystem(creatorSabun)) {
            throw new IllegalStateException("영업배전시스템실 소속은 작업 요청서를 등록할 수 없습니다.");
        }

        String now = LocalDateTime.now().format(DT_FMT);

        // 등록 다이얼로그를 열 때 미리 예약해 화면에 보여준 번호가 있으면(그리고 그 사이 충돌이 없었으면)
        // 그 번호를 그대로 사용한다 (createWorkOrder와 동일한 채번 예약 메커니즘을 공유해서 쓴다).
        String instId;
        synchronized (reserveLock) {
            String reservedNo = request.getWorkRequestNo();
            if (reservedNo != null && !reservedNo.isBlank()
                    && RESERVED_NOS.containsKey(reservedNo)
                    && !itWorkReportRepository.existsByInstId(reservedNo)
                    && !realWorkReportRepository.existsById(reservedNo)) {
                instId = reservedNo;
            } else {
                instId = generateUniqueInstId();
            }
            RESERVED_NOS.remove(instId);
        }

        RealWorkReportVo realWorkReport = RealWorkReportVo.builder()
                .instId(instId)
                .changeTitle(request.getChangeTitle())
                .chgRsnCtt(request.getChgRsnCtt())
                .changeReason(request.getChangeReason())
                .serviceType(request.getServiceType())
                .serviceTypeNm(codeNameOf(WorkOrderCodeOptions.SERVICE_TYPE_GUBUN, request.getServiceType()))
                .systemCd(request.getSystemCd())
                .statusCd("102")
                .statusNm(ApprovalFlow.ACT_ID_NM.get("102"))
                .expectedDt(toExpectedFinishedDt(request.getExpectedDt()))
                .workStartDt(now)
                .regUserSabun(creatorSabun)
                .regUserName(creatorName)
                .regUserDepCd(creatorDepCd)
                .regUserDepNm(creatorDepNm)
                .build();
        realWorkReportRepository.save(realWorkReport);

        userInfoSecretRepository.save(UserInfoSecretVo.builder()
                .instId(instId)
                .docType(DOC_TYPE_REQUEST)
                .reqId(instId)
                .isSecret(request.getIsSecret())
                .oppbClYn(secretSubField(request.getIsSecret(), request.getOppbClYn()))
                .userSecretContent(secretSubField(request.getIsSecret(), request.getUserSecretContent()))
                .attachExpireDate(toAttachExpireDate(request.getIsSecret(), request.getAttachExpireDate()))
                .build());

        appendHistory(instId, "100", ApprovalFlow.ACT_ID_NM.get("100"), "S", creatorSabun, creatorName, now, null);

        nSignRepository.save(NSignVo.builder()
                .instId(instId)
                .sabun(request.getInitialApproverSabun())
                .actId("102")
                .name(request.getInitialApproverName())
                .build());

        saveRealWorkReportAttachments(instId, now, files);

        return instId;
    }

    /** 104(지시서 작성)/106(지시서 승인) 단계는 항상 한전 담당자가 처리하므로, 이 두 단계로만 수정을 열어주면 곧 한전 담당자만 수정할 수 있다는 뜻이 된다. */
    private static final Set<String> EDITABLE_ACT_IDS = Set.of("104", "106");

    @Override
    @Transactional
    public void updateWorkOrder(String instId, String sabun, WorkMyDto.UpdateRequest request,
                                 List<MultipartFile> newFiles) throws IOException {
        NSignVo current = requireCurrentApprover(instId, sabun);
        if (!EDITABLE_ACT_IDS.contains(current.getActId())) {
            throw new IllegalStateException("지시서 작성/승인 단계에서만 수정할 수 있습니다.");
        }
        if (request.getChangeTitle() == null || request.getChangeTitle().isBlank()) {
            throw new IllegalStateException("제목을 입력해야 합니다.");
        }

        ItWorkReportVo report = requireReport(instId);
        report.setChangeTitle(request.getChangeTitle());
        report.setChangeReason(request.getChangeReason());
        report.setSystemCd(request.getSystemCd());
        report.setServiceType(request.getServiceType());
        report.setWorkType(request.getWorkType());
        report.setWorkGubun(request.getWorkGubun());
        report.setWorkLevel(request.getWorkLevel());
        report.setWorkPeriod(request.getWorkPeriod());
        report.setWorkChangePeriod(request.getWorkPeriod());
        report.setWorkPeriodManual(request.getWorkDuration());
        report.setExpectedFinishedDt(toExpectedFinishedDt(request.getExpectedFinishedDt()));
        report.setDrsImptYn(request.getDrsImptYn());
        itWorkReportRepository.save(report);

        UserInfoSecretPk secretPk = new UserInfoSecretPk(instId, DOC_TYPE_WORK_ORDER, instId);
        UserInfoSecretVo secret = userInfoSecretRepository.findById(secretPk)
                .orElse(UserInfoSecretVo.builder()
                        .instId(instId)
                        .docType(DOC_TYPE_WORK_ORDER)
                        .reqId(instId)
                        .build());
        secret.setIsSecret(request.getIsSecret());
        secret.setOppbClYn(secretSubField(request.getIsSecret(), request.getOppbClYn()));
        secret.setUserSecretContent(secretSubField(request.getIsSecret(), request.getUserSecretContent()));
        secret.setAttachExpireDate(toAttachExpireDate(request.getIsSecret(), request.getAttachExpireDate()));
        userInfoSecretRepository.save(secret);

        removeAttachments(instId, request.getRemoveAttachSeqs());

        String now = LocalDateTime.now().format(DT_FMT);
        int nextSeq = itWorkReportAttachRepository.findByInstId(instId).stream()
                .mapToInt(a -> Integer.parseInt(a.getSeq()))
                .max()
                .orElse(0) + 1;
        saveAttachments(instId, now, newFiles, nextSeq);
    }

    /** 수정 시 사용자가 지정한 기존 첨부파일을 삭제한다 (실제 파일 + 레코드). */
    private void removeAttachments(String instId, List<String> removeSeqs) {
        if (removeSeqs == null || removeSeqs.isEmpty()) return;
        Set<String> targetSeqs = Set.copyOf(removeSeqs);
        List<ItWorkReportAttachVo> targets = itWorkReportAttachRepository.findByInstId(instId).stream()
                .filter(a -> targetSeqs.contains(a.getSeq()))
                .collect(Collectors.toList());
        for (ItWorkReportAttachVo attach : targets) {
            try {
                Files.deleteIfExists(Paths.get(attach.getFileLocation()));
            } catch (IOException e) {
                log.warn("업무지시서 첨부파일 삭제 실패: instId={}, seq={}", instId, attach.getSeq(), e);
            }
        }
        itWorkReportAttachRepository.deleteAll(targets);
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
        boolean canEdit = myTurn && EDITABLE_ACT_IDS.contains(currentActId);
        boolean canExtendPeriod = "109".equals(currentActId)
                && sabun != null && sabun.equals(report.getRegUserSabun());

        // 요청서에서 전환된 지시서(REQ_ID가 자기 자신이 아니라 원본 요청서를 가리킴)인지 여부
        boolean fromRequest = report.getReqId() != null && !report.getReqId().isBlank() && !report.getReqId().equals(instId);

        List<WorkMyDto.ApprovalHistoryItem> approvalHistory = new ArrayList<>();
        // fromRequest인 경우, 그 요청서의 100/102/103 결재이력을 지시서 자체 이력보다 먼저(시간순) 붙여준다.
        if (fromRequest) {
            workHistoryRepository.findByInstIdOrderBySeqAsc(report.getReqId()).stream()
                    .map(this::toApprovalHistoryItem)
                    .forEach(approvalHistory::add);
        }
        workHistoryRepository.findByInstIdOrderBySeqAsc(instId).stream()
                .map(this::toApprovalHistoryItem)
                .forEach(approvalHistory::add);
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

        UserInfoSecretVo workResultSecretInfo = userInfoSecretRepository
                .findById(new UserInfoSecretPk(instId, DOC_TYPE_WORK_RESULT, instId))
                .orElse(null);
        String workResultIsSecret = workResultSecretInfo != null ? workResultSecretInfo.getIsSecret() : null;

        WorkMyDto.WorkResultItem workResultItem = workResultRepository.findByInstId(instId).stream().findFirst()
                .map(r -> WorkMyDto.WorkResultItem.builder()
                        .result(r.getResult())
                        .workerName(r.getWorkerName())
                        .regDt(r.getRegDt())
                        .attachments(workResultAttachments)
                        .isSecret(workResultIsSecret)
                        .isSecretLabel("Y".equals(workResultIsSecret) ? "포함" : "N".equals(workResultIsSecret) ? "미포함" : null)
                        .oppbClYn(workResultSecretInfo != null ? workResultSecretInfo.getOppbClYn() : null)
                        .userSecretContent(workResultSecretInfo != null ? workResultSecretInfo.getUserSecretContent() : null)
                        .attachExpireDate(workResultSecretInfo != null ? toDisplayDate(workResultSecretInfo.getAttachExpireDate()) : null)
                        .build())
                .orElse(null);

        UserInfoSecretVo secretInfo = userInfoSecretRepository
                .findById(new UserInfoSecretPk(instId, DOC_TYPE_WORK_ORDER, instId))
                .orElse(null);
        String isSecret = secretInfo != null ? secretInfo.getIsSecret() : null;

        SystemInfoVo systemInfo = (report.getSystemCd() != null && !report.getSystemCd().isBlank())
                ? systemInfoRepository.findBySystemCd(report.getSystemCd()).orElse(null)
                : null;

        // 희망완료일: its_real_work_report.INST_ID = its_it_work_report.REQ_ID로 조인해 조회하는 읽기 전용 값
        RealWorkReportVo realWorkReport = (report.getReqId() != null && !report.getReqId().isBlank())
                ? realWorkReportRepository.findById(report.getReqId()).orElse(null)
                : null;

        // 요청서 기반 전환 지시서인 경우에만 원본 요청서의 작성자(요청자) 정보를 채운다.
        // 전화번호는 its_real_work_report에 없어 사번으로 its_kepco_user를 조회해 가져온다(유선 우선, 없으면 휴대폰).
        // REG_USER_SABUN엔 8자리로 잘린 사번이 들어있어 findBySabun(10자리 PK) 대신 끝자리 매칭으로 역조회한다.
        KepcoUserVo requesterUser = realWorkReport != null
                ? kepcoUserRepository.findFirstBySabunEndingWith(realWorkReport.getRegUserSabun()).orElse(null)
                : null;
        String requesterTel = requesterUser != null
                ? (requesterUser.getTel() != null && !requesterUser.getTel().isBlank()
                        ? requesterUser.getTel() : requesterUser.getHp())
                : null;

        return WorkMyDto.Detail.builder()
                .workOrderNo(instId)
                .sourceRequestNo(fromRequest ? report.getReqId() : null)
                .regUserDepNm(report.getRegUserDepNm())
                .requesterDepNm(realWorkReport != null ? realWorkReport.getRegUserDepNm() : null)
                .requesterName(realWorkReport != null ? realWorkReport.getRegUserName() : null)
                .requesterSabun(realWorkReport != null ? realWorkReport.getRegUserSabun() : null)
                .requesterTel(requesterTel)
                .changeTitle(report.getChangeTitle())
                .changeReason(report.getChangeReason())
                .systemCd(report.getSystemCd())
                .systemCdLabel(systemInfo != null ? systemInfo.getCmsName() : null)
                .businessField(systemInfo != null ? systemInfo.getDomainName() + "-" + systemInfo.getSubdomainName() : null)
                .serviceType(report.getServiceType())
                .serviceTypeLabel(codeNameOf(WorkOrderCodeOptions.SERVICE_TYPE_GUBUN, report.getServiceType()))
                .workType(report.getWorkType())
                .workTypeLabel(codeNameOf(WorkOrderCodeOptions.WORK_TYPE_GUBUN, report.getWorkType()))
                .workGubun(report.getWorkGubun())
                .workGubunLabel(codeNameOf(WorkOrderCodeOptions.WORK_GUBUN_GUBUN, report.getWorkGubun()))
                .workLevel(report.getWorkLevel())
                .workPeriod(report.getWorkChangePeriod())
                .workDuration(report.getWorkPeriodManual())
                .expectedFinishedDt(toDisplayDate(report.getExpectedFinishedDt()))
                .hopeFinishedDt(realWorkReport != null ? toDisplayDate(realWorkReport.getExpectedDt()) : null)
                .targetDepNm(resolveDepNm(report.getWorkerDepCd()))
                .drsImptYn(report.getDrsImptYn())
                .drsImptLabel(WorkOrderCodeOptions.labelOf(WorkOrderCodeOptions.DRS_IMPT_YN, report.getDrsImptYn()))
                .isSecret(isSecret)
                .isSecretLabel("Y".equals(isSecret) ? "포함" : "N".equals(isSecret) ? "미포함" : null)
                .oppbClYn(secretInfo != null ? secretInfo.getOppbClYn() : null)
                .userSecretContent(secretInfo != null ? secretInfo.getUserSecretContent() : null)
                .attachExpireDate(secretInfo != null ? toDisplayDate(secretInfo.getAttachExpireDate()) : null)
                .attachments(attachments)
                .currentActId(currentActId)
                .myTurn(myTurn)
                .canEdit(canEdit)
                .canDelete(sabun != null && sabun.equals(report.getRegUserSabun()))
                .canExtendPeriod(canExtendPeriod)
                .workResult(workResultItem)
                .approvalHistory(approvalHistory)
                .build();
    }

    @Override
    @Transactional
    public WorkMyDto.Detail extendWorkPeriod(String instId, String sabun, String newWorkPeriod) {
        ItWorkReportVo report = requireReport(instId);
        if (sabun == null || !sabun.equals(report.getRegUserSabun())) {
            throw new IllegalStateException("본인이 등록한 업무지시서만 처리기간을 연장할 수 있습니다.");
        }

        String currentActId = nSignRepository.findByInstId(instId).stream().findFirst()
                .map(NSignVo::getActId).orElse(null);
        if (!"109".equals(currentActId)) {
            throw new IllegalStateException("결과 보고(KDN 대리) 단계가 결재 대기 중일 때만 처리기간을 연장할 수 있습니다.");
        }

        int currentPeriod = parseWorkPeriod(report.getWorkChangePeriod());
        int newPeriod = parseWorkPeriodInput(newWorkPeriod);
        if (newPeriod <= currentPeriod) {
            throw new IllegalStateException("처리기간은 기존(" + currentPeriod + "일)보다 늘려야 합니다.");
        }

        LocalDateTime currentExpected = (report.getExpectedFinishedDt() != null
                && report.getExpectedFinishedDt().length() == 14)
                ? LocalDateTime.parse(report.getExpectedFinishedDt(), DT_FMT)
                : LocalDateTime.now();
        LocalDateTime newExpected = currentExpected.plusDays(newPeriod - currentPeriod);

        report.setWorkChangePeriod(String.valueOf(newPeriod));
        report.setExpectedFinishedDt(newExpected.format(DT_FMT));
        itWorkReportRepository.save(report);

        return getDetail(instId, sabun);
    }

    private int parseWorkPeriod(String value) {
        if (value == null || value.isBlank()) return 0;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int parseWorkPeriodInput(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("처리기간을 입력해야 합니다.");
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("처리기간은 숫자로 입력해야 합니다.");
        }
    }

    @Override
    @Transactional
    public void deleteWorkOrder(String instId, String sabun) {
        ItWorkReportVo report = requireReport(instId);
        if (!sabun.equals(report.getRegUserSabun())) {
            throw new IllegalStateException("본인이 등록한 업무지시서만 삭제할 수 있습니다.");
        }

        String now = LocalDateTime.now().format(DT_FMT);
        itWorkReportDelRepository.save(ItWorkReportDelVo.builder()
                .instId(report.getInstId())
                .reqId(report.getReqId())
                .changeTitle(report.getChangeTitle())
                .changeReason(report.getChangeReason())
                .serviceType(report.getServiceType())
                .systemCd(report.getSystemCd())
                .workGubun(report.getWorkGubun())
                .exptFpCntEi(report.getExptFpCntEi())
                .exptFpCntEo(report.getExptFpCntEo())
                .exptFpCntEq(report.getExptFpCntEq())
                .exptFpCntIlf(report.getExptFpCntIlf())
                .exptFpCntEif(report.getExptFpCntEif())
                .workType(report.getWorkType())
                .workLevel(report.getWorkLevel())
                .workPeriodManual(report.getWorkPeriodManual())
                .workPeriod(report.getWorkPeriod())
                .workChangePeriod(report.getWorkChangePeriod())
                .workChangePeriodCmt(report.getWorkChangePeriodCmt())
                .expectedFinishedDt(report.getExpectedFinishedDt())
                .workCode(report.getWorkCode())
                .requestCode(report.getRequestCode())
                .workStartDt(report.getWorkStartDt())
                .workEndDt(report.getWorkEndDt())
                .reportDt(report.getReportDt())
                .isSuccess(report.getIsSuccess())
                .satisfyPoint(report.getSatisfyPoint())
                .satisfyCnt(report.getSatisfyCnt())
                .actId(report.getActId())
                .approve1Sabun(report.getApprove1Sabun())
                .approve1Name(report.getApprove1Name())
                .approve1Dt(report.getApprove1Dt())
                .approve2Sabun(report.getApprove2Sabun())
                .approve2Name(report.getApprove2Name())
                .approve2Dt(report.getApprove2Dt())
                .approve3Sabun(report.getApprove3Sabun())
                .approve3Name(report.getApprove3Name())
                .approve3Dt(report.getApprove3Dt())
                .regUserSabun(report.getRegUserSabun())
                .regUserName(report.getRegUserName())
                .regUserDepCd(report.getRegUserDepCd())
                .regUserDepNm(report.getRegUserDepNm())
                .regDt(report.getRegDt())
                .workerSabun(report.getWorkerSabun())
                .workerName(report.getWorkerName())
                .workerDepCd(report.getWorkerDepCd())
                .workerWorkCnt(report.getWorkerWorkCnt())
                .workerWorkDays(report.getWorkerWorkDays())
                .itmsInstId(report.getItmsInstId())
                .exptFpSum(report.getExptFpSum())
                .changeRoot(report.getChangeRoot())
                .delDt(now)
                .delSabun(sabun)
                .build());

        List<ItWorkReportAttachVo> attachments = itWorkReportAttachRepository.findByInstId(instId);
        for (ItWorkReportAttachVo attach : attachments) {
            try {
                Files.deleteIfExists(Paths.get(attach.getFileLocation()));
            } catch (IOException e) {
                log.warn("업무지시서 첨부파일 삭제 실패: instId={}, seq={}", instId, attach.getSeq(), e);
            }
        }
        itWorkReportAttachRepository.deleteAll(attachments);

        List<WorkResultAttachVo> workResultAttachments = workResultAttachRepository.findByInstId(instId);
        for (WorkResultAttachVo attach : workResultAttachments) {
            try {
                Files.deleteIfExists(Paths.get(attach.getFileLocation()));
            } catch (IOException e) {
                log.warn("작업결과 첨부파일 삭제 실패: instId={}, seq={}", instId, attach.getSeq(), e);
            }
        }
        workResultAttachRepository.deleteAll(workResultAttachments);

        itWorkReportRepository.delete(report);
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

    // ── 작업 요청서(MY) - 100/102 관련자 조회 + 102(요청서 승인) 승인·반송 ──────────────────
    // 103(요청서 접수)은 approveWorkRequest로 "승인"하는 단계가 아니다 - ApprovalFlow.REQUEST_NEXT에
    // 여전히 "103" 키가 없어 시도하면 예외로 막히며, 이는 의도된 동작이다. 103은 createWorkOrder의
    // sourceRequestNo 경로(위 createWorkOrder 참고)로 완료된다: 103 담당자가 이 요청서를 기반으로
    // 실제 지시서를 작성해야 비로소 103 이력이 남고 NSign이 정리된다. 103도 반송(returnWorkRequestToPrevious,
    // 102로 되돌림)은 그대로 지원한다.

    @Override
    public List<WorkMyDto.RequestListItem> getWorkRequestsMyList(String sabun) {
        return realWorkReportRepository.findAll().stream()
                .filter(r -> isRelatedToRequest(r, sabun))
                .map(r -> toRequestListItem(r, sabun))
                .collect(Collectors.toList());
    }

    /** 등록자 본인이거나, 결재이력에 처리자로 등장했거나, 현재 결재 대기자인 경우 관련자로 본다. */
    private boolean isRelatedToRequest(RealWorkReportVo r, String sabun) {
        if (sabun.equals(r.getRegUserSabun())) {
            return true;
        }
        if (workHistoryRepository.findByInstIdOrderBySeqAsc(r.getInstId()).stream()
                .anyMatch(h -> sabun.equals(h.getRegSabun()))) {
            return true;
        }
        return nSignRepository.findByInstId(r.getInstId()).stream()
                .anyMatch(sign -> sabun.equals(sign.getSabun()));
    }

    private WorkMyDto.RequestListItem toRequestListItem(RealWorkReportVo r, String sabun) {
        String instId = r.getInstId();
        Optional<NSignVo> currentSign = nSignRepository.findByInstId(instId).stream().findFirst();
        boolean myTurn = currentSign.map(NSignVo::getSabun).map(s -> s.equals(sabun)).orElse(false);

        List<WorkMyDto.ApprovalHistoryItem> approvalHistory = new ArrayList<>(
                workHistoryRepository.findByInstIdOrderBySeqAsc(instId).stream()
                        .map(this::toApprovalHistoryItem)
                        .collect(Collectors.toList()));
        currentSign.ifPresent(sign -> approvalHistory.add(toPendingApprovalHistoryItem(sign)));

        return WorkMyDto.RequestListItem.builder()
                .workRequestNo(instId)
                .title(r.getChangeTitle())
                .approvalStatus(myTurn ? "결재 대기" : "미요청")
                // 협의/완료는 요청서에 아직 없는 개념이라 "처리 중"으로 고정한다.
                .status("처리 중")
                .dueDt(r.getExpectedDt()) // 프론트에서 앞 8자리만 잘라 표시하므로 지시서 목록과 동일하게 원본 그대로 넘긴다.
                .approvalHistory(approvalHistory)
                .build();
    }

    @Override
    public WorkMyDto.NextCandidatesResponse getRequestNextCandidates(String instId, String sabun) {
        NSignVo current = requireCurrentApprover(instId, sabun);
        String nextActId = ApprovalFlow.requestNext(current.getActId());
        List<WorkMyDto.Candidate> candidates = (nextActId == null) ? List.of() : salesDistributionSystemCandidates();
        return WorkMyDto.NextCandidatesResponse.builder()
                .currentActId(current.getActId())
                .candidates(candidates)
                .build();
    }

    @Override
    @Transactional
    public void approveWorkRequest(String instId, String sabun, String sabunName, String nextSabun, String nextName) {
        NSignVo current = requireCurrentApprover(instId, sabun);
        String currentActId = current.getActId();
        String nextActId = ApprovalFlow.requestNext(currentActId);
        if (nextActId == null) {
            throw new IllegalStateException("이미 완료된 건이거나 아직 지원하지 않는 진행단계입니다.");
        }
        if (nextSabun == null || nextSabun.isBlank()) {
            throw new IllegalStateException("다음 단계 담당자를 지정해야 합니다.");
        }

        String now = LocalDateTime.now().format(DT_FMT);
        appendHistory(instId, currentActId, ApprovalFlow.ACT_ID_NM.get(currentActId), "S", sabun, sabunName, now, null);
        nSignRepository.deleteById(new NSignPk(instId, sabun));
        nSignRepository.save(NSignVo.builder()
                .instId(instId)
                .sabun(nextSabun)
                .actId(nextActId)
                .name(nextName)
                .build());

        RealWorkReportVo realWorkReport = requireRealWorkReport(instId);
        realWorkReport.setStatusCd(nextActId);
        realWorkReport.setStatusNm(ApprovalFlow.ACT_ID_NM.get(nextActId));
        // 102 승인 시점이 바로 "IT담당자" 배정 시점이다 (등록 화면에서 "결재 진행 후 배정됩니다"라던 그 항목).
        if ("103".equals(nextActId)) {
            realWorkReport.setSendItSabun(nextSabun);
            realWorkReport.setSendItName(nextName);
        }
        realWorkReportRepository.save(realWorkReport);
    }

    @Override
    public WorkMyDto.ReturnTargetsResponse getRequestReturnTargets(String instId, String sabun) {
        NSignVo current = requireCurrentApprover(instId, sabun);
        List<String> priorSteps = ApprovalFlow.requestPriorSteps(current.getActId());

        List<WorkHistoryVo> history = workHistoryRepository.findByInstIdOrderBySeqAsc(instId);
        Map<String, WorkHistoryVo> lastByActId = new LinkedHashMap<>();
        history.forEach(h -> lastByActId.put(h.getActId(), h));

        List<WorkMyDto.ReturnTarget> targets = priorSteps.stream()
                .map(actId -> {
                    WorkHistoryVo h = lastByActId.get(actId);
                    return WorkMyDto.ReturnTarget.builder()
                            .actId(actId)
                            .actIdNm(ApprovalFlow.ACT_ID_NM.get(actId))
                            .sabun(h != null ? h.getRegSabun() : null)
                            .name(h != null ? h.getRegName() : null)
                            .build();
                })
                .collect(Collectors.toList());

        return WorkMyDto.ReturnTargetsResponse.builder()
                .currentActId(current.getActId())
                .targets(targets)
                .build();
    }

    @Override
    @Transactional
    public void returnWorkRequestToPrevious(String instId, String sabun, String sabunName, String reason,
                                             String targetActId) {
        NSignVo current = requireCurrentApprover(instId, sabun);
        String currentActId = current.getActId();
        List<String> priorSteps = ApprovalFlow.requestPriorSteps(currentActId);
        if (priorSteps.isEmpty()) {
            throw new IllegalStateException("반송할 이전 단계가 없습니다.");
        }
        String resolvedTargetActId = (targetActId == null || targetActId.isBlank())
                ? priorSteps.get(0)
                : targetActId;
        if (!priorSteps.contains(resolvedTargetActId)) {
            throw new IllegalStateException("반송할 수 없는 단계입니다.");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalStateException("반송 사유를 입력해야 합니다.");
        }

        List<WorkHistoryVo> history = workHistoryRepository.findByInstIdOrderBySeqAsc(instId);
        WorkHistoryVo targetHistory = history.stream()
                .filter(h -> resolvedTargetActId.equals(h.getActId()))
                .reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalStateException("이전 단계 처리자를 찾을 수 없습니다."));

        String now = LocalDateTime.now().format(DT_FMT);
        appendHistory(instId, currentActId, ApprovalFlow.ACT_ID_NM.get(currentActId), "R", sabun, sabunName, now, reason);
        nSignRepository.deleteById(new NSignPk(instId, sabun));
        nSignRepository.save(NSignVo.builder()
                .instId(instId)
                .sabun(targetHistory.getRegSabun())
                .actId(resolvedTargetActId)
                .name(targetHistory.getRegName())
                .build());

        RealWorkReportVo realWorkReport = requireRealWorkReport(instId);
        realWorkReport.setStatusCd(resolvedTargetActId);
        realWorkReport.setStatusNm(ApprovalFlow.ACT_ID_NM.get(resolvedTargetActId));
        realWorkReportRepository.save(realWorkReport);
    }

    @Override
    public WorkMyDto.RequestDetail getRequestDetail(String instId, String sabun) {
        RealWorkReportVo r = requireRealWorkReport(instId);

        List<WorkMyDto.AttachmentItem> attachments = realWorkReportAttachRepository.findByInstId(instId).stream()
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
                workHistoryRepository.findByInstIdOrderBySeqAsc(instId).stream()
                        .map(this::toApprovalHistoryItem)
                        .collect(Collectors.toList()));
        currentSign.ifPresent(sign -> approvalHistory.add(toPendingApprovalHistoryItem(sign)));

        SystemInfoVo systemInfo = (r.getSystemCd() != null && !r.getSystemCd().isBlank())
                ? systemInfoRepository.findBySystemCd(r.getSystemCd()).orElse(null)
                : null;

        UserInfoSecretVo secretInfo = userInfoSecretRepository
                .findById(new UserInfoSecretPk(instId, DOC_TYPE_REQUEST, instId))
                .orElse(null);
        String isSecret = secretInfo != null ? secretInfo.getIsSecret() : null;

        return WorkMyDto.RequestDetail.builder()
                .workRequestNo(instId)
                .changeTitle(r.getChangeTitle())
                .chgRsnCtt(r.getChgRsnCtt())
                .changeReason(r.getChangeReason())
                .serviceType(r.getServiceType())
                .serviceTypeLabel(codeNameOf(WorkOrderCodeOptions.SERVICE_TYPE_GUBUN, r.getServiceType()))
                .systemCd(r.getSystemCd())
                .systemCdLabel(systemInfo != null ? systemInfo.getCmsName() : null)
                .businessField(systemInfo != null ? systemInfo.getDomainName() + "-" + systemInfo.getSubdomainName() : null)
                .expectedDt(toDisplayDate(r.getExpectedDt()))
                .sendItSabun(r.getSendItSabun())
                .sendItName(r.getSendItName())
                .workSabun(r.getWorkSabun())
                .workName(r.getWorkName())
                .regUserSabun(r.getRegUserSabun())
                .regUserName(r.getRegUserName())
                .regUserDepNm(r.getRegUserDepNm())
                .regDt(r.getWorkStartDt())
                .isSecret(isSecret)
                .isSecretLabel("Y".equals(isSecret) ? "포함" : "N".equals(isSecret) ? "미포함" : null)
                .oppbClYn(secretInfo != null ? secretInfo.getOppbClYn() : null)
                .userSecretContent(secretInfo != null ? secretInfo.getUserSecretContent() : null)
                .attachExpireDate(secretInfo != null ? toDisplayDate(secretInfo.getAttachExpireDate()) : null)
                .attachments(attachments)
                .currentActId(currentActId)
                .myTurn(myTurn)
                .approvalHistory(approvalHistory)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkMyDto.DownloadFile downloadRequestAttach(String instId, String seq) throws MalformedURLException {
        RealWorkReportAttachVo attach = realWorkReportAttachRepository.findByInstId(instId).stream()
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

    private RealWorkReportVo requireRealWorkReport(String instId) {
        return realWorkReportRepository.findById(instId)
                .orElseThrow(() -> new IllegalStateException("작업 요청서를 찾을 수 없습니다."));
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
        saveAttachments(instId, now, files, 1);
    }

    private void saveAttachments(String instId, String now, List<MultipartFile> files, int startSeq) throws IOException {
        if (files == null || files.isEmpty()) return;

        String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path uploadPath = Paths.get(uploadDir, dateDir, instId);
        Files.createDirectories(uploadPath);

        int seq = startSeq;
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

    private void saveRealWorkReportAttachments(String instId, String now, List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) return;

        String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path uploadPath = Paths.get(uploadDir, dateDir, instId, "request");
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

            realWorkReportAttachRepository.save(RealWorkReportAttachVo.builder()
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

    /** isSecret=Y일 때만 값을 유지하고, 아니면 null로 비워 개인정보 부가정보가 남지 않게 한다. */
    private String secretSubField(String isSecret, String value) {
        return "Y".equals(isSecret) ? value : null;
    }

    /** yyyy-MM-dd 형식 입력을 ATTACH_EXPIRE_DATE 컬럼 규격(yyyyMMdd)으로 변환한다. isSecret=Y가 아니면 null. */
    private String toAttachExpireDate(String isSecret, String dateInput) {
        if (!"Y".equals(isSecret) || dateInput == null || dateInput.isBlank()) {
            return null;
        }
        String digitsOnly = dateInput.replaceAll("[^0-9]", "");
        if (digitsOnly.length() != 8) {
            throw new IllegalStateException("파기일 형식이 올바르지 않습니다.");
        }
        return digitsOnly;
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

    /**
     * currentActId가 109(결과 보고)이고, 이 건의 마지막 처리 이력이 "114(한전) 담당자가 반송"한
     * 기록이면 그 이력을 반환한다. 결재 흐름은 분기 없이 순차 진행되므로, 109 담당자가 지금
     * 결재 대기 중이면서 마지막 이력이 (actId=114, actSign=R)이라는 것은 한전이 109(실제 작업자)를
     * 반송 대상으로 직접 지정했다는 뜻이다(110/111 담당자가 반송했다면 마지막 이력의 actId가
     * 110/111이었을 것이다). 이 경우 재작업 재제출 시 KDN 내부 재검토를 생략하고 그 한전
     * 담당자에게 바로 결재 요청한다.
     */
    private Optional<WorkHistoryVo> findLastKepcoReturnToWorker(String instId, String currentActId) {
        if (!"109".equals(currentActId)) {
            return Optional.empty();
        }
        List<WorkHistoryVo> history = workHistoryRepository.findByInstIdOrderBySeqAsc(instId);
        if (history.isEmpty()) {
            return Optional.empty();
        }
        WorkHistoryVo last = history.get(history.size() - 1);
        return ("114".equals(last.getActId()) && "R".equals(last.getActSign()))
                ? Optional.of(last)
                : Optional.empty();
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
            case "106" -> salesDistributionSystemCandidates();
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
            case "114" -> salesDistributionSystemCandidates();
            default -> List.of();
        };
    }

    private WorkMyDto.Candidate toCandidate(String sabun, String name, String roleNm) {
        return WorkMyDto.Candidate.builder().sabun(sabun).name(name).roleNm(roleNm).build();
    }

    /**
     * 작업 요청서(100→102) 최초 결재자 후보: 요청자와 같은 소속(SOSOK_CD)인 한전 담당자만 후보로 준다
     * (2026-07-22, 사용자 확인: 요청자 본인도 후보 목록에 포함, 처장은 DeptHeadUtil.isKepcoDeptHead()로 동일하게 제외).
     */
    private List<WorkMyDto.Candidate> kepcoStaffCandidatesBySosokCd(String sosokCd) {
        if (sosokCd == null || sosokCd.isBlank()) {
            return List.of();
        }
        return kepcoUserRepository.findAll().stream()
                .filter(u -> !DeptHeadUtil.isKepcoDeptHead(u))
                .filter(u -> sosokCd.equals(u.getSosokCd()))
                .map(u -> toCandidate(KepcoSabunUtil.toLegacySabun(u.getSabun()), u.getName(), "한전 담당자"))
                .collect(Collectors.toList());
    }

    /**
     * "영업배전시스템실 소속 한전 담당자"만 후보/등록 가능 여부를 가려야 하는 모든 곳에서 쓰는 공용 상수.
     * [2026-07-23, 사용자 확인] 작업지시서 결재 체인 중 한전 담당자가 처리하는 단계(106 지시서 승인,
     * 114 조치결과 승인, 그리고 요청서 없이 지시서를 바로 등록하는 104 직접등록)는 전부 이 부서
     * 소속만 결재/등록할 수 있어야 한다 - 그 반대로 작업 요청서(100 등록)는 이 부서 "가 아닌" 사람만
     * 등록할 수 있어야 한다(createWorkRequest()의 검증 참고). 즉 영업배전시스템실은 "요청을 받아서
     * 처리하는 쪽", 그 외 한전 부서는 "요청을 넣는 쪽"으로 역할이 나뉜다.
     * [2026-07-23] 판별 기준을 SOSOK_HAN(소속명, 문자열)에서 SOSOK_CD1(소속코드-본부)로 변경 -
     * 사용자 확인: 실제 영업배전시스템실의 SOSOK_CD1 값은 "70ZR".
     */
    static final String SALES_DISTRIBUTION_SYSTEM_SOSOK_CD1 = "70ZR";

    /**
     * 한전 담당자 한 명(사번 기준)이 영업배전시스템실 소속인지 확인한다.
     * createWorkOrder()/createWorkRequest() 등록 시점 검증에서 쓴다 - JwtUserDto에는 SOSOK_CD1이
     * 없으므로(depTitle=SOSOK_HAN만 있음) 사번으로 its_kepco_user를 다시 조회해서 판별한다.
     */
    private boolean isSalesDistributionSystem(String sabun) {
        return kepcoUserRepository.findFirstBySabunEndingWith(sabun)
                .map(KepcoUserVo::getSosokCd1)
                .map(SALES_DISTRIBUTION_SYSTEM_SOSOK_CD1::equals)
                .orElse(false);
    }

    /**
     * 영업배전시스템실 소속 한전 담당자만 후보로 준다(처장 제외). 아래 네 곳에서 공유해서 쓴다:
     * - 103 요청서 접수(요청서 승인(102) 다음 단계)
     * - 106 지시서 승인, 114 조치결과 승인 (resolveCandidates())
     * - 요청서를 거치지 않고 지시서를 바로 등록할 때의 최초 결재자(106) 후보 (getInitialApproverCandidates())
     * [2026-07-23] 예전엔 이 네 곳 중 106/114/직접등록만 부서 제한 없이 전체 한전 담당자를 후보로
     * 보여주던 kepcoStaffCandidates()를 썼는데, 그러면 영업배전시스템실이 아닌 다른 한전 부서
     * 사람(예: 나누리/오광식)도 지시서 결재자로 선택될 수 있었다 - 이 메서드로 통일해 막는다.
     */
    private List<WorkMyDto.Candidate> salesDistributionSystemCandidates() {
        return kepcoUserRepository.findAll().stream()
                .filter(u -> !DeptHeadUtil.isKepcoDeptHead(u))
                .filter(u -> SALES_DISTRIBUTION_SYSTEM_SOSOK_CD1.equals(u.getSosokCd1()))
                .map(u -> toCandidate(KepcoSabunUtil.toLegacySabun(u.getSabun()), u.getName(), "한전 영업배전시스템실"))
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
