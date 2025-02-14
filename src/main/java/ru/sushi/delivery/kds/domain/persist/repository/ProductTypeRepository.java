package ru.sushi.delivery.kds.domain.persist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sushi.delivery.kds.domain.persist.entity.product.ProductType;

import java.util.List;

@Repository
public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {

    @Query("select pt from ProductType pt left join fetch pt.allowedNeighbors")
    List<ProductType> findAll();
}