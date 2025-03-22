package ru.sushi.delivery.kds.domain.service;

import com.vaadin.flow.router.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.controller.dto.MealData;
import ru.sushi.delivery.kds.domain.persist.entity.flow.Flow;
import ru.sushi.delivery.kds.domain.persist.entity.product.Meal;
import ru.sushi.delivery.kds.domain.persist.repository.flow.FlowRepository;
import ru.sushi.delivery.kds.domain.persist.repository.product.MealRepository;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class MealService {

    private final MealRepository mealRepository;
    private final FlowRepository flowRepository;

    public List<Meal> getAllMeals() {
        return mealRepository.findAll();
    }

    public List<Meal> getAllExtras() {
        return mealRepository.findAllExtras();
    }

    public List<MealData> getAllMealsDTO() {
        return mealRepository.findAll().stream().map(MealData::of).toList();
    }

    public Meal getMealById(Long menuId) {
        return mealRepository.findById(menuId)
                .orElseThrow(() -> new NotFoundException("Meal not found id " + menuId));
    }

    public void deleteMeal(MealData mealData) {
        this.mealRepository.deleteById(mealData.getId());
    }

    public void saveMeal(MealData mealData) {
        Flow flow = flowRepository.getFlowByName(mealData.getFlow())
                .orElseThrow(() -> new NotFoundException("Flow not found id " + mealData.getFlow()));

        Meal meal = Optional.ofNullable(mealData.getId())
                .map(this.mealRepository::findById)
                .flatMap(Function.identity())
                .map(m -> this.setNewMealData(m, mealData, flow))
                .orElseGet(() -> Meal.of(mealData, flow));

        mealRepository.save(meal);
    }

    public Meal setNewMealData(
            Meal meal,
            MealData mealData,
            Flow flow
    ) {
        return meal.toBuilder()
                .id(meal.getId())
                .name(mealData.getName())
                .flow(flow)
                .build();
    }
}
