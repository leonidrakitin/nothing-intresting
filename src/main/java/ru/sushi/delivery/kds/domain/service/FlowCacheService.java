package ru.sushi.delivery.kds.domain.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.flow.FlowStep;
import ru.sushi.delivery.kds.domain.persist.repository.flow.FlowStepRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlowCacheService {

    public final static int CANCEL_STEP_ORDER = -1;
    public final static int DONE_STEP_ORDER = 0;

    private final FlowStepRepository flowStepRepository;
    private final Map<Long, Map<Integer, FlowStep>> flowCache = new ConcurrentHashMap<>(); //todo guava cache!

    @PostConstruct
    public void initializeCache() {
        List<FlowStep> flowSteps = flowStepRepository.findAll();
        flowCache.putAll(
                flowSteps.stream()
                        .collect(
                                Collectors.groupingBy(
                                        flowStep -> flowStep.getFlow().getId(),
                                        Collectors.toMap(
                                                FlowStep::getStepOrder,
                                                flowStep -> flowStep,
                                                (existing, replacement) -> existing,
                                                HashMap::new
                                        )
                                )
                        )
        );

    }

    public FlowStep getStep(long flowId, int currentFlowStep) {
        return flowCache.get(flowId).get(currentFlowStep);
    }

    public FlowStep getNextStep(long flowId, int currentFlowStep) {
        int nextFlowStep = currentFlowStep + 1;
        if (flowCache.get(flowId).containsKey(nextFlowStep)) {
            return flowCache.get(flowId).get(nextFlowStep);
        } else {
            return flowCache.get(flowId).get(DONE_STEP_ORDER);
        }
    }

    public FlowStep getCanceledStep(Long flowId) {
        return flowCache.get(flowId).get(CANCEL_STEP_ORDER);
    }

    public FlowStep getDoneStep(Long flowId) {
        return flowCache.get(flowId).get(DONE_STEP_ORDER);
    }
}

