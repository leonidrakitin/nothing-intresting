package ru.sushi.delivery.kds.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sushi.delivery.kds.domain.persist.entity.Order;
import ru.sushi.delivery.kds.domain.persist.entity.OrderItem;
import ru.sushi.delivery.kds.domain.persist.repository.ProductPackageRepository;
import ru.sushi.delivery.kds.domain.persist.repository.ProductTypeRepository;

@Service
@RequiredArgsConstructor
public class ProductPackageService {

    private final ProductPackageRepository productPackageRepository;

    private final ProductTypeRepository productTypeRepository;

    public void calculatePackages(Order order) {
        for (OrderItem orderItem : order.getOrderItems()) {
            orderItem.getMenuItem().getProductType().getAllowedNeighbors();
        }
    }
}
