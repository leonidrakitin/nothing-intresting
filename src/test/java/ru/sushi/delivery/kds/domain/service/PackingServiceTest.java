package ru.sushi.delivery.kds.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.sushi.delivery.kds.domain.persist.entity.Order;
import ru.sushi.delivery.kds.domain.persist.entity.OrderItem;
import ru.sushi.delivery.kds.domain.persist.entity.ProductPackage;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;
import ru.sushi.delivery.kds.domain.persist.entity.product.ProductType;
import ru.sushi.delivery.kds.domain.persist.repository.ProductPackageRepository;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class PackingServiceTest {

    @InjectMocks
    private PackingService packingService;

    @Mock
    private ProductPackageRepository productPackageRepository;

    @Test
    void packItems() {
        ProductType productType2 = new ProductType();
        ProductType productType1 = new ProductType();
        productType1.setHeight(1);
        productType1.setLength(1);
        productType1.setWidth(1);
        productType1.setAllowedNeighbors(Set.of(productType2));
        productType2.setHeight(2);
        productType2.setLength(1);
        productType2.setWidth(1);
        productType2.setAllowedNeighbors(Set.of(productType1));

        MenuItem package1Item = new MenuItem();
        package1Item.setPrice(100.0);
        ProductPackage package1 = new ProductPackage();
        package1.setHeight(1);
        package1.setLength(2);
        package1.setWidth(1);
        package1.setMenuItem(package1Item);

        MenuItem package2Item = new MenuItem();
        package2Item.setPrice(50.0);
        ProductPackage package2 = new ProductPackage();
        package2.setHeight(1);
        package2.setLength(2);
        package2.setWidth(2);
        package2.setMenuItem(package2Item);

        MenuItem package3Item = new MenuItem();
        package3Item.setPrice(50.0);
        ProductPackage package3 = new ProductPackage();
        package3.setHeight(2);
        package3.setLength(1);
        package3.setWidth(1);
        package3.setMenuItem(package3Item);

//        MenuItem package4Item = new MenuItem();
//        package4Item.setPrice(50.0);
//        ProductPackage package4 = new ProductPackage();
//        package4.setHeight(2);
//        package4.setLength(1);
//        package4.setWidth();
//        package4.setMenuItem(package3Item);

        MenuItem package5Item = new MenuItem();
        package5Item.setPrice(35.0);
        ProductPackage package5 = new ProductPackage();
        package5.setHeight(1);
        package5.setLength(1);
        package5.setWidth(1);
        package5.setMenuItem(package3Item);

        MenuItem menuItem1 = new MenuItem();
        menuItem1.setProductType(productType1);
        menuItem1.setId(1L);
        MenuItem menuItem2 = new MenuItem();
        menuItem2.setProductType(productType1);
        menuItem2.setId(1L);
        MenuItem menuItem3 = new MenuItem();
        menuItem3.setProductType(productType2);
        menuItem3.setId(1L);
        Order order = new Order();
        order.setOrderItems(List.of(
                OrderItem.builder().menuItem(menuItem1).build(),
                OrderItem.builder().menuItem(menuItem2).build(),
                OrderItem.builder().menuItem(menuItem3).build()
        ));

        when(productPackageRepository.findAll()).thenReturn(List.of(package1, package2, package3, package5));
        var result = packingService.packItems(order);
        assertFalse(result.isEmpty());
        assertEquals(3, result.size()); //три разные коробки, package5 x2, и package3 x1
        assertEquals(120, result.stream().map(PackageDTO::getPackage).map(ProductPackage::getMenuItem).mapToDouble(MenuItem::getPrice).sum());
    }
}