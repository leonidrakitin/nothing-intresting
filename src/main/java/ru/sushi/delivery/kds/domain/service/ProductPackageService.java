package ru.sushi.delivery.kds.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.Order;
import ru.sushi.delivery.kds.domain.persist.entity.OrderItem;
import ru.sushi.delivery.kds.domain.persist.entity.ProductPackage;
import ru.sushi.delivery.kds.domain.persist.entity.product.Meal;
import ru.sushi.delivery.kds.domain.persist.entity.product.ProductType;
import ru.sushi.delivery.kds.domain.persist.repository.ProductPackageRepository;
import ru.sushi.delivery.kds.dto.PackageDto;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductPackageService {

    private final ProductPackageRepository productPackageRepository;

    public List<PackageDto> calculatePackages(Order order) {
        List<Meal> meals = order.getOrderItems()
                .stream()
                .map(OrderItem::getMeal)
                .toList();
        List<List<Meal>> groups = this.groupCompatibleItems(meals);
        List<ProductPackage> availablePackages = this.productPackageRepository.findAll()
                .stream()
                .sorted(Comparator.comparingDouble(this::getPricePerArea))
                .toList();
        return findOptimalPacking(groups, availablePackages);
    }

    private List<List<Meal>> groupCompatibleItems(List<Meal> meals) {
        List<List<Meal>> groups = new ArrayList<>();
        Map<ProductType, List<Meal>> typeMap = new HashMap<>();

        for (Meal item : meals) {
            typeMap.computeIfAbsent(item.getProductType(), k -> new ArrayList<>()).add(item);
        }

        while (!typeMap.isEmpty()) {
            List<Meal> group = new ArrayList<>();
            Iterator<Map.Entry<ProductType, List<Meal>>> iterator = typeMap.entrySet().iterator();

            Map.Entry<ProductType, List<Meal>> first = iterator.next();
            group.addAll(first.getValue());
            Set<ProductType> allowed = new HashSet<>(first.getKey().getAllowedNeighbors());
            iterator.remove();

            while (iterator.hasNext()) {
                Map.Entry<ProductType, List<Meal>> entry = iterator.next();
                if (allowed.contains(entry.getKey())) {
                    group.addAll(entry.getValue());
                    allowed.retainAll(entry.getKey().getAllowedNeighbors());
                    iterator.remove();
                }
            }
            groups.add(group);
        }
        return groups;
    }

    private List<PackageDto> findOptimalPacking(List<List<Meal>> groups, List<ProductPackage> packages) {
        List<PackageDto> result = new ArrayList<>();
        for (List<Meal> group : groups) {
            group = group.stream()
                    .sorted(Comparator.comparingInt(meal -> meal.getProductType().getHeight()))
                    .toList();
            this.packOptimalGroup(new ArrayList<>(packages), group, result);
        }
        return result;
    }

    private void packOptimalGroup(
            List<ProductPackage> packageList,
            List<Meal> items,
            List<PackageDto> result
    ) {
        Iterator<ProductPackage> packageIterator = packageList.iterator();
        List<Meal> currentPackageItems = new ArrayList<>();
        while (packageIterator.hasNext()) {
            ProductPackage currentPackage = packageIterator.next();
            int availableArea = currentPackage.getWidth() * currentPackage.getLength();
            int availableHeight = currentPackage.getHeight();
            Iterator<Meal> remainingItems = items.iterator();

            while (remainingItems.hasNext()) {
                Meal item = remainingItems.next();
                int itemHeight = item.getProductType().getHeight();

                if (availableHeight < itemHeight) {
                    //*next package*, list remaining, list result
                    if (packageIterator.hasNext()) {
                        this.finishOrCallAgain(packageList, items, result, availableArea, currentPackage, currentPackageItems);
                    }
                    else {
                        log.error("There is no way to pack group items {}", items);
//                        return List.of();
                    }
                    return;
                }
                int itemArea = item.getProductType().getLength() * item.getProductType().getWidth();

                if (availableArea >= itemArea) {
                    availableArea -= itemArea;
                    currentPackageItems.add(item);
                } else {
                    this.finishOrCallAgain(packageList, items, result, availableArea, currentPackage, currentPackageItems);
                    return;
                }

                if (!remainingItems.hasNext()) {
                    this.finishOrCallAgain(packageList, items, result, availableArea, currentPackage, currentPackageItems);
                    return;
                }
            }
        }
    }

    private void finishOrCallAgain(
            List<ProductPackage> packageList,
            List<Meal> items,
            List<PackageDto> result,
            double availableArea,
            ProductPackage currentPackage,
            List<Meal> currentPackageItems
    ) {
        double availableSpace = availableArea / (currentPackage.getWidth() * currentPackage.getLength());
        if (availableSpace <= 0.3) {
            result.add(new PackageDto(currentPackage, currentPackageItems));
            List<Meal> itemsWithoutPackage;
            if (currentPackageItems.size() < items.size()) {
                itemsWithoutPackage = items.subList(currentPackageItems.size(), items.size());
            } else {
                itemsWithoutPackage = List.of();
            }
            this.packOptimalGroup(packageList, itemsWithoutPackage, result);
        } else if (!packageList.isEmpty() && !items.isEmpty()) {
            packageList.removeFirst();
            this.packOptimalGroup(packageList, items, result);
        } else {
            log.error("There is no way to pack group items {}", items);
        }
        log.info("finished, result {}", result);
    }

    public Double getPricePerArea(ProductPackage productPackage) {
        return productPackage.getMeal().getPrice() / (productPackage.getWidth() * productPackage.getLength());
    }
}
