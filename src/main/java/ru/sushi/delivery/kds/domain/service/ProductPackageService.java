package ru.sushi.delivery.kds.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.Order;
import ru.sushi.delivery.kds.domain.persist.entity.OrderItem;
import ru.sushi.delivery.kds.domain.persist.entity.ProductPackage;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;
import ru.sushi.delivery.kds.domain.persist.entity.product.ProductType;
import ru.sushi.delivery.kds.domain.persist.repository.ProductPackageRepository;
import ru.sushi.delivery.kds.dto.PackageDto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Log4j2
@Service
@RequiredArgsConstructor
public class ProductPackageService {

    private final ProductPackageRepository productPackageRepository;

    public List<PackageDto> calculatePackages(Order order) {
        List<MenuItem> menuItems = order.getOrderItems()
                .stream()
                .map(OrderItem::getMenuItem)
                .toList();
        List<List<MenuItem>> groups = this.groupCompatibleItems(menuItems);
        List<ProductPackage> availablePackages = this.productPackageRepository.findAll()
                .stream()
                .sorted(Comparator.comparingDouble(this::getPricePerArea))
                .toList();
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

    private List<PackageDto> findOptimalPacking(List<List<MenuItem>> groups, List<ProductPackage> packages) {
        List<PackageDto> result = new ArrayList<>();
        for (List<MenuItem> group : groups) {
            group = group.stream()
                    .sorted(Comparator.comparingInt(menuItem -> menuItem.getProductType().getHeight()))
                    .toList();
            this.packOptimalGroup(new ArrayList<>(packages), group, result);
        }
        return result;
    }

    private void packOptimalGroup(
            List<ProductPackage> packageList,
            List<MenuItem> items,
            List<PackageDto> result
    ) {
        Iterator<ProductPackage> packageIterator = packageList.iterator();
        List<MenuItem> currentPackageItems = new ArrayList<>();
        while (packageIterator.hasNext()) {
            ProductPackage currentPackage = packageIterator.next();
            int availableArea = currentPackage.getWidth() * currentPackage.getLength();
            int availableHeight = currentPackage.getHeight();
            Iterator<MenuItem> remainingItems = items.iterator();

            while (remainingItems.hasNext()) {
                MenuItem item = remainingItems.next();
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
            List<MenuItem> items,
            List<PackageDto> result,
            double availableArea,
            ProductPackage currentPackage,
            List<MenuItem> currentPackageItems
    ) {
        double availableSpace = availableArea / (currentPackage.getWidth() * currentPackage.getLength());
        if (availableSpace <= 0.3) {
            result.add(new PackageDto(currentPackage, currentPackageItems));
            List<MenuItem> itemsWithoutPackage;
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
        return productPackage.getMenuItem().getPrice() / (productPackage.getWidth() * productPackage.getLength());
    }
}
