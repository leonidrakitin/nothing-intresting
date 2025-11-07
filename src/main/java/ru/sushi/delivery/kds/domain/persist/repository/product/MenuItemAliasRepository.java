package ru.sushi.delivery.kds.domain.persist.repository.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sushi.delivery.kds.domain.persist.entity.product.MenuItemAlias;

import java.util.Optional;

@Repository
public interface MenuItemAliasRepository extends JpaRepository<MenuItemAlias, Long> {

    Optional<MenuItemAlias> findByAliasTextIgnoreCase(String aliasText);
}

