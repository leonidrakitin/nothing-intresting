package ru.sushi.delivery.kds.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.Order;
import ru.sushi.delivery.kds.domain.persist.entity.OrderItem;
import ru.sushi.delivery.kds.domain.persist.entity.ProductPackage;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;
import ru.sushi.delivery.kds.domain.persist.entity.product.ProductType;
import ru.sushi.delivery.kds.domain.persist.repository.ProductPackageRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PackingService {

    private final ProductPackageRepository productPackageRepository;

    public List<PackageDTO> packItems(Order order) {

        List<MenuItem> menuItems = order.getOrderItems().stream().map(OrderItem::getMenuItem).toList();

        // 1. Группируем товары по совместимости
        List<List<MenuItem>> groups = groupCompatibleItems(menuItems);
        List<ProductPackage> availablePackages = this.productPackageRepository.findAll();

        // 2. Ищем оптимальное размещение товаров в коробках
        return findOptimalPacking(groups, availablePackages);
    }

    private List<List<MenuItem>> groupCompatibleItems(List<MenuItem> menuItems) {
        List<List<MenuItem>> groups = new ArrayList<>();
        Map<ProductType, List<MenuItem>> typeMap = new HashMap<>();

        for (MenuItem item : menuItems) {
            typeMap.computeIfAbsent(item.getProductType(), k -> new ArrayList<>()).add(item);
        }

        while (!typeMap.isEmpty()) {
            List<MenuItem> group = new ArrayList<>();
            Iterator<Map.Entry<ProductType, List<MenuItem>>> iterator = typeMap.entrySet().iterator();

            Map.Entry<ProductType, List<MenuItem>> first = iterator.next();
            group.addAll(first.getValue());
            Set<ProductType> allowed = new HashSet<>(first.getKey().getAllowedNeighbors());
            iterator.remove();

            while (iterator.hasNext()) {
                Map.Entry<ProductType, List<MenuItem>> entry = iterator.next();
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

    private List<PackageDTO> findOptimalPacking(List<List<MenuItem>> groups, List<ProductPackage> packages) {
        List<PackageDTO> result = new ArrayList<>();
        for (List<MenuItem> group : groups) {
            result.addAll(findBestPackingForGroup(group, packages));
        }
        return result;
    }

    private List<PackageDTO> findBestPackingForGroup(List<MenuItem> items, List<ProductPackage> packages) {
        List<PackageDTO> bestSolution = new ArrayList<>();
        double bestCost = Double.MAX_VALUE;

        for (ProductPackage productPackage : packages) {
            List<MenuItem> remainingItems = new ArrayList<>(items);
            List<PackageDTO> currentSolution = new ArrayList<>();
            Map<ProductPackage, Integer> usedPackages = new HashMap<>();

            while (!remainingItems.isEmpty()) {
                ProductPackage packageInstance = clonePackage(productPackage, usedPackages);
                List<PackageDTO> packedItems = tryPackItems(remainingItems, packageInstance);
                if (packedItems.isEmpty()) {
                    break;
                }
                currentSolution.addAll(packedItems);
                remainingItems = getRemainingItems(remainingItems, packedItems);
            }

            double cost = currentSolution.stream().mapToDouble(p -> p.getPackage().getMenuItem().getPrice()).sum();
            double emptySpacePercentage = calculateEmptySpacePercentage(currentSolution);

            if ((emptySpacePercentage <= 0.3 || bestSolution.isEmpty()) && cost < bestCost) {
                bestSolution = currentSolution;
                bestCost = cost;
            }
        }
        return bestSolution;
    }

    private ProductPackage clonePackage(ProductPackage original, Map<ProductPackage, Integer> usedPackages) {
        int count = usedPackages.getOrDefault(original, 0) + 1;
        usedPackages.put(original, count);

        ProductPackage copy = new ProductPackage();
        copy.setHeight(original.getHeight());
        copy.setLength(original.getLength());
        copy.setWidth(original.getWidth());
        copy.setMenuItem(original.getMenuItem());
        return copy;
    }

    private List<MenuItem> getRemainingItems(List<MenuItem> originalItems, List<PackageDTO> packedItems) {
        Set<MenuItem> packedSet = packedItems.stream()
                .flatMap(p -> p.getItems().stream())
                .collect(Collectors.toSet());
        return originalItems.stream()
                .filter(item -> !packedSet.contains(item))
                .collect(Collectors.toList());
    }

    private double calculateEmptySpacePercentage(List<PackageDTO> packedItems) {
        double totalVolume = packedItems.stream().mapToDouble(p -> p.getPackage().getLength() * p.getPackage().getWidth() * p.getPackage().getHeight()).sum();
        double usedVolume = packedItems.stream().flatMap(dto -> dto.getItems().stream())
                .mapToDouble(item -> item.getProductType().getLength() * item.getProductType().getWidth() * item.getProductType().getHeight())
                .sum();
        return 1 - (usedVolume / totalVolume);
    }

    private List<PackageDTO> tryPackItems(List<MenuItem> items, ProductPackage productPackage) {
        List<PackageDTO> packedItems = new ArrayList<>();
        List<MenuItem> packed = new ArrayList<>();

        int availableLength = productPackage.getLength();
        int availableWidth = productPackage.getWidth();
        int availableHeight = productPackage.getHeight();

        Iterator<MenuItem> iterator = items.iterator();
        while (iterator.hasNext()) {
            MenuItem item = iterator.next();
            ProductType type = item.getProductType();

            if (type.getHeight() <= availableHeight &&
                    type.getLength() <= availableLength &&
                    type.getWidth() <= availableWidth) {
                packed.add(item);
                availableLength -= type.getLength();
                availableWidth -= type.getWidth();
                iterator.remove();
            }
        }

        if (!packed.isEmpty()) {
            packedItems.add(new PackageDTO(productPackage, new ArrayList<>(packed)));
        }
        return packedItems;
    }
}

class PackageDTO {
    private ProductPackage productPackage;
    private List<MenuItem> items;

    public PackageDTO(ProductPackage productPackage, List<MenuItem> items) {
        this.productPackage = productPackage;
        this.items = items;
    }

    public ProductPackage getPackage() {
        return productPackage;
    }

    public List<MenuItem> getItems() {
        return items;
    }
}