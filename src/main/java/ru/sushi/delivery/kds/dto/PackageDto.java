package ru.sushi.delivery.kds.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.sushi.delivery.kds.domain.persist.entity.ProductPackage;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItem;

import java.util.List;

@Getter
@AllArgsConstructor
public class PackageDto {
    private ProductPackage productPackage;
    private List<MenuItem> items;
}
