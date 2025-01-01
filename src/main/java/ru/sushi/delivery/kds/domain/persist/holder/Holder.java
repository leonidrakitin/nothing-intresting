package ru.sushi.delivery.kds.domain.persist.holder;

import java.util.Collection;
import java.util.Optional;

public interface Holder<E, I> {
    Collection<E> findAll();
    Optional<E> get(I id);
    E save(E entity);
    void delete(E entity);
    void deleteById(I id);
}
