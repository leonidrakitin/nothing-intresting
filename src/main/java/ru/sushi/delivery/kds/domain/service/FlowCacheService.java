package ru.sushi.delivery.kds.domain.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.FlowStep;
import ru.sushi.delivery.kds.domain.persist.repository.FlowStepRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlowCacheService {

    private final FlowStepRepository flowStepRepository;
    private final Map<Long, List<FlowStep>> flowCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void initializeCache() {
        List<FlowStep> flowSteps = flowStepRepository.findAll();
        flowCache.putAll(
                flowSteps.stream().collect(
                        Collectors.groupingBy(
                                flowStep -> flowStep.getFlow().getId(),
                                Collectors.toList()
                        )
                )
        );
    }

    public List<FlowStep> getFlowSteps(Long flowId) {
        return flowCache.get(flowId);
    }

    public FlowStep getCurrentStep(long flowId, int currentFlowStep) {
        List<FlowStep> flowSteps = flowCache.get(flowId);
        if (currentFlowStep > 0 && currentFlowStep - 1 < flowSteps.size()) {
            return flowSteps.get(currentFlowStep - 1);
        }
        return flowSteps.getFirst();
    }

    public FlowStep getNextStep(long flowId, int currentFlowStep) {
        List<FlowStep> flowSteps = flowCache.get(flowId);
        if (currentFlowStep >= 0 && currentFlowStep < flowSteps.size()) {
            return flowSteps.get(currentFlowStep);
        }
        return flowSteps.getLast();
    }
}

